package com.taskmanager.infrastructure.routes

import com.taskmanager.application.dtos.StatusDTO
import com.taskmanager.application.exceptions.AuthorizationException
import com.taskmanager.domain.services.IStatusService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*

fun Route.statusRoutes(statusService: IStatusService) {
    route("/statuses") {
        get {
            val statuses = statusService.getAll()
            call.respond(
                statuses,
                typeInfo = typeInfo<List<StatusDTO>>()
            )
        }
        get("/{id}") {
            val statusId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid status ID")
            val status = statusService.getById(statusId) ?: throw NotFoundException("Status not found")

            call.respond(
                status,
                typeInfo = typeInfo<StatusDTO>())
        }
    }
    authenticate("auth-jwt") {
        route("/statuses") {
            post {
                val principal = call.principal<JWTPrincipal>()!!
                val userRole = principal.getClaim("role", String::class)!!

                if (userRole != "ADMIN") {
                    throw AuthorizationException("Access denied: Only admins can access this resource.")
                }

                val status = call.receive<StatusDTO>()
                val createdStatus = statusService.create(status)
                call.respond(
                    createdStatus,
                    typeInfo = typeInfo<StatusDTO>())
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userRole = principal.getClaim("role", String::class)!!

                if (userRole != "ADMIN") {
                    throw AuthorizationException("Access denied: Only admins can access this resource.")
                }

                val statusId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid status ID")
                val status = call.receive<StatusDTO>()
                val updatedStatus = statusService.update(statusId, status)
                call.respond(
                    updatedStatus,
                    typeInfo = typeInfo<StatusDTO>())
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userRole = principal.getClaim("role", String::class)!!

                if (userRole != "ADMIN") {
                    throw AuthorizationException("Access denied: Only admins can access this resource.")
                }

                val statusId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid status ID")
                statusService.delete(statusId)
                call.response.status(HttpStatusCode.NoContent)
            }
        }
    }
}