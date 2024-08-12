package com.chronokeep.registration.objects.responses

import kotlinx.serialization.Serializable

@Serializable
sealed class Response{
    abstract val command: String
}
