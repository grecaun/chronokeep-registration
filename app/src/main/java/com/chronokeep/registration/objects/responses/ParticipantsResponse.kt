package com.chronokeep.registration.objects.responses

import com.chronokeep.registration.objects.registration.Participant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("registration_participants")
class ParticipantsResponse (
    override val command: String = "registration_participants",
    val participants: ArrayList<Participant>,
    val distances: ArrayList<String>,
) : Response()