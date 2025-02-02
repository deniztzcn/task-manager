import com.taskmanager.application.dtos.PaginationResponse
import com.taskmanager.application.dtos.requests.TaskRequestDTO
import com.taskmanager.application.dtos.responses.TaskResponseDTO
import com.taskmanager.domain.entities.Task
import io.ktor.server.auth.jwt.*

interface ITaskService {
    fun getTasksForRole(appUserRole: String?, userId: Int?, page: Int, limit: Int, filter: String?): PaginationResponse<TaskResponseDTO>
    fun assignTask(taskRequest: TaskRequestDTO, principal: JWTPrincipal): TaskResponseDTO
    fun getTaskById(appUserRole: String, taskId: Int, userId: Int): Task?
    fun updateTask(taskId: Int, taskRequestDTO: TaskRequestDTO, userId: Int, appUserRole: String): Task?
    fun deleteTask(taskId: Int, userId: Int, appUserRole: String)
}