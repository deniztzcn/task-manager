package com.taskmanager.infrastructure.controllers

import com.taskmanager.application.services.UserService
import com.taskmanager.domain.services.ITeamService
import com.taskmanager.domain.services.IUserService
import com.taskmanager.infrastructure.routes.teamRoutes
import com.taskmanager.infrastructure.routes.userRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.userController(userService: IUserService) {
    routing {
        userRoutes(userService)
    }
}