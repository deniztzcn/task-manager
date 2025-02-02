package com.taskmanager.application.dtos

import kotlinx.serialization.Serializable

@Serializable
data class AppUserRoleRequestDTO(
    val role: String,
)
