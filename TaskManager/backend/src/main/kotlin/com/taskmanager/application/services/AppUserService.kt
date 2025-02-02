package com.taskmanager.application.services

import com.taskmanager.application.dtos.requests.RegistrationRequestDTO
import com.taskmanager.domain.entities.AppUser
import com.taskmanager.domain.repositories.IAppUserRepository
import com.taskmanager.domain.services.IAppUserService

class AppUserService(
    private val appUserRepository: IAppUserRepository,
) : IAppUserService {

    override fun findById(appUserId: Int): AppUser? {
        return appUserRepository.findById(appUserId)
    }

    override fun findByUsername(username: String): AppUser? {
        return appUserRepository.findByUsername(username)
    }

    override fun createAppUser(registrationRequestDTO: RegistrationRequestDTO): AppUser {
        return appUserRepository.create(registrationRequestDTO)
    }

    override fun deleteAppUser(appUserId: Int): Boolean {
        return appUserRepository.delete(appUserId)
    }
}
