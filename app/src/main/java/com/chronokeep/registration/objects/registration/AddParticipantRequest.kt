package com.chronokeep.registration.objects.registration

import com.chronokeep.registration.objects.database.DatabaseParticipant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class AddParticipantRequest (
    val command: String = RequestCommands.ADD_PARTICIPANT,
    val participant: DatabaseParticipant,
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}