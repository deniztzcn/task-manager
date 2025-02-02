package com.taskmanager.infrastructure.routes

import ITaskService
import com.taskmanager.application.dtos.PaginationResponse
import com.taskmanager.application.dtos.requests.TaskRequestDTO
import com.taskmanager.application.dtos.responses.TaskResponseDTO
import com.taskmanager.domain.entities.toDTO
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.log

fun Route.taskRoutes(taskService: ITaskService) {
    authenticate("auth-jwt") {
        route("/tasks") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val appUserRole = principal?.getClaim("role", String::class)
                val userId = principal?.getClaim("userId", Int::class)

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val filter = call.request.queryParameters["filter"]?.trim()


                if (page <= 0 || limit <= 0) {
                    throw BadRequestException("Page and limit must be positive integers")
                }

                val paginationResponse = transaction {
                    taskService.getTasksForRole(appUserRole, userId, page, limit, filter)
                }

                call.respond(
                    paginationResponse,
                    typeInfo = typeInfo<PaginationResponse<TaskResponseDTO>>()
                )
            }
            post {
                val principal = call.principal<JWTPrincipal>()
                val taskRequestDTO = call.receive<TaskRequestDTO>()
                println("Deserialized payload: $taskRequestDTO")
                val task = transaction { taskService.assignTask(taskRequestDTO, principal!!) }
                call.respond(
                    task,
                    typeInfo = typeInfo<TaskResponseDTO>()
                )
            }
            get("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", Int::class)!!
                val taskId = call.parameters["id"]?.toIntOrNull()
                val appUserRole = principal.getClaim("role", String::class)!!

                if (taskId == null) {
                    throw BadRequestException("Invalid task ID")
                }

                val taskDto = transaction {
                    val task = taskService.getTaskById(appUserRole,taskId, userId)
                    task?.toDTO()
                }
                call.respond(taskDto, typeInfo = typeInfo<TaskResponseDTO>())
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!
                val taskId = call.parameters["id"]?.toIntOrNull()

                if (taskId == null) {
                    throw BadRequestException("Invalid task ID")
                }

                val taskRequestDTO = call.receive<TaskRequestDTO>()
                val taskDto = transaction {
                    val task = taskService.updateTask(taskId, taskRequestDTO, userId, appUserRole)
                    task?.toDTO() ?: throw NotFoundException("Task with ID $taskId not found")
                }
                call.respond(taskDto, typeInfo = typeInfo<TaskResponseDTO>())
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", Int::class)!!
                val appUserRole = principal.getClaim("role", String::class)!!
                val taskId = call.parameters["id"]?.toIntOrNull()

                if (taskId == null) {
                    throw BadRequestException("Invalid task ID")
                }

                transaction {
                    taskService.deleteTask(taskId, userId, appUserRole)
                }

                call.response.status(HttpStatusCode.NoContent)
            }

        }
    }
}