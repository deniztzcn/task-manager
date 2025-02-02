package com.taskmanager.application.dtos.responses

import com.taskmanager.application.dtos.RoleDTO
import kotlinx.serialization.Serializable

@Serializable
data class MemberDTO(
    val user: UserResponseDTO,
    val role: RoleDTO,
    val joinedDate: String
)
