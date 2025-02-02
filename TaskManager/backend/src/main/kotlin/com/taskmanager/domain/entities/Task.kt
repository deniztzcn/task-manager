package com.taskmanager.domain.entities

import com.taskmanager.application.dtos.responses.TaskResponseDTO
import com.taskmanager.application.dtos.UserTeamDTO
import com.taskmanager.application.dtos.responses.UserResponseDTO
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object Tasks : IntIdTable("TASKS") {
    val assignedToUser = reference("ASSIGNED_TO_USER_ID", Users)
    val assignedToTeam = reference("ASSIGNED_TO_TEAM_ID", Teams).nullable()
    val assignedByUser = reference("ASSIGNED_BY_USER_ID", Users)
    val assignedByTeam = reference("ASSIGNED_BY_TEAM_ID", Teams).nullable()
    val priority = reference("PRIORITY_ID", Priorities)
    val status = reference("STATUS_ID", Statuses)
    val title = varchar("TITLE", 100)
    val description = text("DESCRIPTION")
    val assignedDate = date("ASSIGNED_DATE")
    val dueDate = date("DUE_DATE")
}

class Task(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Task>(Tasks)

    var assignedToUser by User referencedOn Tasks.assignedToUser
    var assignedToTeam by Team optionalReferencedOn Tasks.assignedToTeam
    var assignedByUser by User referencedOn Tasks.assignedByUser
    var assignedByTeam by Team optionalReferencedOn Tasks.assignedByTeam
    var priority by Priority referencedOn Tasks.priority
    var status by Status referencedOn Tasks.status
    var title by Tasks.title
    var description by Tasks.description
    var assignedDate by Tasks.assignedDate
    var dueDate by Tasks.dueDate
}

fun Task.toDTO(): TaskResponseDTO {
    return TaskResponseDTO(
        id = this.id.value,
        title = this.title,
        description = this.description,
        assignedTo = UserTeamDTO(
            user = UserResponseDTO(
                id = this.assignedToUser.id.value,
                username = this.assignedToUser.appUser.username,
                name = this.assignedToUser.name,
                surname = this.assignedToUser.surname,
                email = this.assignedToUser.email,
                authRole = this.assignedToUser.appUser.role
            ),
            teamId = this.assignedToTeam?.id?.value,
            teamName = this.assignedToTeam?.title
        ),
        assignedBy = UserTeamDTO(
            user = UserResponseDTO(
                id = this.assignedByUser.id.value,
                username = this.assignedByUser.appUser.username,
                name = this.assignedByUser.name,
                surname = this.assignedByUser.surname,
                email = this.assignedByUser.email,
                authRole = this.assignedByUser.appUser.role
            ),
            teamId = this.assignedByTeam?.id?.value,
            teamName = this.assignedByTeam?.title
        ),
        priority = this.priority.toDTO(),
        status = this.status.toDTO(),
        assignedDate = this.assignedDate.toKotlinLocalDate(),
        dueDate = this.dueDate.toKotlinLocalDate(),
    )
}