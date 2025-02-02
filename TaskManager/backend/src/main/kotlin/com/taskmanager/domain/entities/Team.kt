package com.taskmanager.domain.entities

import com.taskmanager.application.dtos.responses.MemberDTO
import com.taskmanager.application.dtos.responses.TeamResponseDTO
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.selectAll

object Teams : IntIdTable("TEAMS") {
    val title = varchar("TITLE", 100)
    val createdDate = date("CREATED_DATE")
    var createdBy = reference("CREATED_BY", Users)
}

class Team(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Team>(Teams)

    var title by Teams.title
    var createdDate by Teams.createdDate
    var createdBy by User referencedOn Teams.createdBy
}

fun Team.toResponseDTO(): TeamResponseDTO {
    return TeamResponseDTO(
        id = id.value,
        title = title,
        createdDate = createdDate.toKotlinLocalDate(),
        createdBy = createdBy.toDTO(),
        members = UserTeams
            .selectAll().where(UserTeams.team eq this.id)
            .map { userTeam ->
                MemberDTO(
                    user = User.findById(userTeam[UserTeams.user])!!.toDTO(),
                    role = Role.findById(userTeam[UserTeams.role])!!.toDTO(),
                    joinedDate = userTeam[UserTeams.joinedDate].toString()
                )
            }
    )
}