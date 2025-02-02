package com.taskmanager.application.dtos.requests

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.notBlank
import io.konform.validation.constraints.pattern
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequestDTO(
    val username: String,
    var password: String,
    val email: String,
    val name: String,
    val surname: String
)

val registrationValidator = Validation {
    RegistrationRequestDTO::username {
        minLength(3) hint "Username must be at least 3 characters long"
        notBlank() hint "Username must not be blank"
    }
    RegistrationRequestDTO::password {
        minLength(6) hint "Password must be at least 6 characters long"
        notBlank() hint "Password must not be blank"
    }
    RegistrationRequestDTO::email {
        notBlank() hint "Email must not be blank"
        pattern("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$") hint "Email must be a valid email address"
    }
    RegistrationRequestDTO::name  {
        minLength(2) hint "Name must be at least 2 characters long."
        maxLength(50) hint "Name must not exceed 50 characters."
    }

    RegistrationRequestDTO::surname {
        minLength(2) hint "Surname must be at least 2 characters long."
        maxLength(50) hint "Surname must not exceed 50 characters."
    }
}