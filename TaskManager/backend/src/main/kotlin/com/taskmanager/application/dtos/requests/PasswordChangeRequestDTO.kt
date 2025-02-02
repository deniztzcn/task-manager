package com.taskmanager.application.dtos.requests
import io.konform.validation.Validation
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.notBlank
import kotlinx.serialization.Serializable

@Serializable
data class PasswordChangeRequestDTO(
    val oldPassword: String,
    val newPassword: String
)

val passwordChangeValidator = Validation {
    PasswordChangeRequestDTO::oldPassword {
        notBlank() hint "Password must not be blank"
    }
    PasswordChangeRequestDTO::newPassword {
        minLength(6) hint "Password must be at least 6 characters long"
        notBlank() hint "Password must not be blank"
    }
}