package com.chronokeep.registration.network.chronokeep.objects

import kotlinx.serialization.Serializable

@Serializable
class ChronokeepEventYear (
    val year: String,
    val date_time: String,
    val live: Boolean
)