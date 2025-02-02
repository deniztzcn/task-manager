package com.taskmanager.application.dtos.requests

import kotlinx.serialization.Serializable

@Serializable
data class  TeamRequestDTO(
    val title: String
)
