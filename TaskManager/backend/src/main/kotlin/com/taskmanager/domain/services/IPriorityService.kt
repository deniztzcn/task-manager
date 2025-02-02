package com.taskmanager.domain.services

import com.taskmanager.application.dtos.PriorityDTO

interface IPriorityService {
    fun getAll(): List<PriorityDTO>

    fun getById(priorityId: Int): PriorityDTO?

    fun create(priorityDTO: PriorityDTO): PriorityDTO

    fun update(priorityId: Int, priorityDTO: PriorityDTO): PriorityDTO

    fun delete(priorityId: Int)
}