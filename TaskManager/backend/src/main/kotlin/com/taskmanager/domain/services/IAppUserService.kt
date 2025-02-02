package com.taskmanager.domain.services

import com.taskmanager.application.dtos.requests.RegistrationRequestDTO
import com.taskmanager.domain.entities.AppUser

interface IAppUserService {
    fun findById(appUserId: Int): AppUser?
    fun findByUsername(username: String): AppUser?
    fun createAppUser(registrationRequestDTO: RegistrationRequestDTO): AppUser
    fun deleteAppUser(appUserId: Int): Boolean
}
