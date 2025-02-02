package com.taskmanager.application.dtos

import kotlinx.serialization.Serializable

@Serializable
data class StatusDTO(
    val id: Int? = null,
    val name: String
)