package com.taskmanager.infrastructure.routes

import com.taskmanager.application.dtos.AppUserRoleRequestDTO
import com.taskmanager.application.dtos.responses.UserResponseDTO
import com.taskmanager.application.dtos.requests.PasswordChangeRequestDTO
import com.taskmanager.application.dtos.requests.UserUpdateRequestDTO
import com.taskmanager.application.exceptions.AuthorizationException
import com.taskmanager.domain.enums.AuthRole
import com.taskmanager.domain.services.IUserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.userRoutes(userService: IUserService) {
    authenticate("auth-jwt") {
        route("/users") {
            get {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val filter = call.request.queryParameters["filter"]?.trim()

                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!
                val paginationResponse =
                    transaction { userService.getAllUsers(userId, appUserRole, page, limit, filter) }
                call.respond(paginationResponse)
            }
            get("/{id}") {
                val userId = call.parameters["id"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid user ID.")
                    return@get
                }
                val userResponse = transaction { userService.findById(userId) }
                call.respond(userResponse)
            }
            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val appUserRole = principal.getClaim("role", String::class)!!
                val userId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid user ID")

                if (appUserRole != "ADMIN") {
                    throw AuthorizationException("Only admins can delete user accounts")
                }
                transaction {
                    userService.deleteAccount(userId)
                }
                call.response.status(HttpStatusCode.NoContent)
            }
            route("/{id}/teams") {
                get {
                    val principal = call.principal<JWTPrincipal>()!!
                    val requestingUserId = principal.getClaim("userId", Int::class)!!
                    val userId = call.parameters["id"]?.toIntOrNull()

                    if (userId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid user ID.")
                        return@get
                    }

                    if (userId != requestingUserId && principal.getClaim(
                            "role",
                            String::class
                        ) != AuthRole.ADMIN.name
                    ) {
                        call.respond(HttpStatusCode.Forbidden, "You are not authorized to access this user's teams.")
                        return@get
                    }

                    val userTeams = transaction {
                        userService.getUserTeams(userId)
                    }

                    call.respond(userTeams)
                }
            }
            route("/me") {
                get {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getClaim("userId", Int::class)!!
                    val userResponse = transaction { userService.findById(userId) }
                    call.respond(
                        userResponse, typeInfo = typeInfo<UserResponseDTO>()
                    )
                }
                put {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getClaim("userId", Int::class)!!

                    val userUpdateRequest = call.receive<UserUpdateRequestDTO>()

                    val updatedUser = transaction { userService.updateUser(userId, userUpdateRequest) }
                    call.respond(
                        updatedUser, typeInfo = typeInfo<UserResponseDTO>()
                    )
                }
                put("/password") {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getClaim("userId", Int::class)!!

                    val passwordChangeRequest = call.receive<PasswordChangeRequestDTO>()

                    transaction {
                        userService.updatePassword(userId, passwordChangeRequest)
                    }

                    call.respondText("Password updated successfully", status = HttpStatusCode.OK)
                }

                delete {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getClaim("userId", Int::class)!!

                    transaction {
                        userService.deleteAccount(userId)
                    }

                    call.respondText("Account deleted successfully", status = HttpStatusCode.OK)
                }
            }
            route("/{id}/role") {
                put {
                    val principal = call.principal<JWTPrincipal>()!!
                    val adminRole = principal.getClaim("role", String::class)

                    val userId = call.parameters["id"]?.toIntOrNull()
                    if (userId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid user ID.")
                        return@put
                    }
                    if (adminRole != "ADMIN") {
                        call.respond(HttpStatusCode.Forbidden, "You are not authorized to update roles.")
                        return@put
                    }

                    val roleUpdateRequest = call.receive<AppUserRoleRequestDTO>()

                    transaction {
                        userService.updateUserRole(userId, roleUpdateRequest)
                    }

                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to "User role updated successfully")
                    )
                }
            }
        }
    }
}