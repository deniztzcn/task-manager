package com.taskmanager.domain.entities

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object AppUsers : IntIdTable("APP_USER") {
    val username = varchar("USERNAME", 50).uniqueIndex()
    val password = varchar("PASSWORD", 64)
    val role = varchar("ROLE", 20)
}

class AppUser(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AppUser>(AppUsers)

    var username by AppUsers.username
    var password by AppUsers.password
    var role by AppUsers.role
}