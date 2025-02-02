package com.taskmanager.application.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PriorityDTO(
    val id: Int? = null,
    val name: String,
)
