package com.taskmanager.domain.services

import com.taskmanager.application.dtos.PriorityDTO
import com.taskmanager.application.dtos.RoleDTO
import com.taskmanager.application.dtos.StatusDTO

interface IStatusService {
    fun getAll(): List<StatusDTO>

    fun getById(statusId: Int): StatusDTO?

    fun create(statusDTO: StatusDTO): StatusDTO

    fun update(statusId: Int, statusDTO: StatusDTO): StatusDTO

    fun delete(statusId: Int)
}