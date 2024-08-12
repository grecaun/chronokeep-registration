package com.chronokeep.registration.network

import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import com.chronokeep.registration.objects.responses.ConnectionSuccessfulResponse
import com.chronokeep.registration.objects.responses.ErrorResponse
import com.chronokeep.registration.objects.responses.ParticipantsResponse
import com.chronokeep.registration.objects.responses.Response
import com.chronokeep.registration.util.Globals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class Connection(
    private val address: InetAddress,
    private val port: Int,
    private var handler: ConnectionHandler
): Runnable {
    companion object {
        @JvmStatic
        val msg_connection_closed = 1
        @JvmStatic
        val msg_connection_unavailable = 2
        @JvmStatic
        val msg_connection_open = 3
        @JvmStatic
        val msg_connection_success = 8
        @JvmStatic
        val msg_connection_participants = 10
        @JvmStatic
        val msg_connection_error = 12
    }

    private val tag = "Chrono.Con"
    private val socket: SocketChannel = SocketChannel.open()
    private var keepalive: Boolean = true

    private val format = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "command"
    }

    private fun initialize() {
        Log.d(tag, "Initializing socket connection.")
        socket.connect(InetSocketAddress(this.address, this.port))
        socket.configureBlocking(true)
        socket.socket()?.tcpNoDelay = true
    }

    private fun sendMessage(message: String) {
        val outBuf = ByteBuffer.allocate(2056)
        outBuf.put("$message\n".toByteArray())
        outBuf.flip()
        while (outBuf.hasRemaining()) {
            socket.write(outBuf)
        }
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    fun sendAsyncMessage(message: String) {
        ioScope.launch {
            withContext(Dispatchers.IO) {
                val outBuf = ByteBuffer.allocate(2056)
                outBuf.put("$message\n".toByteArray())
                outBuf.flip()
                while (outBuf.hasRemaining()) {
                    socket.write(outBuf)
                }
            }
        }
    }

    fun setHandler(handler: ConnectionHandler) {
        this.handler = handler
    }

    fun setDialogFragment(frag: Fragment) {
        this.handler.setDFrag(frag)
    }

    private fun conUnavailable() {
        Log.d(tag, "Unable to connect.")
        val msg = Message()
        msg.what = msg_connection_unavailable
        handler.sendMessage(msg)
        keepalive = false
    }

    fun alive(): Boolean {
        return keepalive
    }

    fun stop() {
        try {
            socket.close()
        } catch (_: IOException) {
            Log.d(tag, "Unable to close socket.")
        }
        keepalive = false
    }

    override fun run() {
        keepalive = true
        try {
            initialize()
        } catch (_: IOException) {
            conUnavailable()
        }
        Log.d(tag, "Sending connect message.")
        sendMessage(ConnectRequest().encode())
        try {
            val reader = BufferedReader(InputStreamReader(socket.socket().getInputStream()))
            val buffer = StringBuilder()
            Log.d(tag, "Entering loop.")
            while (keepalive) {
                Log.d(tag, "Reading line.")
                buffer.append(reader.readLine())
                buffer.append("\n")
                val json = buffer.toString().trim()
                if (json.equals("null", true) || json.isEmpty()) {
                    Log.d(tag, "Nothing read, connection closed.")
                    conUnavailable()
                } else {
                    val jsonParts = json.split("\n")
                    for (part in jsonParts) {
                        if (part.trim().isNotEmpty()) {
                            try {
                                // we actually received a message, so parse it
                                val data = format.decodeFromString<Response>(part)
                                Log.d(tag, "${data.command} message received.")
                                when (data.command) {
                                    "registration_connection_successful" -> {
                                        val conData: ConnectionSuccessfulResponse = data as ConnectionSuccessfulResponse
                                        Globals.setServerInfo(conData.name, conData.kind, conData.version)
                                        val msg = Message()
                                        msg.what = msg_connection_open
                                        handler.sendMessage(msg)
                                    }
                                    "registration_error" -> {
                                        val errorData: ErrorResponse = data as ErrorResponse
                                        Globals.setRegistrationError(errorData.error)
                                        val msg = Message()
                                        msg.what = msg_connection_error
                                        handler.sendMessage(msg)
                                    }
                                    "registration_participants" -> {
                                        val partData: ParticipantsResponse = data as ParticipantsResponse
                                        Log.d(tag, "number of participants: ${partData.participants.size}")
                                        // TODO update database with registration and update distances?
                                        val msg = Message()
                                        msg.what = msg_connection_participants
                                        handler.sendMessage(msg)
                                    }
                                    "disconnect" -> {
                                        conUnavailable()
                                    }
                                    else -> {
                                        conUnavailable()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.d(tag, "Error encountered trying to parse message. $e")
                            }
                        }
                    }
                    buffer.clear()
                }
            }
        } catch (_: IOException) {
            conUnavailable()
        }
    }
}