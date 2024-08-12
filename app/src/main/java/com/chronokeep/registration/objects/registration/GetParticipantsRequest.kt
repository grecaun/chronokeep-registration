package com.chronokeep.registration.objects.registration

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class GetParticipantsRequest (
    val command: String = RequestCommands.GET_PARTICIPANTS,
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}