package com.chronokeep.registration.network.chronokeep.objects

import kotlinx.serialization.Serializable

@Serializable
class ChronokeepAllEventYear (
    val name: String,
    val slug: String,
    val year: String,
    val date_time: String,
    val live: Boolean,
    val days_allowed: Int,
)