package com.chronokeep.registration.objects.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("registration_connection_successful")
class ConnectionSuccessfulResponse (
    override val command: String = "registration_connection_successful",
    val name: String,
    val kind: String,
    val version: Int,
) : Response()