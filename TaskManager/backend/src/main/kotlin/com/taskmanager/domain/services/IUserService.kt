package com.taskmanager.domain.services

import com.taskmanager.application.dtos.AppUserRoleRequestDTO
import com.taskmanager.application.dtos.PaginationResponse
import com.taskmanager.application.dtos.requests.RegistrationRequestDTO
import com.taskmanager.application.dtos.responses.UserResponseDTO
import com.taskmanager.application.dtos.requests.PasswordChangeRequestDTO
import com.taskmanager.application.dtos.requests.UserUpdateRequestDTO
import com.taskmanager.application.dtos.responses.TeamResponseDTO
import com.taskmanager.domain.entities.AppUser

interface IUserService {
    fun getAllUsers(
        userId: Int,
        appUserRole: String,
        page: Int,
        limit: Int,
        filter: String?
    ): PaginationResponse<UserResponseDTO>
    fun findById(userId: Int): UserResponseDTO
    fun findByEmail(email: String): UserResponseDTO?
    fun findByUsername(username: String): UserResponseDTO
    fun createUser(request: RegistrationRequestDTO, appUser: AppUser): UserResponseDTO
    fun updateUser(userId: Int, userUpdateRequestDTO: UserUpdateRequestDTO): UserResponseDTO
    fun updatePassword(userId: Int, passwordChangeRequestDTO: PasswordChangeRequestDTO)
    fun deleteAccount(userId: Int)
    fun updateUserRole(userId: Int, roleUpdateRequest: AppUserRoleRequestDTO)
    fun getUserTeams(userId: Int): List<TeamResponseDTO>
}
