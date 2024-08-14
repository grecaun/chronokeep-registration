package com.chronokeep.registration.objects.responses

import com.chronokeep.registration.objects.database.DatabaseParticipant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
@SerialName("registration_participants")
class ParticipantsResponse (
    override val command: String = "registration_participants",
    val participants: ArrayList<DatabaseParticipant>,
    val distances: ArrayList<String>,
) : Response()