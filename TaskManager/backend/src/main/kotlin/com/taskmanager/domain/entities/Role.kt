package com.taskmanager.domain.entities

import com.taskmanager.application.dtos.RoleDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Roles : IntIdTable("ROLES") {
    val name = varchar("NAME", 50)
}

class Role(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Role>(Roles)

    var name by Roles.name
}

fun Role.toDTO(): RoleDTO {
    return RoleDTO(
        id = this.id.value,
        name = this.name
    )
}
