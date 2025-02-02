package com.taskmanager.infrastructure.controllers

import com.taskmanager.application.services.TeamService
import com.taskmanager.domain.services.ITeamService
import com.taskmanager.infrastructure.routes.taskRoutes
import com.taskmanager.infrastructure.routes.teamRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.teamController(teamService: ITeamService) {
    routing {
        teamRoutes(teamService)
    }
}