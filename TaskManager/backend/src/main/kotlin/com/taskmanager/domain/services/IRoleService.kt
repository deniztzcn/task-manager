package com.taskmanager.domain.services

import com.taskmanager.application.dtos.PriorityDTO
import com.taskmanager.application.dtos.RoleDTO

interface IRoleService {
    fun getAll(): List<RoleDTO>

    fun getById(roleId: Int): RoleDTO?

    fun create(roleDTO: RoleDTO): RoleDTO

    fun update(roleId: Int, roleDTO: RoleDTO): RoleDTO

    fun delete(roleId: Int)
}