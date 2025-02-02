package com.taskmanager.application.services

import com.taskmanager.application.dtos.AppUserRoleRequestDTO
import com.taskmanager.application.dtos.PaginationResponse
import com.taskmanager.application.dtos.responses.UserResponseDTO
import com.taskmanager.application.dtos.requests.*
import com.taskmanager.application.dtos.responses.TeamResponseDTO
import com.taskmanager.application.exceptions.AdminDeletionException
import com.taskmanager.application.exceptions.AuthenticationException
import com.taskmanager.application.exceptions.AuthorizationException
import com.taskmanager.application.exceptions.ValidationException
import com.taskmanager.domain.entities.*
import com.taskmanager.domain.entities.Teams.createdBy
import com.taskmanager.domain.enums.AuthRole
import com.taskmanager.domain.repositories.IUserRepository
import com.taskmanager.domain.services.IUserService
import io.konform.validation.Invalid
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserService(
    private val userRepository: IUserRepository,
    private val securityService: SecurityService,
) : IUserService {

    override fun getUserTeams(userId: Int): List<TeamResponseDTO> {
        return transaction {
            val teamIds = UserTeams
                .selectAll().where { UserTeams.user eq userId }
                .map { it[UserTeams.team] }

            Teams.selectAll().where { Teams.id inList teamIds }
                .map { Team.wrapRow(it).toResponseDTO() }
        }
    }

    override fun getAllUsers(
        userId: Int,
        appUserRole: String,
        page: Int,
        limit: Int,
        filter: String?
    ): PaginationResponse<UserResponseDTO> {
        if (appUserRole != AuthRole.ADMIN.name) {
            throw AuthorizationException("Access denied: Only admins can fetch all users.")
        }

        val offset = (page - 1) * limit

        return transaction {
            val queryBase = Users.selectAll().where {
                if(!filter.isNullOrBlank()){
                    Teams.title.lowerCase() like "%$filter%"
                } else{
                    Op.TRUE
                }
            }

            val totalItems = queryBase.count()
            val query = queryBase.limit(limit).offset(offset.toLong())
            val users = query.map { User.wrapRow(it).toDTO()}

            PaginationResponse(
                data = users,
                currentPage = page,
                pageSize = limit,
                totalItems = totalItems,
                totalPages = (totalItems / limit).toInt() + if (totalItems % limit > 0) 1 else 0
            )
        }
    }

    override fun findById(userId: Int): UserResponseDTO {
        return userRepository.findById(userId)?.toDTO()
            ?: throw NotFoundException("User with ID $userId not found")
    }

    override fun findByEmail(email: String): UserResponseDTO? {
        return userRepository.findByEmail(email)?.toDTO()
    }

    override fun findByUsername(username: String): UserResponseDTO {
        return userRepository.findByUsername(username)?.toDTO()
        ?: throw NotFoundException("User with Username $username not found")
    }

    override fun createUser(request: RegistrationRequestDTO, appUser: AppUser): UserResponseDTO {
        return userRepository.create(request, appUser).toDTO()
    }

    override fun updateUser(userId: Int, userUpdateRequestDTO: UserUpdateRequestDTO): UserResponseDTO {
        val validationResult = userUpdateRequestValidator.validate(userUpdateRequestDTO)
        if (validationResult is Invalid) {
            val errors = validationResult.errors
                .groupBy(
                    { it.dataPath.removePrefix(".") },
                    { it.message },
                )
                .mapValues { (_, messages) -> messages.joinToString(", ") }
            throw ValidationException(errors)
        }

        return transaction {
            val user = User.findById(userId) ?: throw NotFoundException("User not found")

            user.apply {
                userUpdateRequestDTO.name?.let { name = it }
                userUpdateRequestDTO.surname?.let { surname = it }
                userUpdateRequestDTO.email?.let { email = it }
            }
            user.toDTO()
        }
    }
    override fun updatePassword(userId: Int, passwordChangeRequestDTO: PasswordChangeRequestDTO) {
        val validationResult = passwordChangeValidator.validate(passwordChangeRequestDTO)
        if (validationResult is Invalid) {
            val errors = validationResult.errors
                .groupBy(
                    { it.dataPath.removePrefix(".") },
                    { it.message },
                )
                .mapValues { (_, messages) -> messages.joinToString(", ") }
            throw ValidationException(errors)
        }

        transaction {
            val user = User.findById(userId) ?: throw NotFoundException("User not found")

            if (!securityService.validatePassword(passwordChangeRequestDTO.oldPassword, user.appUser.password)) {
                throw AuthenticationException("Old password is incorrect")
            }

            user.appUser.password = securityService.hashPassword(passwordChangeRequestDTO.newPassword)
        }
    }

    override fun deleteAccount(userId: Int) {
        transaction {
            val user = User.findById(userId)
                ?: throw NotFoundException("User not found")

            if (user.appUser.role == "ADMIN") {
                throw AdminDeletionException("Admin accounts cannot be deleted")
            }

            val adminUser = AppUser.find { AppUsers.role eq AuthRole.ADMIN.name }.singleOrNull()

            Teams.update({ createdBy eq userId }) {
                it[createdBy] = adminUser!!.id.value
            }

            Tasks.deleteWhere { assignedToUser eq userId }
            Tasks.update({Tasks.assignedByUser eq userId}) {
                it[assignedByUser] = adminUser!!.id.value
            }

            UserTeams.deleteWhere { this.user eq userId }
            AppUsers.deleteWhere { AppUsers.id eq user.appUser.id.value }
            user.delete()
        }
    }

    override fun updateUserRole(userId: Int, roleUpdateRequest: AppUserRoleRequestDTO) {
        val user = AppUser.findById(userId) ?: throw NotFoundException("User not found")
        user.apply { role = roleUpdateRequest.role }
    }
}
