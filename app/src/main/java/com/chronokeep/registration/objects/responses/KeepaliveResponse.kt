package com.chronokeep.registration.objects.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("keepalive")
class KeepaliveResponse (
    override val command: String = "keepalive",
) : Response()