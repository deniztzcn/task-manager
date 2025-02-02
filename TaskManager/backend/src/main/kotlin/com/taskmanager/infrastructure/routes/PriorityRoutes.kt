package com.taskmanager.infrastructure.routes

import com.taskmanager.application.dtos.PriorityDTO
import com.taskmanager.application.exceptions.AuthorizationException
import com.taskmanager.domain.services.IPriorityService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*

fun Route.priorityRoutes(priorityService: IPriorityService) {
    route("/priorities") {
        get {
            val priorities = priorityService.getAll()
            call.respond(
                priorities,
                typeInfo = typeInfo<List<PriorityDTO>>()
            )
        }
        get("/{id}") {
            val priorityId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid priority ID")
            val priority = priorityService.getById(priorityId) ?: throw NotFoundException("Priority not found")
            call.respond(
                priority,
                typeInfo = typeInfo<PriorityDTO>())
        }
    }
    authenticate("auth-jwt") {
        route("/priorities") {
            post {
                val principal = call.principal<JWTPrincipal>()!!
                val userRole = principal.getClaim("role", String::class)!!

                if (userRole != "ADMIN") {
                    throw AuthorizationException("Access denied: Only admins can access this resource.")
                }

                val priority = call.receive<PriorityDTO>()
                val createdPriority = priorityService.create(priority)
                call.respond(
                    createdPriority,
                    typeInfo = typeInfo<PriorityDTO>())
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userRole = principal.getClaim("role", String::class)!!

                if (userRole != "ADMIN") {
                    throw AuthorizationException("Access denied: Only admins can access this resource.")
                }

                val priorityId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid priority ID")
                val priority = call.receive<PriorityDTO>()
                val updatedPriority = priorityService.update(priorityId, priority)
                call.respond(
                    updatedPriority,
                    typeInfo = typeInfo<PriorityDTO>())
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userRole = principal.getClaim("role", String::class)!!

                if (userRole != "ADMIN") {
                    throw AuthorizationException("Access denied: Only admins can access this resource.")
                }

                val priorityId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid priority ID")
                priorityService.delete(priorityId)
                call.response.status(HttpStatusCode.NoContent)
            }
        }
    }
}