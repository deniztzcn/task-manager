package com.taskmanager.infrastructure.controllers

import com.taskmanager.application.services.PriorityService
import com.taskmanager.domain.entities.Priority
import com.taskmanager.domain.services.IPriorityService
import com.taskmanager.domain.services.IRoleService
import com.taskmanager.infrastructure.routes.priorityRoutes
import com.taskmanager.infrastructure.routes.roleRoutes
import com.taskmanager.infrastructure.routes.taskRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.roleController(roleService: IRoleService ) {
    routing {
        roleRoutes(roleService)
    }
}