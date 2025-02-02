package com.taskmanager.domain.entities

import com.taskmanager.application.dtos.UserTeamDTO
import com.taskmanager.application.dtos.responses.UserResponseDTO
import com.taskmanager.domain.entities.UserTeams.user
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object UserTeams : Table("USER_TEAM") {
    val user = reference("USER_ID", Users)
    val team = reference("TEAM_ID", Teams)
    val role = reference("ROLE_ID", Roles)
    val joinedDate = date("JOINED_DATE")

    override val primaryKey = PrimaryKey(user, team, name = "PK_UserTeam")
}

//fun ResultRow.toUserTeamDTO(): UserTeamDTO {
//    return UserTeamDTO(
//        user = UserResponseDTO(
//            id = this[Users.id].value,
//            username = this[AppUsers.username],
//            name = this[Users.name],
//            surname = this[Users.surname],
//            email = this[Users.email],
//        ),
//        teamId = this[UserTeams.team].value,
//        teamName = this[Teams.title]
//    )
//}