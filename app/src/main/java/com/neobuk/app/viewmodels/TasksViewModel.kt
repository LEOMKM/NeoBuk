package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Product is now from repositories to match ProductsRepository output
import com.neobuk.app.data.repositories.Product 
import com.neobuk.app.data.models.Task
import com.neobuk.app.data.models.TaskPriority
import com.neobuk.app.data.models.TaskStatus
import com.neobuk.app.data.repositories.ProductsRepository
import com.neobuk.app.data.repositories.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(
    private val productsRepository: ProductsRepository,
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private var currentBusinessId: String? = null

    // Persistent tasks from Supabase
    private val persistentTasks = tasksRepository.tasks

    // Auto-generated tasks (in-memory)
    private val _stockTasks = MutableStateFlow<List<Task>>(emptyList())

    // Combined Tasks exposed to UI
    val tasks: StateFlow<List<Task>> = combine(persistentTasks, _stockTasks) { persistent, stock ->
        (persistent + stock).sortedBy { it.dueDate ?: Long.MAX_VALUE }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Pending count
    val pendingTaskCount: StateFlow<Int> = tasks.map { list ->
        list.count { it.status == TaskStatus.TODO || it.status == TaskStatus.IN_PROGRESS }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val isLoading: StateFlow<Boolean> = tasksRepository.isLoading

    init {
        // Observe products to generate stock tasks automatically
        viewModelScope.launch {
            productsRepository.products.collect { products ->
                generateStockTasks(products)
            }
        }
    }

    fun setBusinessId(businessId: String) {
        currentBusinessId = businessId
        viewModelScope.launch {
            tasksRepository.fetchTasks(businessId)
        }
    }

    fun addTask(task: Task) {
        val businessId = currentBusinessId ?: return
        viewModelScope.launch {
            tasksRepository.addTask(
                businessId = businessId,
                title = task.title,
                description = task.relatedLink,
                dueDate = task.dueDate
            )
        }
    }

    fun updateTaskStatus(task: Task, status: TaskStatus) {
        viewModelScope.launch {
            tasksRepository.updateTaskStatus(task, status)
        }
    }

    fun updateTaskDueDate(task: Task, dueDate: Long) {
        viewModelScope.launch {
            tasksRepository.updateTaskDueDate(task, dueDate)
        }
    }

    fun removeTask(task: Task) {
        viewModelScope.launch {
            tasksRepository.deleteTask(task.id)
        }
    }

    private fun generateStockTasks(products: List<Product>) {
        val stockTasks = products.filter { it.quantity < 5.0 }.map { product ->
            Task(
                id = "stock_${product.id}",
                title = "Reorder ${product.name}",
                relatedLink = "Stock Low: ${product.quantity} ${product.unit}",
                dueDate = System.currentTimeMillis() + 86400000, // +1 day
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH
            )
        }
        _stockTasks.value = stockTasks
    }
}
