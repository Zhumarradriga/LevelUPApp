package com.example.levelupapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.levelupapp.data.RetrofitClient
import com.example.levelupapp.data.Task
import com.example.levelupapp.data.TaskRequest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class TaskViewModel(private val context: Context) : ViewModel() {
    private val TAG = "TaskViewModel"

    val tasks = mutableStateOf<List<Task>?>(null)
    val selectedDate = mutableStateOf(LocalDate.now()) // 2025-06-23
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    init {
        Log.d(TAG, "TaskViewModel initialized with date: ${selectedDate.value}")
        loadTasks()
    }

    fun loadTasks() {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getTasks()
                if (response.isSuccessful) {
                    tasks.value = response.body()?.take(50)
                    Log.d(TAG, "Tasks loaded: ${tasks.value?.size}, first task due_date: ${tasks.value?.firstOrNull()?.due_date}")
                    if (tasks.value.isNullOrEmpty()) {
                        // Добавляем мок-данные для тестирования
                        tasks.value = listOf(
                            Task(
                                id = 1,
                                title = "Тестовая задача",
                                due_date = selectedDate.value.atTime(10, 0).atOffset(OffsetDateTime.now().offset).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                category_id = null,
                                stat_id = null,
                                priority = 50,
                                is_completed = false,
                                description = "test"
                            )
                        )
                        Log.d(TAG, "Added mock task with due_date: ${tasks.value?.firstOrNull()?.due_date}")
                    }
                } else {
                    errorMessage.value = "Ошибка загрузки задач: ${response.code()}"
                    Log.e(TAG, "Tasks load failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun addTask(taskRequest: TaskRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createTask(taskRequest)
                if (response.isSuccessful) {
                    loadTasks()
                    onSuccess()
                    Log.d(TAG, "Task added successfully")
                } else {
                    errorMessage.value = "Ошибка при создании задачи: ${response.code()}"
                    Log.e(TAG, "Add task failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun updateTask(taskId: String, taskRequest: TaskRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateTask(taskId.toInt(), taskRequest)
                if (response.isSuccessful) {
                    loadTasks()
                    onSuccess()
                    Log.d(TAG, "Task updated successfully")
                } else {
                    errorMessage.value = "Ошибка при обновлении задачи: ${response.code()}"
                    Log.e(TAG, "Update task failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteTask(taskId.toInt())
                if (response.isSuccessful) {
                    loadTasks()
                    Log.d(TAG, "Task deleted successfully")
                } else {
                    errorMessage.value = "Ошибка при удалении задачи: ${response.code()}"
                    Log.e(TAG, "Delete task failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun toggleTask(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val task = tasks.value?.find { it.id.toString() == taskId }
                if (task == null) {
                    errorMessage.value = "Задача не найдена"
                    Log.e(TAG, "Task not found: $taskId")
                    return@launch
                }
                val taskRequest = TaskRequest(
                    title = task.title,
                    description = task.description,
                    due_date = task.due_date,
                    category_id = task.category_id,
                    stat_id = task.stat_id,
                    priority = task.priority,
                    is_completed = isCompleted
                )
                val response = RetrofitClient.apiService.updateTask(taskId.toInt(), taskRequest)
                if (response.isSuccessful) {
                    loadTasks()
                    Log.d(TAG, "Task toggled successfully")
                } else {
                    errorMessage.value = "Ошибка при обновлении задачи: ${response.code()}"
                    Log.e(TAG, "Toggle task failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun getTasksForDate(): List<Task>? {
        val filteredTasks = tasks.value?.filter {
            val taskDate = parseDueDateToLocalDate(it.due_date)
            Log.d("TaskViewModel", "Task date: $taskDate, selected date: ${selectedDate.value}")
            taskDate != null && taskDate.isEqual(selectedDate.value)
        }
        Log.d("TaskViewModel", "Filtered tasks count: ${filteredTasks?.size}")
        return filteredTasks
    }
}

fun parseDueDateToLocalDate(dueDate: String?): LocalDate? {
    if (dueDate == null) return null
    return try {
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val dateTime = java.time.OffsetDateTime.parse(dueDate, formatter)
        dateTime.toLocalDate()
    } catch (e: Exception) {
        Log.e("TaskViewModel", "Error parsing date: $dueDate, error: ${e.message}")
        null
    }
}

class TaskViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}