package com.taskmanager.application.dtos.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDTO(
    val id: Int,
    val username: String,
    val name: String,
    val surname: String,
    val email: String,
    val authRole: String
)