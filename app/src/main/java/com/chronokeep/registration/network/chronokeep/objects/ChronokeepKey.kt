package com.chronokeep.registration.network.chronokeep.objects

import kotlinx.serialization.Serializable

@Serializable
class ChronokeepKey (
    val name: String,
    val value: String,
    val type: String,
    val allowed_hosts: String,
    val valid_until: String
)