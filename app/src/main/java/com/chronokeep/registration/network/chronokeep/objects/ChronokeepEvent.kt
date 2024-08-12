package com.chronokeep.registration.network.chronokeep.objects

import kotlinx.serialization.Serializable

@Serializable
class ChronokeepEvent (
    val name: String,
    val slug: String,
    val website: String,
    val image: String,
    val contact_email: String,
    val access_restricted: Boolean,
    val type: String,
    val recent_time: String,
)