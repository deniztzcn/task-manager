package com.taskmanager.domain.repositories

import com.taskmanager.application.dtos.requests.RegistrationRequestDTO
import com.taskmanager.domain.entities.AppUser
import com.taskmanager.domain.entities.User

interface IUserRepository {
    fun findById(userId: Int): User?
    fun findByEmail(email: String): User?
    fun findByUsername(username: String): User?
    fun create(request: RegistrationRequestDTO, appUser: AppUser): User
    fun update(user: User): Boolean
    fun delete(userId: Int): Boolean
}