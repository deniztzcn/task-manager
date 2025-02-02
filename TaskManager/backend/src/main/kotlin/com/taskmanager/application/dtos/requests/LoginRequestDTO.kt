package com.taskmanager.application.dtos.requests

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDTO(
    val username: String, val password: String
)