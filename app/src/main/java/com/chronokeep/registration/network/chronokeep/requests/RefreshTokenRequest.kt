package com.chronokeep.registration.network.chronokeep.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class RefreshTokenRequest (
    val refresh_token: String
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}