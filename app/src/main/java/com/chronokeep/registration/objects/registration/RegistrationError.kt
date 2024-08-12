package com.chronokeep.registration.objects.registration

import kotlinx.serialization.Serializable

@Serializable
enum class RegistrationError {
    NONE,
    UNKNOWN_MESSAGE,
    PARTICIPANT_NOT_FOUND,
    DISTANCE_NOT_FOUND
}