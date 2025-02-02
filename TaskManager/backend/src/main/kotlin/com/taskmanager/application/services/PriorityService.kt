package com.taskmanager.application.services

import com.taskmanager.application.dtos.PriorityDTO
import com.taskmanager.domain.entities.Priority
import com.taskmanager.domain.entities.toDTO
import com.taskmanager.domain.services.IPriorityService
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction

class PriorityService : IPriorityService {

    override fun getAll(): List<PriorityDTO> {
        return transaction {
            Priority.all().map { it.toDTO() }
        }
    }

    override fun getById(priorityId: Int): PriorityDTO? {
        return transaction {
            val priority = Priority.findById(priorityId)
            priority?.toDTO()
        }
    }

    override fun create(priorityDTO: PriorityDTO): PriorityDTO {
        return transaction {
            val newPriority = Priority.new {
                this.name = priorityDTO.name
            }
            newPriority.toDTO()
        }
    }

    override fun update(priorityId: Int, priorityDTO: PriorityDTO): PriorityDTO {
        return transaction {
            val priority = Priority.findById(priorityId)
                ?: throw NotFoundException("Priority with ID $priorityId not found")

            priority.apply {
                this.name = priorityDTO.name
            }
            priority.toDTO()
        }
    }

    override fun delete(priorityId: Int) {
        transaction {
            val priority = Priority.findById(priorityId)
                ?: throw NotFoundException("Priority with ID $priorityId not found")
            priority.delete()
        }
    }
}