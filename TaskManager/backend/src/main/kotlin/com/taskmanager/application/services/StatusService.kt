package com.taskmanager.application.services

import com.taskmanager.application.dtos.PriorityDTO
import com.taskmanager.application.dtos.RoleDTO
import com.taskmanager.application.dtos.StatusDTO
import com.taskmanager.domain.entities.Priority
import com.taskmanager.domain.entities.Role
import com.taskmanager.domain.entities.Status
import com.taskmanager.domain.entities.toDTO
import com.taskmanager.domain.services.IPriorityService
import com.taskmanager.domain.services.IRoleService
import com.taskmanager.domain.services.IStatusService
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction

class StatusService : IStatusService {

    override fun getAll(): List<StatusDTO> {
        return transaction {
            Status.all().map { it.toDTO() }
        }
    }

    override fun getById(statusId: Int): StatusDTO? {
        return transaction {
            val status = Status.findById(statusId)
            status?.toDTO()
        }
    }

    override fun create(statusDTO: StatusDTO): StatusDTO {
        return transaction {
            val newStatus = Status.new {
                this.name = statusDTO.name
            }
            newStatus.toDTO()
        }
    }

    override fun update(statusId: Int, statusDTO: StatusDTO): StatusDTO {
        return transaction {
            val status = Status.findById(statusId)
                ?: throw NotFoundException("Priority with ID $statusId not found")

            status.apply {
                this.name = statusDTO.name
            }
            status.toDTO()
        }
    }

    override fun delete(statusId: Int) {
        transaction {
            val status = Status.findById(statusId)
                ?: throw NotFoundException("Priority with ID $statusId not found")
            status.delete()
        }
    }
}