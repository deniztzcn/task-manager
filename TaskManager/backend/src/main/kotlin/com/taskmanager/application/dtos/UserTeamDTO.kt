package com.taskmanager.application.dtos

import com.taskmanager.application.dtos.responses.UserResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class UserTeamDTO(
    val user: UserResponseDTO,
    val teamId: Int? = null,
    val teamName: String? = null,
)