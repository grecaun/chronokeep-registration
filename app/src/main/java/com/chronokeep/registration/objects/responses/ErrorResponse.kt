package com.chronokeep.registration.objects.responses

import com.chronokeep.registration.objects.registration.RegistrationError
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("registration_error")
class ErrorResponse (
    override val command: String = "registration_error",
    val error: RegistrationError,
) : Response()