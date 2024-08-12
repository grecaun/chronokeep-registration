package com.chronokeep.registration.network.chronokeep.requests

import com.chronokeep.registration.network.chronokeep.objects.ChronokeepParticipant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class UpdateParticipantsRequest (
    val slug: String,
    val year: String,
    val participants: List<ChronokeepParticipant>
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}