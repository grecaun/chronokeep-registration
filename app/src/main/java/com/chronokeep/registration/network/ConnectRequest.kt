package com.chronokeep.registration.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class ConnectRequest (
    val command: String = "connect",
    val reads: Boolean = true,
    val sightings: Boolean = true,
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}