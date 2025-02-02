package com.taskmanager.application.services

import com.taskmanager.application.dtos.PriorityDTO
import com.taskmanager.application.dtos.RoleDTO
import com.taskmanager.domain.entities.Priority
import com.taskmanager.domain.entities.Role
import com.taskmanager.domain.entities.toDTO
import com.taskmanager.domain.services.IPriorityService
import com.taskmanager.domain.services.IRoleService
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction

class RoleService : IRoleService {

    override fun getAll(): List<RoleDTO> {
        return transaction {
            Role.all().map { it.toDTO() }
        }
    }

    override fun getById(roleId: Int): RoleDTO? {
        return transaction {
            val role = Role.findById(roleId)
            role?.toDTO()
        }
    }

    override fun create(roleDTO: RoleDTO): RoleDTO {
        return transaction {
            val newRole = Role.new {
                this.name = roleDTO.name
            }
            newRole.toDTO()
        }
    }

    override fun update(roleId: Int, roleDTO: RoleDTO): RoleDTO {
        return transaction {
            val role = Role.findById(roleId)
                ?: throw NotFoundException("Priority with ID $roleId not found")

            role.apply {
                this.name = roleDTO.name
            }
            role.toDTO()
        }
    }

    override fun delete(roleId: Int) {
        transaction {
            val role = Role.findById(roleId)
                ?: throw NotFoundException("Priority with ID $roleId not found")
            role.delete()
        }
    }
}