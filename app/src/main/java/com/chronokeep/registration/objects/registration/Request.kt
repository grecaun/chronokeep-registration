package com.chronokeep.registration.objects.registration

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Request (
    val command: String
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}