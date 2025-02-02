package com.taskmanager.application.dtos.responses

import com.taskmanager.application.dtos.PriorityDTO
import com.taskmanager.application.dtos.StatusDTO
import com.taskmanager.application.dtos.UserTeamDTO
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TaskResponseDTO(
    val id: Int,
    val title: String,
    val description: String,
    val assignedTo: UserTeamDTO?,
    val assignedBy: UserTeamDTO?,
    val priority: PriorityDTO,
    val status: StatusDTO,
    val assignedDate: LocalDate,
    val dueDate: LocalDate
)