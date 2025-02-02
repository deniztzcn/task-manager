package com.taskmanager.domain.entities

import com.taskmanager.application.dtos.PriorityDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Priorities : IntIdTable("PRIORITY") {
    val name = varchar("NAME", 50)
}

class Priority(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Priority>(Priorities)

    var name by Priorities.name
}

fun Priority.toDTO(): PriorityDTO {
    return PriorityDTO(
        id = this.id.value,
        name = this.name
    )
}

