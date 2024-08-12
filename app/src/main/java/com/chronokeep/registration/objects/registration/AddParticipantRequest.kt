package com.chronokeep.registration.objects.registration

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class AddParticipantRequest (
    val command: String = RequestCommands.ADD_PARTICIPANT,
    val participant: Participant,
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}