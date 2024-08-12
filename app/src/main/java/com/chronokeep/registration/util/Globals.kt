package com.chronokeep.registration.util

import android.content.Context
import androidx.room.Room
import com.chronokeep.registration.network.Connection
import com.chronokeep.registration.objects.ServerInformation
import com.chronokeep.registration.objects.database.Database
import com.chronokeep.registration.objects.registration.RegistrationError
import com.chronokeep.registration.objects.registration.Participant

object Globals {
    class RegistrationInfo {
        var error = RegistrationError.NONE
        val participants = ArrayList<Participant>()
        val distances = ArrayList<String>()
    }

    // Connection and thread for dealing with our open connection.
    var con: Connection? = null
    var conThread: Thread? = null

    var connected = false

    var uniqueToken = ""

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
    }

    // Registration information getters and setters
    fun setRegistrationParticipants(parts: ArrayList<Participant>) {
        synchronized(registration.participants) {
            registration.participants.clear()
            registration.participants.addAll(parts)
        }
    }

    fun getRegistrationParticipants(): ArrayList<Participant> {
        val output: ArrayList<Participant>
        synchronized(registration.participants) {
            output = ArrayList(registration.participants)
        }
        return output
    }

    fun setRegistrationDistances(distances: ArrayList<String>) {
        synchronized(registration.distances) {
            registration.distances.clear()
            registration.distances.addAll(distances)
        }
    }

    fun getRegistrationDistances(): ArrayList<String> {
        val output: ArrayList<String>
        synchronized(registration.distances) {
            output = ArrayList(registration.distances)
        }
        return output
    }

    fun setRegistrationError(err: RegistrationError) {
        synchronized(registration.error) {
            registration.error = err
        }
    }

    fun getRegistrationError(): RegistrationError {
        val output: RegistrationError
        synchronized(registration.error) {
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