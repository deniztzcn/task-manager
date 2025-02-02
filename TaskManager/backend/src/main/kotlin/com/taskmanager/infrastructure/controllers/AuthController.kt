package com.taskmanager.infrastructure.controllers

import com.taskmanager.application.services.AuthService
import com.taskmanager.infrastructure.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getKoin

fun Application.authController() {
    val authService: AuthService = getKoin().get()
    routing {
        authRoutes(authService)
    }
}