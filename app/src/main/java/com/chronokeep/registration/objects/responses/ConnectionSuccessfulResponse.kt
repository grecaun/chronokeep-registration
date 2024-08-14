package com.chronokeep.registration.objects.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
@SerialName("connection_successful")
class ConnectionSuccessfulResponse (
    override val command: String = "connection_successful",
    val name: String,
    val kind: String,
    val version: Int,
) : Response()