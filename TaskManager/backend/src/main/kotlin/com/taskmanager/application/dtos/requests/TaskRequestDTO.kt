package com.taskmanager.application.dtos.requests

import io.konform.validation.Validation
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.minimum
import io.konform.validation.constraints.notBlank
import kotlinx.datetime.*
import kotlinx.serialization.Serializable

@Serializable
data class UserTeamId(
    val userId: Int,
    val teamId: Int? = null
)

@Serializable
data class TaskRequestDTO(
    val title: String,
    val description: String,
    val assignedTo: UserTeamId?,
    val priorityId: Int,
    val statusId: Int,
    val dueDate: LocalDate
)


val userTeamIdValidator = Validation {
    UserTeamId::userId {
        minimum(1) hint "User ID must be a positive integer"
    }
    UserTeamId::teamId ifPresent  {
        minimum(1) hint "Team ID must be a positive integer"
    }
}

val taskRequestValidator = Validation {
    TaskRequestDTO::title {
        minLength(3) hint "Title must be at least 3 characters long"
        notBlank() hint "Title must not be blank"
    }
    TaskRequestDTO::description {
        notBlank() hint "Description must not be blank"
    }
    TaskRequestDTO::assignedTo ifPresent {
        run(userTeamIdValidator)
    }
    TaskRequestDTO::priorityId {
        minimum(1) hint "Priority ID must be a positive integer"
    }
    TaskRequestDTO::statusId {
        minimum(1) hint "Status ID must be a positive integer"
    }
}