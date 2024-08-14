package com.chronokeep.registration.objects.registration

import com.chronokeep.registration.objects.database.DatabaseParticipant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class AddUpdateParticipantsRequest (
    val command: String = RequestCommands.ADD_UPDATE_PARTICIPANTS,
    val participants: List<DatabaseParticipant>,
) {
    fun encode(): String {
        val format = Json { encodeDefaults = true }
        return format.encodeToString(this)
    }
}