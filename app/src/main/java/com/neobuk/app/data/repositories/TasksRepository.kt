package com.neobuk.app.data.repositories

import com.neobuk.app.data.models.Task
import com.neobuk.app.data.models.TaskStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TaskDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    val title: String,
    val description: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    val status: String = "TODO",
    @SerialName("created_at") val createdAt: String? = null
)

class TasksRepository(private val supabaseClient: SupabaseClient) {

    private val database = supabaseClient.postgrest

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun fetchTasks(businessId: String): List<Task> {
        return try {
            _isLoading.value = true
            val result = database["tasks"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<TaskDTO>()

            val mapped = result.map { it.toTask() }
            _tasks.value = mapped
            mapped
        } catch (e: Exception) {
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun addTask(
        businessId: String,
        title: String,
        description: String?,
        dueDate: Long?
    ): Result<Task> {
        return try {
            _isLoading.value = true
            val dto = TaskDTO(
                businessId = businessId,
                title = title,
                description = description,
                dueDate = dueDate?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it).toString() },
                status = "TODO"
            )

            val result = database["tasks"]
                .insert(dto) { select() }
                .decodeSingle<TaskDTO>()

            val task = result.toTask()
            _tasks.value = listOf(task) + _tasks.value
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updateTaskStatus(task: Task, status: TaskStatus): Result<Unit> {
        return try {
            // Only update if it's a persistent task (has a regular UUID, not stock_...)
            if (task.id.startsWith("stock_")) return Result.success(Unit) // Local only

            database["tasks"]
                .update(mapOf("status" to status.name)) {
                    filter { eq("id", task.id) }
                }

            _tasks.value = _tasks.value.map {
                if (it.id == task.id) it.copy(status = status) else it
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTaskDueDate(task: Task, dueDate: Long): Result<Unit> {
         return try {
            if (task.id.startsWith("stock_")) return Result.success(Unit)

            database["tasks"]
                .update(mapOf("due_date" to kotlinx.datetime.Instant.fromEpochMilliseconds(dueDate).toString())) {
                    filter { eq("id", task.id) }
                }

            _tasks.value = _tasks.value.map {
                if (it.id == task.id) it.copy(dueDate = dueDate) else it
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            if (taskId.startsWith("stock_")) return Result.success(Unit)

            database["tasks"]
                .delete {
                    filter { eq("id", taskId) }
                }

            _tasks.value = _tasks.value.filter { it.id != taskId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun clearData() {
        _tasks.value = emptyList()
    }

    private fun TaskDTO.toTask(): Task {
        return Task(
            id = this.id ?: UUID.randomUUID().toString(),
            title = this.title,
            relatedLink = this.description,
            dueDate = try {
                this.dueDate?.let { kotlinx.datetime.Instant.parse(it).toEpochMilliseconds() }
            } catch (e: Exception) { null },
            status = try {
                TaskStatus.valueOf(this.status)
            } catch (e: Exception) { TaskStatus.TODO }
        )
    }
}
