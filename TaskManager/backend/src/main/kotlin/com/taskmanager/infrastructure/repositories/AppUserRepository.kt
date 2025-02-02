package com.taskmanager.infrastructure.repositories

import com.taskmanager.application.dtos.requests.RegistrationRequestDTO
import com.taskmanager.domain.entities.AppUser
import com.taskmanager.domain.entities.AppUsers
import com.taskmanager.domain.enums.AuthRole
import com.taskmanager.domain.repositories.IAppUserRepository
import org.jetbrains.exposed.sql.transactions.transaction

class AppUserRepository : IAppUserRepository {

    override fun findById(appUserId: Int): AppUser? {
        return transaction {
            AppUser.findById(appUserId)
        }
    }

    override fun findByUsername(username: String): AppUser? {
        return transaction {
            AppUser.find { AppUsers.username eq username }.singleOrNull()
        }
    }

    override fun create(registrationRequestDTO: RegistrationRequestDTO): AppUser {
        return transaction {
            AppUser.new {
                this.username = registrationRequestDTO.username
                this.password = registrationRequestDTO.password
                this.role = AuthRole.USER.name
            }
        }
    }

    override fun update(appUser: AppUser): Boolean {
        return transaction {
            val foundUser = AppUser.findById(appUser.id.value)
            foundUser?.let {
                it.username = appUser.username
                it.password = appUser.password
                it.role = appUser.role
                true
            } ?: false
        }
    }

    override fun delete(appUserId: Int): Boolean {
        return transaction {
            val appUser = AppUser.findById(appUserId)
            appUser?.delete() != null
        }
    }
}
