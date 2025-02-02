package com.taskmanager.domain.entities

import com.taskmanager.application.dtos.StatusDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Statuses : IntIdTable("TASK_STATUS") {
    val name = varchar("NAME", 50)
}

class Status(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Status>(Statuses)

    var name by Statuses.name
}

fun Status.toDTO(): StatusDTO {
    return StatusDTO(
        id = this.id.value,
        name = this.name
    )
}
