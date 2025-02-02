package com.taskmanager.application.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PaginationResponse<T>(
    val data: List<T>,
    val currentPage: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int
)
