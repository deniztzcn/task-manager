package com.taskmanager.application.dtos.requests

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.pattern
import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequestDTO(
    val name: String? = null,
    val surname: String? = null,
    val email: String? = null
)

val userUpdateRequestValidator = Validation {
    UserUpdateRequestDTO::name ifPresent {
        minLength(2) hint "Name must be at least 2 characters long."
        maxLength(50) hint "Name must not exceed 50 characters."
    }

    UserUpdateRequestDTO::surname ifPresent {
        minLength(2) hint "Surname must be at least 2 characters long."
        maxLength(50) hint "Surname must not exceed 50 characters."
    }

    UserUpdateRequestDTO::email ifPresent {
        pattern("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$") hint "Must be a valid email address."
    }
}
