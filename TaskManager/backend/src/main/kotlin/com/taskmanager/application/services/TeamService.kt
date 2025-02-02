package com.taskmanager.application.services

import com.taskmanager.application.dtos.PaginationResponse
import com.taskmanager.application.dtos.requests.TeamRequestDTO
import com.taskmanager.application.dtos.responses.TaskResponseDTO
import com.taskmanager.application.dtos.responses.TeamResponseDTO
import com.taskmanager.application.exceptions.AuthorizationException
import com.taskmanager.application.exceptions.ValidationException
import com.taskmanager.domain.entities.*
import com.taskmanager.domain.entities.UserTeams.role
import com.taskmanager.domain.enums.AuthRole
import com.taskmanager.domain.services.ITeamService
import io.ktor.server.plugins.*
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

class TeamService : ITeamService {
    override fun createTeam(teamRequestDTO: TeamRequestDTO, userId: Int, appUserRole: String): TeamResponseDTO {
        return transaction {
            if (appUserRole != AuthRole.ADMIN.name && appUserRole != AuthRole.MANAGER.name) {
                throw AuthorizationException("Only Admins or Managers can create a team")
            }

            val existingTeam = Team.find { Teams.title eq teamRequestDTO.title }.singleOrNull()
            if (existingTeam != null) {
                throw ValidationException(mapOf("title" to "A team with this title already exists"))
            }

            // Create the team
            val newTeam = Team.new {
                title = teamRequestDTO.title
                createdDate = LocalDate.now()
                createdBy = User.findById(userId)
                    ?: throw NotFoundException("User with ID $userId not found")
            }

            newTeam.toResponseDTO()
        }
    }

    override fun getTeams(
        userId: Int,
        appUserRole: String,
        page: Int,
        limit: Int,
        filter: String?
    ): PaginationResponse<TeamResponseDTO> {
        val offset = (page - 1) * limit

        return transaction {
            val queryBase = when (appUserRole) {
                AuthRole.ADMIN.name -> Teams.selectAll().where {
                    if (!filter.isNullOrBlank()) {
                        Teams.title.lowerCase() like "%$filter%"
                    } else {
                        Op.TRUE
                    }
                }

                AuthRole.MANAGER.name -> Teams.selectAll().where(
                    (Teams.createdBy eq userId) and if (!filter.isNullOrBlank()) {
                        Teams.title.lowerCase() like "%$filter%"
                    } else {
                        Op.TRUE
                    }
                )

                AuthRole.USER.name -> {
                    val userTeamIds = UserTeams
                        .selectAll().where(UserTeams.user eq userId)
                        .map { it[UserTeams.team] }
                    Teams.selectAll().where {
                        (Teams.id inList userTeamIds) and (if (!filter.isNullOrBlank()) {
                            Teams.title.lowerCase() like "%$filter%"
                        } else {
                            Op.TRUE
                        })
                    }
                }

                else -> throw AuthorizationException("Invalid role: $appUserRole")
            }
            val totalItems = queryBase.count()

            val query = queryBase.limit(limit).offset(offset.toLong())

            val teams = query.map { Team.wrapRow(it).toResponseDTO() }

            PaginationResponse(
                data = teams,
                currentPage = page,
                pageSize = limit,
                totalItems = totalItems,
                totalPages = (totalItems / limit).toInt() + if (totalItems % limit > 0) 1 else 0
            )
        }
    }

    override fun getTeamById(teamId: Int, userId: Int, appUserRole: String): TeamResponseDTO? {
        return transaction {
            val team = Team.findById(teamId) ?: return@transaction null

            when (appUserRole) {
                AuthRole.ADMIN.name -> {
                    return@transaction team.toResponseDTO()
                }

                AuthRole.MANAGER.name -> {
                    val isCreator = team.createdBy.id.value == userId
                    if (!isCreator) {
                        throw AuthorizationException("Managers can only access teams they created")
                    }
                    return@transaction team.toResponseDTO()
                }

                AuthRole.USER.name -> {
                    val teamLeaderRoleId = Roles
                        .selectAll()
                        .where(Roles.name eq "Team Leader")
                        .singleOrNull()?.get(Roles.id)
                        ?: throw AuthorizationException("Role 'Team Leader' not found")

                    val isTeamLeader = UserTeams
                        .selectAll()
                        .where(
                            (UserTeams.user eq userId) and
                                    (UserTeams.team eq team.id.value) and
                                    (UserTeams.role eq teamLeaderRoleId)
                        ).count() > 0

                    if (!isTeamLeader) {
                        throw AuthorizationException("You are not authorized to access this team")
                    }
                    return@transaction team.toResponseDTO()
                }

                else -> throw AuthorizationException("You are not authorized to access this team")
            }
        }
    }

    override fun updateTeam(
        teamId: Int,
        teamRequest: TeamRequestDTO,
        userId: Int,
        appUserRole: String
    ): TeamResponseDTO {
        return transaction {
            val team = Team.findById(teamId)
                ?: throw NotFoundException("Team with ID $teamId not found")

            when (appUserRole) {
                AuthRole.ADMIN.name -> {
                }

                AuthRole.MANAGER.name -> {
                    if (team.createdBy.id.value != userId) {
                        throw AuthorizationException("Managers can only update teams they created")
                    }
                }

                else -> throw AuthorizationException("Only Admins and Managers can update teams")
            }

            if (teamRequest.title.isBlank()) {
                throw ValidationException(mapOf("title" to "Team title cannot be empty"))
            }

            team.apply {
                title = teamRequest.title
            }

            team.toResponseDTO()
        }
    }

    override fun deleteTeam(teamId: Int, userId: Int, appUserRole: String) {
        transaction {
            val team = Team.findById(teamId) ?: throw NotFoundException("Team with ID $teamId not found")

            when (appUserRole) {
                AuthRole.ADMIN.name -> {
                }

                AuthRole.MANAGER.name -> {
                    if (team.createdBy.id.value != userId) {
                        throw AuthorizationException("Managers can only delete teams they created")
                    }
                }

                else -> {
                    throw AuthorizationException("You are not authorized to delete teams")
                }
            }

            team.delete()
        }
    }

    override fun addMemberToTeam(
        teamId: Int,
        userId: Int,
        roleId: Int,
        addedById: Int,
        appUserRole: String
    ) {
        return transaction {
            val team = Team.findById(teamId) ?: throw NotFoundException("Team with ID $teamId not found")

            when (appUserRole) {
                AuthRole.ADMIN.name -> {}
                AuthRole.MANAGER.name -> {
                    if (team.createdBy.id.value != addedById) {
                        throw AuthorizationException("Managers can only add members to teams they created")
                    }
                }

                AuthRole.USER.name -> {
                    val isTeamLeader = UserTeams.selectAll().where {
                        (UserTeams.team eq teamId) and (UserTeams.user eq addedById) and
                                (UserTeams.role eq Roles.selectAll().where { Roles.name eq "Team Leader" }
                                    .single()[Roles.id])
                    }.count() > 0

                    if (!isTeamLeader) {
                        throw AuthorizationException("Only Team Leaders can add members to their team")
                    }
                }

                else -> throw AuthorizationException("Unauthorized to add members to this team")
            }

            val userToAdd = User.findById(userId) ?: throw NotFoundException("User with ID $userId not found")

            val roleToAdd = Role.findById(roleId) ?: throw NotFoundException("Role with ID $roleId not found")

            val isAlreadyMember = UserTeams.selectAll().where {
                (UserTeams.user eq userId) and (UserTeams.team eq teamId)
            }.count() > 0

            if (isAlreadyMember) {
                throw ValidationException(mapOf("userId" to "User is already a member of the team"))
            }

            UserTeams.insert {
                it[user] = userId
                it[UserTeams.team] = teamId
                it[role] = roleId
                it[joinedDate] = LocalDate.now()
            }
        }
    }

    override fun removeMemberFromTeam(
        teamId: Int,
        userId: Int,
        requestorId: Int,
        appUserRole: String
    ): TeamResponseDTO {
        return transaction {
            val team = Team.findById(teamId)
                ?: throw NotFoundException("Team with ID $teamId not found")

            when (appUserRole) {
                AuthRole.ADMIN.name -> {
                }

                AuthRole.MANAGER.name -> {
                    if (team.createdBy.id.value != requestorId) {
                        throw AuthorizationException("Managers can only remove members from teams they created")
                    }
                }

                AuthRole.USER.name -> {
                    val isTeamLeader = UserTeams.selectAll()
                        .where {
                            (UserTeams.team eq teamId) and
                                    (UserTeams.user eq requestorId) and
                                    (UserTeams.role eq Roles.selectAll()
                                        .where { Roles.name eq "Team Leader" }.single()[Roles.id])
                        }
                        .count() > 0

                    if (!isTeamLeader) {
                        throw AuthorizationException("Only Team Leaders can remove members from their team")
                    }
                }

                else -> throw AuthorizationException("Unauthorized to remove members")
            }

            val isMember = UserTeams.selectAll()
                .where { (UserTeams.user eq userId) and (UserTeams.team eq teamId) }
                .count() > 0

            if (!isMember) {
                throw NotFoundException("User with ID $userId is not a member of the team with ID $teamId")
            }

            UserTeams.deleteWhere { (user eq userId) and (UserTeams.team eq teamId) }

            team.toResponseDTO()
        }
    }

    override fun getTasksForTeam(
        teamId: Int,
        userId: Int,
        appUserRole: String,
        page: Int,
        limit: Int,
        filter: String?
    ): PaginationResponse<TaskResponseDTO> {
        val offset = (page - 1) * limit
        return transaction {
            val isAuthorized = when (appUserRole) {
                AuthRole.ADMIN.name -> true
                AuthRole.MANAGER.name -> Teams.selectAll()
                    .where { (Teams.id eq teamId) and (Teams.createdBy eq userId) }.count() > 0

                AuthRole.USER.name -> UserTeams.selectAll()
                    .where { (UserTeams.team eq teamId) and (UserTeams.user eq userId) }.count() > 0

                else -> false
            }

            if (!isAuthorized) {
                throw AuthorizationException("Unauthorized to view tasks for this team")
            }

            val tasks = Tasks.selectAll().where { Tasks.assignedToTeam eq teamId and if (!filter.isNullOrBlank()) {
                Tasks.title.lowerCase() like "%$filter%"
            } else {
                Op.TRUE
            }}
                .limit(limit).offset(start = offset.toLong())
                .map { Task.wrapRow(it) }

            val totalItems = Tasks.selectAll().where { Tasks.assignedToTeam eq teamId }.count()

            PaginationResponse(
                data = tasks.map { it.toDTO() },
                currentPage = page,
                pageSize = limit,
                totalItems = totalItems,
                totalPages = (totalItems / limit).toInt() + if (totalItems % limit > 0) 1 else 0
            )
        }
    }

    override fun updateMemberRole(
        teamId: Int,
        userIdToUpdate: Int,
        newRoleId: Int,
        requestorId: Int,
        appUserRole: String
    ) {
        return transaction {
            val isAuthorized = when (appUserRole) {
                AuthRole.ADMIN.name -> true
                AuthRole.MANAGER.name -> Teams.selectAll()
                    .where { (Teams.id eq teamId) and (Teams.createdBy eq requestorId) }.count() > 0

                AuthRole.USER.name -> {
                    val teamLeaderRoleId = Roles.selectAll().where { Roles.name eq "Team Leader" }
                        .singleOrNull()?.get(Roles.id)
                        ?: throw IllegalArgumentException("Team Leader role not found")

                    UserTeams.selectAll().where {
                        (UserTeams.team eq teamId) and
                                (UserTeams.user eq requestorId) and
                                (UserTeams.role eq teamLeaderRoleId)
                    }.count() > 0
                }

                else -> false
            }

            if (!isAuthorized) {
                throw AuthorizationException("Unauthorized to update roles in this team")
            }

            val userTeam = UserTeams.selectAll().where {
                (UserTeams.user eq userIdToUpdate) and (UserTeams.team eq teamId)
            }.singleOrNull() ?: throw NotFoundException("User is not a member of the team")

            val newRole = Roles.selectAll().where { Roles.id eq newRoleId }.singleOrNull()
                ?: throw NotFoundException("Role with ID $newRoleId not found")

            UserTeams.update(
                {
                    (UserTeams.user eq userIdToUpdate) and (UserTeams.team eq teamId)
                }) {
                it[role] = newRole[Roles.id]
            }
        }
    }

    override fun getTasksForUserInTeam(
        teamId: Int,
        userId: Int,
        appUserId: Int,
        appUserRole: String,
        page: Int,
        limit: Int
    ): PaginationResponse<TaskResponseDTO> {
        val offset = (page - 1) * limit

        return transaction {
            val isAuthorized = when (appUserRole) {
                AuthRole.ADMIN.name -> true
                AuthRole.MANAGER.name -> Teams.selectAll()
                    .where { (Teams.id eq teamId) and (Teams.createdBy eq appUserId) }.count() > 0

                AuthRole.USER.name -> UserTeams.selectAll()
                    .where { (UserTeams.team eq teamId) and (UserTeams.user eq appUserId) }.count() > 0
                else -> false
            }

            if (!isAuthorized) {
                throw AuthorizationException("Unauthorized to view tasks for this user in the team")
            }

            val isUserInTeam = UserTeams.selectAll()
                .where { (UserTeams.team eq teamId) and (UserTeams.user eq userId) }
                .count() > 0

            if (!isUserInTeam) {
                throw NotFoundException("User with ID $userId is not a member of the team with ID $teamId")
            }

            val taskQuery = Tasks.selectAll()
                .where { (Tasks.assignedToUser eq userId) and (Tasks.assignedToTeam eq teamId) }
                .limit(limit).offset(start = offset.toLong())

            val tasks = taskQuery.map { Task.wrapRow(it) }

            val totalItems = Tasks.selectAll()
                .where { (Tasks.assignedToUser eq userId) and (Tasks.assignedToTeam eq teamId) }
                .count()

            PaginationResponse(
                data = tasks.map { it.toDTO() },
                currentPage = page,
                pageSize = limit,
                totalItems = totalItems,
                totalPages = (totalItems / limit).toInt() + if (totalItems % limit > 0) 1 else 0
            )
        }
    }

}