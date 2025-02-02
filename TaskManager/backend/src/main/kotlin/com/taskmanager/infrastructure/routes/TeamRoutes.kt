package com.taskmanager.infrastructure.routes

import com.taskmanager.application.dtos.PaginationResponse
import com.taskmanager.application.dtos.RoleDTO
import com.taskmanager.application.dtos.requests.MemberRequestDTO
import com.taskmanager.application.dtos.requests.TeamRequestDTO
import com.taskmanager.application.dtos.responses.TaskResponseDTO
import com.taskmanager.application.dtos.responses.TeamResponseDTO
import com.taskmanager.domain.services.ITeamService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.teamRoutes(teamService: ITeamService) {
    authenticate("auth-jwt") {
        route("/teams") {
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val filter = call.request.queryParameters["filter"]?.trim()


                val teams = transaction { teamService.getTeams(userId, appUserRole, page, limit, filter) }
                call.respond(
                    teams,
                    typeInfo = typeInfo<PaginationResponse<TeamResponseDTO>>())
            }

            get("/{teamId}/users/{userId}/tasks") {
                val principal = call.principal<JWTPrincipal>()!!
                val teamId = call.parameters["teamId"]?.toIntOrNull() ?: throw BadRequestException("Invalid team ID")
                val userId = call.parameters["userId"]?.toIntOrNull() ?: throw BadRequestException("Invalid user ID")
                println("teamId: $teamId, userId: $userId")

                val appUserId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

                val tasks = transaction {
                    teamService.getTasksForUserInTeam(teamId, userId, appUserId, appUserRole, page, limit)
                }

                call.respond(
                    tasks,
                    typeInfo = typeInfo<PaginationResponse<TaskResponseDTO>>()
                )
            }

            get("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val teamId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid team ID")

                val userId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!

                val teamResponseDTO = transaction {
                    teamService.getTeamById(teamId, userId, appUserRole)
                        ?: throw NotFoundException("Team with ID $teamId not found")
                }

                call.respond(
                    teamResponseDTO,
                    typeInfo = typeInfo<TeamResponseDTO>())
            }

            post {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!

                val teamRequest = call.receive<TeamRequestDTO>()
                val createdTeam = transaction { teamService.createTeam(teamRequest, userId, appUserRole) }
                call.respond(
                    createdTeam,
                    typeInfo = typeInfo<TeamResponseDTO>())
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!
                val teamId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid team ID")

                val teamRequest = call.receive<TeamRequestDTO>()
                val updatedTeam = transaction {
                    teamService.updateTeam(teamId, teamRequest, userId, appUserRole)
                }
                call.respond(
                    updatedTeam,
                        typeInfo = typeInfo<TeamResponseDTO>())
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val appUserRole = principal.getClaim("role", String::class)!!
                val userId = principal.getClaim("userId", Int::class)!!
                val teamId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid team ID")

                transaction {
                    teamService.deleteTeam(teamId, userId, appUserRole)
                }
                call.response.status(HttpStatusCode.NoContent)
            }

            post("/{id}/members") {
                val principal = call.principal<JWTPrincipal>()!!
                val teamId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid team ID")

                val memberRequest = call.receive<MemberRequestDTO>()
                val userId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!

                transaction {
                    teamService.addMemberToTeam(
                        teamId = teamId,
                        userId = memberRequest.userId,
                        roleId = memberRequest.roleId,
                        addedById = userId,
                        appUserRole = appUserRole
                    )
                }
                call.response.status(HttpStatusCode.OK)
            }

            delete("/{id}/members/{userId}") {
                val principal = call.principal<JWTPrincipal>()!!
                val teamId = call.parameters["id"]?.toIntOrNull()
                val userId = call.parameters["userId"]?.toIntOrNull()

                if (teamId == null || userId == null) {
                    throw BadRequestException("Invalid team or user ID")
                }

                val requestorId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!

                val updatedTeam = transaction {
                    teamService.removeMemberFromTeam(teamId, userId, requestorId, appUserRole)
                }
                call.respond(
                    updatedTeam,
                    typeInfo = typeInfo<TeamResponseDTO>())
            }

            get("/{id}/tasks") {
                val principal = call.principal<JWTPrincipal>()!!
                val teamId = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid team ID")

                val userId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val filter = call.request.queryParameters["filter"]?.trim()

                val tasks = transaction {
                    teamService.getTasksForTeam(teamId, userId, appUserRole, page, limit, filter)
                }

                call.respond(
                    tasks,
                    typeInfo = typeInfo<PaginationResponse<TaskResponseDTO>>()
                )
            }

            put("/{id}/members/{userId}") {
                println("Received PUT request for /teams/{id}/members/{userId}")
                val principal = call.principal<JWTPrincipal>()!!
                val teamId = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid team ID")

                val userIdToUpdate = call.parameters["userId"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid user ID")

                val roleRequest = call.receive<RoleDTO>()

                val requestorId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!

                transaction {
                    teamService.updateMemberRole(
                        teamId = teamId,
                        userIdToUpdate = userIdToUpdate,
                        newRoleId = roleRequest.id!!,
                        requestorId = requestorId,
                        appUserRole = appUserRole
                    )
                }
                call.response.status(HttpStatusCode.OK)
            }
        }
    }
}
