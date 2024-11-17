package com.chronokeep.registration.util

import android.content.Context
import androidx.room.Room
import com.chronokeep.registration.interfaces.MenuWatcher
import com.chronokeep.registration.network.Connection
import com.chronokeep.registration.network.ConnectionHandler
import com.chronokeep.registration.objects.ServerInformation
import com.chronokeep.registration.objects.database.Database
import com.chronokeep.registration.objects.registration.RegistrationError
import java.net.InetAddress

object Globals {
    class RegistrationInfo {
        var error = RegistrationError.NONE
        val distances = HashSet<String>()
        val chronokeepInfoSet = HashSet<String>()
    }

    // Connection and thread for dealing with our open connection.
    private var con: Connection? = null
    private var conThread: Thread? = null

    fun startConnection(address: InetAddress, port: Int, handler: ConnectionHandler) {
        if (con != null && con!!.alive()) {
            con!!.stop()
            conThread = null
        }
        con = Connection(address, port, handler)
        conThread = Thread(con)
        conThread?.start()
    }

    fun stopConnection() {
        con?.stop()
        conThread = null
    }

    fun getConnection(): Connection? {
        return con
    }

    fun setConnectionHandler(handler: ConnectionHandler) {
        con?.setHandler(handler)
    }

    private var connected = false

    fun setConnected(newConnected: Boolean) {
        connected = newConnected
    }

    fun isConnected(): Boolean {
        return connected
    }

    var uniqueToken = ""

    private var menuWatcher: MenuWatcher? = null

    fun setMenuWatcher(newWatcher: MenuWatcher?) {
        menuWatcher = newWatcher
    }

    fun getMenuWatcher(): MenuWatcher? {
        return menuWatcher
    }

    // Server information
    private val serverInformation = ServerInformation("", "", 0)

    // Lists for storing information we've received.
    private val registration = RegistrationInfo()

    // Database var for registration information storage.
    private var database: Database? = null

    fun getDatabase(): Database? {
        return database
    }

    fun makeDatabase(context: Context) {
        if (database == null) {
            database = Room.databaseBuilder(context, Database::class.java, "chronokeep-control").allowMainThreadQueries().build()
        }
        val participants = database?.participantDao()?.getParticipants()
        synchronized(registration) {
            registration.distances.clear()
            if (participants != null) {
                for (part in participants) {
                    registration.distances.add(part.distance)
                }
            }
        }
    }

    // Registration information getters and setters
    fun setRegistrationDistances() {
        val participants = database?.participantDao()?.getParticipants()
        synchronized(registration) {
            registration.distances.clear()
            if (participants != null) {
                for (part in participants) {
                    registration.distances.add(part.distance)
                    registration.chronokeepInfoSet.add(part.chronokeep_info)
                }
            }
        }
    }

    fun getRegistrationDistances(): ArrayList<String> {
        val output: ArrayList<String>
        synchronized(registration) {
            output = ArrayList(registration.distances)
        }
        return output
    }

    fun getRegistrationYears(): ArrayList<String> {
        val output: ArrayList<String>
        synchronized(registration) {
            output = ArrayList(registration.chronokeepInfoSet)
        }
        return output
    }

    fun setRegistrationError(err: RegistrationError) {
        synchronized(registration) {
            registration.error = err
        }
    }

    fun getRegistrationError(): RegistrationError {
        val output: RegistrationError
        synchronized(registration) {
            output = registration.error
        }
        return output
    }

    // ServerInformation getter and setter
    fun getServerInfo(): ServerInformation {
        synchronized(serverInformation) {
            return ServerInformation(serverInformation.name, serverInformation.protocol, serverInformation.version)
        }
    }

    fun setServerInfo(name: String, protocol: String, version: Int) {
        synchronized(serverInformation) {
            serverInformation.name = name
            serverInformation.protocol = protocol
            serverInformation.version = version
        }
    }
}