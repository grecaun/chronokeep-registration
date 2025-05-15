package com.chronokeep.registration.network.chronokeep.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class GetParticipantsRequest (
    val slug: String,
    val year: String?,
    val limit: Int?,
    val page: Int?,
    val updated_after: Long?,
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}