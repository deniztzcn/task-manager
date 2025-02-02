package com.taskmanager.application.dtos.responses

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TeamResponseDTO(
    val id: Int,
    val title: String,
    val createdDate: LocalDate,
    val createdBy: UserResponseDTO,
    val members: List<MemberDTO>
)