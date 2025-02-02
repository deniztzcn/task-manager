package com.taskmanager.infrastructure.controllers

import ITaskService
import TaskService
import com.taskmanager.application.services.AuthService
import com.taskmanager.infrastructure.routes.taskRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getKoin

fun Application.taskController(taskService: ITaskService) {
    routing {
        taskRoutes(taskService)
    }
}