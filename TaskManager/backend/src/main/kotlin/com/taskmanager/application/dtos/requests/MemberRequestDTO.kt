package com.taskmanager.application.dtos.requests

import kotlinx.serialization.Serializable

@Serializable
data class MemberRequestDTO(
    val userId: Int,
    val roleId: Int
)
