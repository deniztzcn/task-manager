package com.taskmanager.infrastructure.routes

import com.taskmanager.application.dtos.RoleDTO
import com.taskmanager.application.exceptions.AuthorizationException
import com.taskmanager.domain.services.IRoleService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*

fun Route.roleRoutes(roleService: IRoleService) {
    route("/roles") {
        get {
            val roles = roleService.getAll()
            call.respond(
                roles,
                typeInfo = typeInfo<List<RoleDTO>>()
            )
        }
        get("/{id}") {
            val roleId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid role ID")
            val role = roleService.getById(roleId) ?: throw NotFoundException("Role not found")

            call.respond(
                role,
                typeInfo = typeInfo<RoleDTO>())
        }
    }
    authenticate("auth-jwt") {
        route("/roles") {
            post {
                val principal = call.principal<JWTPrincipal>()!!
                val userRole = principal.getClaim("role", String::class)!!

                if (userRole != "ADMIN") {
                    throw AuthorizationException("Access denied: Only admins can access this resource.")
                }

                val role = call.receive<RoleDTO>()
                val createdRole = roleService.create(role)
                call.respond(
                    createdRole,
                    typeInfo = typeInfo<RoleDTO>())
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userRole = principal.getClaim("role", String::class)!!

                if (userRole != "ADMIN") {
                    throw AuthorizationException("Access denied: Only admins can access this resource.")
                }

                val roleId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid role ID")
                val role = call.receive<RoleDTO>()
                val updatedRole = roleService.update(roleId, role)
                call.respond(
                    updatedRole,
                    typeInfo = typeInfo<RoleDTO>())
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userRole = principal.getClaim("role", String::class)!!

                if (userRole != "ADMIN") {
                    throw AuthorizationException("Access denied: Only admins can access this resource.")
                }

                val roleId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid role ID")
                roleService.delete(roleId)
                call.response.status(HttpStatusCode.NoContent)
            }
        }
    }
}