package com.taskmanager.domain.services

import com.taskmanager.application.dtos.PaginationResponse
import com.taskmanager.application.dtos.requests.TeamRequestDTO
import com.taskmanager.application.dtos.responses.TaskResponseDTO
import com.taskmanager.application.dtos.responses.TeamResponseDTO

interface ITeamService {
    fun createTeam(teamRequestDTO: TeamRequestDTO, userId: Int, appUserRole: String): TeamResponseDTO
    fun getTeams(userId: Int, appUserRole: String, page: Int, limit: Int, filter: String?): PaginationResponse<TeamResponseDTO>
    fun getTeamById(teamId: Int, userId: Int, appUserRole: String): TeamResponseDTO?

    fun updateTeam(teamId: Int, teamRequestDTO: TeamRequestDTO, userId: Int, appUserRole: String): TeamResponseDTO
    fun deleteTeam(teamId: Int, userId: Int, appUserRole: String)
    fun addMemberToTeam(
        teamId: Int,
        userId: Int,
        roleId: Int,
        addedById: Int,
        appUserRole: String
    )
    fun getTasksForTeam(teamId: Int, userId: Int, appUserRole: String, page: Int, limit: Int, filter: String?): PaginationResponse<TaskResponseDTO>
    fun removeMemberFromTeam(teamId: Int, userId: Int, requestorId: Int, appUserRole: String): TeamResponseDTO
    fun updateMemberRole(teamId: Int, userIdToUpdate: Int, newRoleId: Int, requestorId: Int, appUserRole: String)
    fun getTasksForUserInTeam(
        teamId: Int,
        userId: Int,
        appUserId: Int,
        appUserRole: String,
        page: Int,
        limit: Int
    ): PaginationResponse<TaskResponseDTO>
}