package com.taskmanager.infrastructure.plugins

import ITaskService
import com.taskmanager.domain.services.*
import com.taskmanager.infrastructure.controllers.*
import io.ktor.server.application.Application
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val taskService: ITaskService by inject()
    val teamService: ITeamService by inject()
    val userService: IUserService by inject()
    val priorityService: IPriorityService by inject()
    val roleService: IRoleService by inject()
    val statusService: IStatusService by inject()
    authController()
    taskController(taskService)
    teamController(teamService)
    userController(userService)
    priorityController(priorityService)
    roleController(roleService)
    statusController(statusService)
}