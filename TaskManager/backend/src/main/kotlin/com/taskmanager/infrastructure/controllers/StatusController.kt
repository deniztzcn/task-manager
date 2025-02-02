package com.taskmanager.infrastructure.controllers

import com.taskmanager.application.services.StatusService
import com.taskmanager.domain.services.IRoleService
import com.taskmanager.domain.services.IStatusService
import com.taskmanager.infrastructure.routes.roleRoutes
import com.taskmanager.infrastructure.routes.statusRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.statusController(statusService: IStatusService) {
    routing {
        statusRoutes(statusService)
    }
}