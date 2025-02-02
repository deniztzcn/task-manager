package com.taskmanager.domain.repositories

import com.taskmanager.application.dtos.requests.RegistrationRequestDTO
import com.taskmanager.domain.entities.AppUser

interface IAppUserRepository {
    fun findById(appUserId: Int): AppUser?
    fun findByUsername(reg: String): AppUser?
    fun create(registrationRequestDTO: RegistrationRequestDTO): AppUser
    fun update(appUser: AppUser): Boolean
    fun delete(appUserId: Int): Boolean
}
