package com.chronokeep.registration.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("unused")
@Serializable
class ConnectRequest (
    val command: String = "connect",
    val reads: Boolean = false,
    val sightings: Boolean = false,
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}