package com.taskmanager.infrastructure.routes

import com.taskmanager.application.dtos.requests.LoginRequestDTO
import com.taskmanager.application.dtos.requests.RegistrationRequestDTO
import com.taskmanager.application.exceptions.AuthenticationException
import com.taskmanager.application.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/login") {
            val loginRequestDTO = call.receive<LoginRequestDTO>()
            if (loginRequestDTO.username.isBlank() || loginRequestDTO.password.isBlank()) {
                throw BadRequestException("Username and password must not be blank")
            }

            val token = authService.login(loginRequestDTO)
            if (token != null) {
                call.respond(mapOf("token" to token))
            } else {
                throw AuthenticationException("Invalid credentials")
            }
        }
        post("/register") {
            val registrationRequestDTO = call.receive<RegistrationRequestDTO>()
            val newUser = authService.registerUser(registrationRequestDTO)
            call.respond(HttpStatusCode.Created, newUser)
        }

        authenticate("auth-jwt") {
            get("/validate") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.getClaim("username", String::class)
                val role = principal?.getClaim("role", String::class)
                val expiresAt = principal?.expiresAt

                if (username != null && role != null && (expiresAt == null || expiresAt.after(Date()))) {
                    call.respond(
                        mapOf(
                            "username" to username,
                            "role" to role,
                            "expiresAt" to expiresAt.toString()
                        )
                    )
                } else {
                    throw AuthenticationException("Invalid or expired token")
                }
            }
        }
    }
}