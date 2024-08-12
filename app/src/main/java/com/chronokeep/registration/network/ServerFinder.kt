package com.chronokeep.registration.network

import android.os.Handler
import android.os.Message
import android.util.Log
import com.chronokeep.registration.list_items.Server
import com.chronokeep.registration.util.Constants
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket

class ServerFinder(mHandler: Handler): Runnable {
    companion object {
        @JvmStatic
        val msg_server_list_avail = 1
        @JvmStatic
        val msg_server_finder_done = 2
        @JvmStatic
        val ready: HashMap<String, Server> = HashMap()
        @JvmStatic
        fun getAvailableServerItems(): ArrayList<Server> {
            val output = ArrayList<Server>()
            synchronized(ready) {
                output.addAll(ready.values)
            }
            return output
        }
    }

    private var stopped = false

    private val tag = "Chrono.ServerFind"
    private var mainHandler: Handler = mHandler

    private val timeout = 2000

    fun setHandler(handle: Handler) {
        this.mainHandler = handle
    }

    private fun sendMessage(msg: Message) {
        if (!stopped) {
            mainHandler.sendMessage(msg)
        }
    }

    override fun run() {
        this.stopped = false
        ready.clear()
        try {
            val group = InetAddress.getByName(Constants.zero_conf_multicast_address)
            val multicastSocket = MulticastSocket()
            multicastSocket.joinGroup(group)
            for (count in 0..2) {
                Log.d(tag, "Broadcasting message.")
                val sendData = "[DISCOVER_CHRONO_SERVER_REQUEST]".toByteArray()
                try {
                    val pack = DatagramPacket(sendData, sendData.size, group, Constants.zero_conf_port)
                    multicastSocket.send(pack)
                } catch (ignored: Exception) {}
                val recvBuf = ByteArray(1024)
                val recvpack = DatagramPacket(recvBuf, recvBuf.size)
                val curtime = System.currentTimeMillis()
                Log.d(tag, "Current time value is $curtime")
                while (curtime + timeout > System.currentTimeMillis()) {
                    val curTimeout = timeout - (System.currentTimeMillis() - curtime)
                    Log.d(tag, "Waiting for message. Timeout value is $curTimeout")
                    multicastSocket.soTimeout = curTimeout.toInt()
                    try {
                        multicastSocket.receive(recvpack)
                        Log.d(tag, "Received a message from the server. Address is ${recvpack.address} length is ${recvpack.length} message is ${recvpack.data.decodeToString(0, recvpack.length)}")
                        val information = recvpack.data.decodeToString(0, recvpack.length)
                            .replace('[',' ')
                            .replace(']',' ')
                            .trim()
                            .split('|')
                        val potential = Server(information[0],
                            information[1],
                            recvpack.address.hostAddress,
                            information[2].toInt()
                        )
                        synchronized(ready) {
                            if (!ready.containsKey(potential.name)) {
                                Log.d(tag,"Adding to server list.")
                                ready[potential.name] = potential
                            }
                        }
                        val msg = Message()
                        msg.what = msg_server_list_avail
                        sendMessage(msg)
                    } catch (e: Exception) {
                        Log.d(tag, "Exception occurred: $e")
                    }
                }
            }
            multicastSocket.leaveGroup(group)
        } catch (_: Exception) {}
        val msg = Message()
        msg.what = msg_server_finder_done
        sendMessage(msg)
        stopped = true
    }

    fun stop() {
        stopped = true
    }

    fun stopped(): Boolean {
        return stopped
    }
}