package com.chronokeep.registration.network.chronokeep.objects

import kotlinx.serialization.Serializable

@Serializable
class ChronokeepAccount (
    val name: String,
    val email: String,
    val type: String,
    val locked: Boolean
)