package com.taskmanager.infrastructure.repositories

import com.taskmanager.application.dtos.requests.RegistrationRequestDTO
import com.taskmanager.domain.entities.AppUser
import com.taskmanager.domain.entities.AppUsers
import com.taskmanager.domain.entities.User
import com.taskmanager.domain.entities.Users
import com.taskmanager.domain.repositories.IUserRepository
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository : IUserRepository {
    override fun findById(userId: Int): User? {
        return transaction {
            User.findById(userId)
        }
    }

    override fun findByEmail(email: String): User? {
        return transaction {
            User.find { Users.email eq email }.singleOrNull()
        }
    }

    override fun findByUsername(username: String): User? {
        return transaction {
            val appUser = AppUser.find { AppUsers.username eq username }.singleOrNull()
            appUser?.let { appUserEntity ->
                User.find { Users.appUser eq appUserEntity.id }.singleOrNull()
            }
        }
    }


    override fun create(request: RegistrationRequestDTO, appUser: AppUser): User {
        return transaction {
            User.new {
                name = request.name
                surname = request.surname
                email = request.email
                this.appUser = appUser
            }
        }
    }

    override fun update(user: User): Boolean {
        return transaction {
            val existingUser = User.findById(user.id.value)
            if (existingUser != null) {
                existingUser.name = user.name
                existingUser.surname = user.surname
                existingUser.email = user.email
                existingUser.appUser = user.appUser
                true
            } else {
                false
            }
        }
    }


    override fun delete(userId: Int): Boolean {
        return transaction {
            val user = User.findById(userId)
            if (user != null) {
                user.delete()
                true
            } else {
                false
            }
        }
    }

}
