import com.taskmanager.application.dtos.PaginationResponse
import com.taskmanager.application.dtos.requests.TaskRequestDTO
import com.taskmanager.application.dtos.requests.taskRequestValidator
import com.taskmanager.application.dtos.responses.TaskResponseDTO
import com.taskmanager.application.exceptions.*
import com.taskmanager.domain.entities.*
import com.taskmanager.domain.enums.AuthRole
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

class TaskService(
//    private val taskRepository: ITaskRepository,
//    private val userRepository: IUserRepository,
//    private val teamRepository: ITeamRepository
) : ITaskService {

//    override fun getTasksForRole(
//        appUserRole: String?,
//        userId: Int?,
//        page: Int,
//        limit: Int
//    ): PaginationResponse<TaskResponseDTO> {
//        val offset = (page - 1) * limit
//        return transaction {
//            val tasks = when (appUserRole) {
//                AuthRole.ADMIN.name -> taskRepository.getAllTasks(offset, limit)
//                AuthRole.MANAGER.name -> taskRepository.getTasksForManagedTeams(userId, offset, limit)
//                AuthRole.USER.name -> taskRepository.getTasksAssignedToUser(userId, offset, limit)
//                else -> throw IllegalArgumentException("Invalid role: $appUserRole")
//            }
//
//            val totalItems = taskRepository.countAllTasks()
//            PaginationResponse(
//                data = tasks.map { it.toDTO() },
//                currentPage = page,
//                pageSize = limit,
//                totalItems = totalItems,
//                totalPages = (totalItems / limit).toInt() + if (totalItems % limit > 0) 1 else 0
//            )
//        }
//    }

    override fun getTasksForRole(appUserRole: String?, userId: Int?, page: Int, limit: Int, filter: String?): PaginationResponse<TaskResponseDTO> {
        val offset = (page - 1) * limit
        val tasks = transaction {
            when (appUserRole) {
                AuthRole.ADMIN.name -> getAllTasks(offset, limit, filter)
                AuthRole.MANAGER.name -> getTasksForManagedTeams(userId, offset, limit, filter)
                AuthRole.USER.name -> getTasksAssignedToUser(userId, offset, limit, filter)
                else -> throw IllegalArgumentException("Invalid role: $appUserRole")
            }
        }

        val totalItems = transaction {
            when (appUserRole) {
                AuthRole.ADMIN.name -> countAllTasks(filter)
                AuthRole.MANAGER.name -> countTasksForManager(userId, filter)
                AuthRole.USER.name -> countTasksAssignedToUser(userId, filter)
                else -> 0
            }
        }
        return PaginationResponse(
            data = tasks.map { it.toDTO() },
            currentPage = page,
            pageSize = limit,
            totalItems = totalItems,
            totalPages = (totalItems / limit).toInt() + if (totalItems % limit > 0) 1 else 0
        )
    }

//    override fun assignTask(taskRequest: TaskRequestDTO, principal: JWTPrincipal): TaskResponseDTO {
//        val userId = principal.getClaim("userId", Int::class)
//            ?: throw IllegalArgumentException("UserId claim is missing from the token")
//        val userRole = principal.getClaim("role", String::class)
//            ?: throw IllegalArgumentException("Role claim is missing from the token")
//
//        val validationResult = taskRequestValidator.validate(taskRequest)
//        if (validationResult.errors.isNotEmpty()) {
//            throw ValidationException(
//                validationResult.errors.associate { it.dataPath to it.message }
//            )
//        }
//
//        return transaction {
//            val teamId = taskRequest.assignedTo.teamId
//            val assignedUserId = taskRequest.assignedTo.userId
//
//            val userToAssign = userRepository.findById(assignedUserId)
//                ?: throw NotFoundException("User with ID $assignedUserId not found")
//
//            val team = teamRepository.findById(teamId)
//                ?: throw NotFoundException("Team with ID $teamId not found")
//
//            val isUserMemberOfTeam = teamRepository.isMemberOfTeam(assignedUserId, teamId)
//            if (!isUserMemberOfTeam) {
//                throw ValidationException(mapOf("userId" to "User is not a member of the specified team"))
//            }
//
//            when (userRole) {
//                AuthRole.MANAGER.name -> {
//                    val isManagerOfTeam = taskRepository.isManagerOfTeam(userId, teamId)
//                    if (!isManagerOfTeam) {
//                        throw AuthorizationException("Managers can only assign tasks to teams they created")
//                    }
//                }
//
//                AuthRole.ADMIN.name -> {}
//                else -> {
//                    val isTeamLeader = taskRepository.isTeamLeader(userId, teamId)
//                    if (!isTeamLeader) {
//                        throw AuthorizationException("Only Team Leaders can assign tasks in their teams")
//                    }
//                }
//            }
//
//            taskRepository.createTask(
//                assignedToUser = assignedUserId,
//                assignedToTeam = teamId,
//                assignedByUser = userId!!,
//                priority = taskRequest.priorityId,
//                status = taskRequest.statusId,
//                title = taskRequest.title,
//                description = taskRequest.description,
//                dueDate = taskRequest.dueDate.toJavaLocalDate()
//            ).toDTO()
//        }
//    }

    override fun assignTask(taskRequest: TaskRequestDTO, principal: JWTPrincipal): TaskResponseDTO {
        val userId = principal.getClaim("userId", Int::class)
            ?: throw AuthenticationException("User ID missing in token")

        val userRole = principal.getClaim("role", String::class)
            ?: throw AuthorizationException("Role missing in token")

        val validationResult = taskRequestValidator.validate(taskRequest)
        if (validationResult.errors.isNotEmpty()) {
            throw ValidationException(
                validationResult.errors.associate { it.dataPath to it.message }
            )
        }

        return transaction {
            val teamId = taskRequest.assignedTo?.teamId
            val assignedUserId = taskRequest.assignedTo?.userId

            if (teamId != null) {
                val team = Team.findById(teamId)
                    ?: throw NotFoundException("Team with ID $teamId not found")

                val isUserMemberOfTeam = UserTeams.selectAll()
                    .where { (UserTeams.user eq assignedUserId) and (UserTeams.team eq teamId) }
                    .count() > 0

                if (!isUserMemberOfTeam) {
                    throw ValidationException(mapOf("userId" to "User is not a member of the specified team"))
                }
            }

            when(userRole) {
                AuthRole.MANAGER.name -> {
                    val isManagerOfTeam = Teams.selectAll(
                    ).where(
                        (Teams.id eq teamId) and (Teams.createdBy eq userId)
                    ).count() > 0
                    if (!isManagerOfTeam) {
                        throw AuthorizationException("Managers can only assign tasks to teams they created")
                    }
                }
                AuthRole.ADMIN.name -> {
                }

                else -> {
                    val teamLeaderRoleId = Roles
                        .selectAll().where(Roles.name eq "Team Leader")
                        .singleOrNull()?.get(Roles.id)
                        ?: throw IllegalArgumentException("Role 'Team Leader' not found")

                    val isTeamLeader = UserTeams.selectAll(
                    ).where(
                        (UserTeams.user eq userId) and
                                (UserTeams.team eq teamId) and
                                (UserTeams.role eq teamLeaderRoleId)
                    ).count() > 0

                    if (!isTeamLeader) {
                        throw AuthorizationException("Only Team Leaders can assign tasks in their teams")
                    }
                }
            }

            val newTask = Tasks.insertAndGetId {
                it[assignedToUser] = assignedUserId!!
                it[assignedToTeam] = teamId
                it[assignedByUser] = userId
                it[assignedByTeam] = teamId
                it[priority] = taskRequest.priorityId
                it[status] = taskRequest.statusId
                it[title] = taskRequest.title
                it[description] = taskRequest.description
                it[assignedDate] = LocalDate.now()
                it[dueDate] = taskRequest.dueDate.toJavaLocalDate()
            }.value

            Task.findById(newTask)?.toDTO()!!
        }
    }

//    override fun getTaskById(appUserRole: String, taskId: Int, userId: Int): Task {
//        return transaction {
//            val task = taskRepository.getTaskById(taskId) ?: throw NotFoundException("Task with ID $taskId not found")
//
//            when (appUserRole) {
//                AuthRole.ADMIN.name -> task
//                AuthRole.MANAGER.name -> {
//                    val isManager = taskRepository.isManagerForTask(userId, task.assignedToTeam.id.value)
//                    if (!isManager && task.assignedByUser.id.value != userId) {
//                        throw AuthorizationException("Managers can only access tasks they created or manage")
//                    }
//                    task
//                }
//                AuthRole.USER.name -> {
//                    if (task.assignedToUser.id.value != userId && task.assignedByUser.id.value != userId) {
//                        throw AuthorizationException("Users can only access tasks assigned to or created by them")
//                    }
//                    task
//                }
//                else -> throw AuthorizationException("Unauthorized access")
//            }
//        }
//    }

    override fun getTaskById(appUserRole: String, taskId: Int, userId: Int): Task? {
        return transaction {
            val task = Task.findById(taskId) ?: return@transaction null
            when (appUserRole) {
                AuthRole.ADMIN.name -> return@transaction task
                AuthRole.MANAGER.name -> {
                    val isManager = Teams.selectAll()
                        .where { (Teams.id eq task.assignedToTeam?.id?.value) and (Teams.createdBy eq userId) }.count() > 0

                    if (!isManager && task.assignedByUser.id.value != userId) {
                        throw AuthorizationException("Managers can only access tasks they created or manage")
                    }
                    return@transaction task
                }
                AuthRole.USER.name -> {
                    if (task.assignedToUser.id.value != userId && task.assignedByUser.id.value != userId) {
                        throw AuthorizationException("Users can only access tasks assigned to or created by them")
                    }
                    return@transaction task
                }
                else -> throw AuthorizationException("Unauthorized access")
            }
        }
    }

//    override fun updateTask(taskId: Int, taskRequestDTO: TaskRequestDTO, userId: Int, appUserRole: String): Task? {
//        return transaction {
//            val task = taskRepository.getTaskById(taskId) ?: throw NotFoundException("Task with ID $taskId not found")
//
//            when (appUserRole) {
//                AuthRole.ADMIN.name -> {}
//                AuthRole.MANAGER.name -> {
//                    val isManager = taskRepository.isManagerForTask(userId, task.assignedToTeam.id.value)
//                    if (!isManager) {
//                        throw AuthorizationException("Managers can only update tasks in teams they created")
//                    }
//                }
//                AuthRole.USER.name -> {
//                    val isTeamLeader = taskRepository.isTeamLeader(userId, task.assignedToTeam.id.value)
//                    if (!isTeamLeader) {
//                        throw AuthorizationException("Only Team Leaders can update tasks in their teams")
//                    }
//                }
//                else -> throw AuthorizationException("Invalid role for task update")
//            }
//
//            val validationResult = taskRequestValidator.validate(taskRequestDTO)
//            if (validationResult.errors.isNotEmpty()) {
//                throw ValidationException(
//                    validationResult.errors.associate { it.dataPath to it.message }
//                )
//            }
//
//            taskRepository.updateTask(
//                task = task,
//                title = taskRequestDTO.title,
//                description = taskRequestDTO.description,
//                priorityId = taskRequestDTO.priorityId,
//                statusId = taskRequestDTO.statusId,
//                dueDate = taskRequestDTO.dueDate.toJavaLocalDate(),
//                assignedToUser = if (appUserRole == AuthRole.ADMIN.name) taskRequestDTO.assignedTo.userId else null,
//                assignedToTeam = if (appUserRole == AuthRole.ADMIN.name) taskRequestDTO.assignedTo.teamId else null
//            )
//        }
//    }

    override fun updateTask(taskId: Int, taskRequestDTO: TaskRequestDTO, userId: Int, appUserRole: String): Task?   {
        return transaction {
            val task = Task.findById(taskId) ?: return@transaction null

            when (appUserRole) {
                AuthRole.ADMIN.name -> {
                }
                AuthRole.MANAGER.name -> {
                    val isManagerOfTeam = Teams.selectAll()
                        .where { (Teams.id eq task.assignedToTeam?.id?.value) and (Teams.createdBy eq userId) }.count() > 0
                    if (!isManagerOfTeam) {
                        throw AuthorizationException("Managers can only update tasks in teams they created")
                    }
                }
                AuthRole.USER.name -> {
                    val teamLeaderRoleId = Roles
                        .selectAll().where { Roles.name eq "Team Leader" }
                        .singleOrNull()?.get(Roles.id)
                        ?: throw TeamRoleNotFoundException("Role 'Team Leader' not found")

                    val isTeamLeader = UserTeams.selectAll().where {
                        (UserTeams.user eq userId) and
                                (UserTeams.team eq task.assignedToTeam?.id?.value) and
                                (UserTeams.role eq teamLeaderRoleId)
                    }.count() > 0

                    if (!isTeamLeader) {
                        throw AuthorizationException("Only Team Leaders can update tasks in their teams")
                    }
                }
                else -> {
                    throw AuthorizationException("Invalid role for task update")
                }
            }

            val validationResult = taskRequestValidator.validate(taskRequestDTO)
            if (validationResult.errors.isNotEmpty()) {
                throw ValidationException(
                    validationResult.errors.associate { it.dataPath to it.message }
                )
            }

            task.apply {
                title = taskRequestDTO.title
                description = taskRequestDTO.description
                priority = Priority.findById(taskRequestDTO.priorityId)
                    ?: throw NotFoundException("Priority not found")
                status = Status.findById(taskRequestDTO.statusId)
                    ?: throw NotFoundException("Status not found")
                dueDate = taskRequestDTO.dueDate.toJavaLocalDate()

                if (appUserRole == AuthRole.ADMIN.name) {
                    assignedToUser = taskRequestDTO.assignedTo?.userId?.let { User.findById(it) }
                        ?: throw NotFoundException("Assigned user not found")
                    assignedToTeam = taskRequestDTO.assignedTo.teamId?.let { Team.findById(it) }
                        ?: if (taskRequestDTO.assignedTo.teamId == null) null else throw NotFoundException("Assigned team not found")
                    assignedByTeam = null
                }
            }
            return@transaction task
        }
    }

//    override fun deleteTask(taskId: Int, userId: Int, appUserRole: String) {
//        transaction {
//            val task = taskRepository.getTaskById(taskId) ?: throw NotFoundException("Task with ID $taskId not found")
//
//            when (appUserRole) {
//                AuthRole.ADMIN.name -> {}
//                AuthRole.MANAGER.name -> {
//                    val isManager = taskRepository.isManagerForTask(userId, task.assignedToTeam.id.value)
//                    if (!isManager) {
//                        throw AuthorizationException("Managers can only delete tasks in teams they created")
//                    }
//                }
//                AuthRole.USER.name -> {
//                    val isTeamLeader = taskRepository.isTeamLeader(userId, task.assignedToTeam.id.value)
//                    if (!isTeamLeader) {
//                        throw AuthorizationException("Only Team Leaders can delete tasks they created in their team")
//                    }
//                }
//                else -> throw AuthorizationException("You are not authorized to delete tasks")
//            }
//
//            taskRepository.deleteTask(task)
//        }
//    }

    override fun deleteTask(taskId: Int, userId: Int, appUserRole: String) {
        transaction {
            val task = Task.findById(taskId) ?: throw NotFoundException("Task with ID $taskId not found")

            when (appUserRole) {
                AuthRole.ADMIN.name -> {}
                AuthRole.MANAGER.name -> {
                    val isManagerOfTeam = Teams.selectAll()
                        .where { (Teams.id eq task.assignedToTeam?.id?.value) and (Teams.createdBy eq userId) }.count() > 0
                    if (!isManagerOfTeam) {
                        throw AuthorizationException("Managers can only delete tasks in teams they created")
                    }
                }
                AuthRole.USER.name -> {
                    val teamLeaderRoleId = Roles
                        .selectAll().where(Roles.name eq "Team Leader")
                        .singleOrNull()?.get(Roles.id)
                        ?: throw TeamRoleNotFoundException("Role 'Team Leader' not found")

                    val isTeamLeaderAndCreator = UserTeams.selectAll().where {
                        (UserTeams.user eq userId) and
                                (UserTeams.team eq task.assignedToTeam?.id?.value) and
                                (UserTeams.role eq teamLeaderRoleId)
                    }.count() > 0

                    if (!isTeamLeaderAndCreator) {
                        throw AuthorizationException("Only Team Leaders can delete tasks they created in their team")
                    }
                }
                else -> throw AuthorizationException("You are not authorized to delete tasks")
            }
            task.delete()
        }
    }

    private fun getAllTasks(offset: Int, limit: Int, filter: String?): List<Task> {
        return Task.find {
            if (!filter.isNullOrBlank()) {
                (Tasks.title.lowerCase() like "%$filter%")
            } else {
                Op.TRUE
            }
        }
            .limit(count = limit).offset(start = offset.toLong())
            .toList()
    }


    private fun getTasksForManagedTeams(userId: Int?, offset: Int, limit: Int, filter: String?): List<Task> {
        val userAppRole = AppUser.find { AppUsers.id eq userId }
            .singleOrNull()?.role
            ?: throw IllegalArgumentException("Invalid AppUser")

        return if (userAppRole == AuthRole.MANAGER.name) {
            val managedTeamIds = Teams
                .selectAll().where { Teams.createdBy eq userId }
                .map { it[Teams.id].value }

            if (managedTeamIds.isEmpty()) return emptyList()

            Tasks.selectAll().where {
                ((Tasks.assignedByUser eq userId) or
                        (Tasks.assignedToTeam inList managedTeamIds)) and if (!filter.isNullOrBlank()) {
                    (Tasks.title like "%$filter%") or (Tasks.description like "%$filter%")
                } else {
                    Op.TRUE
                }
            }
                .limit(count = limit).offset(start = offset.toLong())
                .map { Task.wrapRow(it) }
        } else {
            val teamLeaderRoleId = Roles
                .selectAll().where { Roles.name eq "TEAM_LEADER" }
                .singleOrNull()?.get(Roles.id)
                ?: throw IllegalArgumentException("Role 'TEAM_LEADER' not found")

            val managedTeamIds = UserTeams
                .selectAll().where { (UserTeams.user eq userId) and (UserTeams.role eq teamLeaderRoleId) }
                .map { it[UserTeams.team].value }

            if (managedTeamIds.isEmpty()) return emptyList()

            Tasks.selectAll().where {
                (Tasks.assignedToTeam inList managedTeamIds) or
                        (Tasks.assignedByTeam inList managedTeamIds) and if (!filter.isNullOrBlank()) {
                    (Tasks.title like "%$filter%") or (Tasks.description like "%$filter%")
                } else {
                    Op.TRUE
                }
            }
                .limit(count = limit).offset(start = offset.toLong())
                .map { Task.wrapRow(it) }
        }
    }

    private fun getTasksAssignedToUser(userId: Int?, offset: Int, limit: Int, filter: String?): List<Task> {
        val userTeamIds = UserTeams
            .selectAll().where { UserTeams.user eq userId }
            .map { it[UserTeams.team].value }

        if (userTeamIds.isEmpty()) return emptyList()

        return Tasks
            .selectAll().where { Tasks.assignedToTeam inList userTeamIds and if (!filter.isNullOrBlank()) {
                (Tasks.title like "%$filter%")
            } else {
                Op.TRUE
            }}
            .limit(count = limit).offset(start = offset.toLong())
            .map { Task.wrapRow(it) }
    }

    private fun countAllTasks(filter: String?): Long {
        return Tasks.selectAll().where {
            if (!filter.isNullOrBlank()) {
                (Tasks.title like "%$filter%")
            } else {
                Op.TRUE
            }
        }.count()
    }

    private fun countTasksForManager(userId: Int?, filter: String?): Long {
        val managedTeamIds = Teams.selectAll().where { Teams.createdBy eq userId }
            .map { it[Teams.id].value }

        if (managedTeamIds.isEmpty()) return 0L

        return Tasks.selectAll().where {
            (Tasks.assignedToTeam inList managedTeamIds) and
                    if (!filter.isNullOrBlank()) {
                        (Tasks.title like "%$filter%") or (Tasks.description like "%$filter%")
                    } else {
                        Op.TRUE
                    }
        }.count()
    }

    private fun countTasksAssignedToUser(userId: Int?, filter: String?): Long {
        if (userId == null) throw IllegalArgumentException("User ID cannot be null")

        return Tasks.selectAll().where {
            (Tasks.assignedToUser eq userId) and
                    if (!filter.isNullOrBlank()) {
                        (Tasks.title like "%$filter%") or (Tasks.description like "%$filter%")
                    } else {
                        Op.TRUE
                    }
        }.count()
    }
}