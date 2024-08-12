package com.chronokeep.registration.objects.registration

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class UpdateParticipantRequest (
    val command: String = RequestCommands.UPDATE_PARTICIPANT,
    val participant: Participant,
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}