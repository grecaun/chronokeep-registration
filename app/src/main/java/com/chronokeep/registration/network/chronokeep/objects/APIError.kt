package com.chronokeep.registration.network.chronokeep.objects

import kotlinx.serialization.Serializable

@Serializable
class APIError (
    val message: String?
)