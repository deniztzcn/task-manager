package com.taskmanager.domain.entities

import com.taskmanager.application.dtos.responses.UserResponseDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable("USERS") {
    val name = varchar("NAME", 50)
    val surname = varchar("SURNAME", 50)
    val email = varchar("EMAIL", 100).uniqueIndex()
    val appUser = reference("APP_USER_ID", AppUsers)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var surname by Users.surname
    var email by Users.email
    var appUser by AppUser referencedOn Users.appUser
}

fun User.toDTO(): UserResponseDTO {
    return UserResponseDTO(
        id = this.id.value,
        username = this.appUser.username,
        name = this.name,
        surname = this.surname,
        email = this.email,
        authRole = this.appUser.role
    )
}
