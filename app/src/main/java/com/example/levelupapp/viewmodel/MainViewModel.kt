package com.example.levelupapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.levelupapp.data.*
import com.example.levelupapp.data.RetrofitClient
import com.example.levelupapp.data.TokenManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class MainViewModel(private val context: Context) : ViewModel() {
    private val TAG = "MainViewModel"

    val username = mutableStateOf("")
    val tasks = mutableStateOf<List<Task>?>(null)
    val categories = mutableStateOf<List<Category>?>(null)
    val stats = mutableStateOf<List<Stat>?>(null)
    val avatarUrl = mutableStateOf<String?>(null)
    val errorMessage = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(true)
    val progressPercentage = mutableStateOf(0f) // Процент выполненных задач на сегодня
    val experienceProgress = mutableStateOf(0f) // Процент опыта до следующего уровня
    val level = mutableStateOf(0)
    val experience = mutableStateOf(0)
    val nextLevelExp = mutableStateOf(100)
    val todayTasksCount = mutableStateOf(0)

    init {
        Log.d(TAG, "ViewModel initialized")
        loadData()
    }

    fun loadData() {
        isLoading.value = true
        Log.d(TAG, "Starting data load after auth")
        viewModelScope.launch {
            try {
                // Загрузка данных пользователя
                val userResponse = RetrofitClient.apiService.getUser()
                if (userResponse.isSuccessful) {
                    username.value = userResponse.body()?.username ?: "пользователь"
                    Log.d(TAG, "User loaded: ${username.value}")
                } else {
                    errorMessage.value = "Ошибка загрузки данных пользователя: ${userResponse.code()}"
                    Log.e(TAG, "User load failed: ${userResponse.code()}")
                    isLoading.value = false
                    return@launch
                }

                // Загрузка задач с ограничением
                val tasksResponse = RetrofitClient.apiService.getTasks()
                if (tasksResponse.isSuccessful) {
                    tasks.value = tasksResponse.body()?.take(50) // Ограничение
                    Log.d(TAG, "Tasks loaded: ${tasks.value?.size}")
                    updateProgress()
                } else {
                    errorMessage.value = "Ошибка загрузки задач: ${tasksResponse.code()}"
                    Log.e(TAG, "Tasks load failed: ${tasksResponse.code()}")
                }

                // Загрузка категорий
                val categoriesResponse = RetrofitClient.apiService.getCategories()
                if (categoriesResponse.isSuccessful) {
                    categories.value = categoriesResponse.body()?.take(20) // Ограничение
                    Log.d(TAG, "Categories loaded: ${categories.value?.size}")
                } else {
                    errorMessage.value = "Ошибка загрузки категорий: ${categoriesResponse.code()}"
                    Log.e(TAG, "Categories load failed: ${categoriesResponse.code()}")
                }

                // Загрузка характеристик
                val statsResponse = RetrofitClient.apiService.getStats()
                if (statsResponse.isSuccessful) {
                    stats.value = statsResponse.body()
                    val levelStat = stats.value?.find { it.name == "Current Level" }
                    val expStat = stats.value?.find { it.name == "Experience Gained" }
                    level.value = levelStat?.value ?: 0
                    experience.value = expStat?.value ?: 0
                    nextLevelExp.value = ((level.value + 1) * (level.value + 1) * 100)
                    updateExperienceProgress()
                    Log.d(TAG, "Stats loaded: Level=$level, Experience=$experience")
                } else {
                    errorMessage.value = "Ошибка загрузки характеристик: ${statsResponse.code()}"
                    Log.e(TAG, "Stats load failed: ${statsResponse.code()}")
                }

                // Загрузка аватара
                val avatarResponse = RetrofitClient.apiService.getAvatar()
                if (avatarResponse.isSuccessful) {
                    avatarUrl.value = avatarResponse.body()?.firstOrNull()?.avatar_details?.image_url
                    Log.d(TAG, "Avatar loaded: ${avatarUrl.value}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            } finally {
                isLoading.value = false
                Log.d(TAG, "Data load completed")
            }
        }
    }

    private fun updateProgress() {
        val today = LocalDate.now().toString()
        val todayTasks = tasks.value?.filter { it.due_date?.startsWith(today) == true } ?: emptyList()
        val completedTasks = todayTasks.count { it.is_completed }
        todayTasksCount.value = todayTasks.size
        progressPercentage.value = if (todayTasks.isNotEmpty()) {
            (completedTasks.toFloat() / todayTasks.size) * 100
        } else {
            0f
        }
        Log.d(TAG, "Progress updated: $progressPercentage.value%")
    }

    private fun updateExperienceProgress() {
        experienceProgress.value = if (nextLevelExp.value > 0) {
            (experience.value.toFloat() / nextLevelExp.value) * 100
        } else {
            0f
        }
        Log.d(TAG, "Experience progress updated: $experienceProgress.value%")
    }

    fun addTask(task: TaskRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createTask(task)
                if (response.isSuccessful) {
                    loadData()
                    onSuccess()
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

    fun updateTask(id: Int, task: TaskRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateTask(id, task)
                if (response.isSuccessful) {
                    loadData()
                    onSuccess()
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

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteTask(id)
                if (response.isSuccessful) {
                    loadData()
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

    fun toggleTask(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val task = tasks.value?.find { it.id == id }
                if (task == null) {
                    errorMessage.value = "Задача не найдена"
                    Log.e(TAG, "Task not found: $id")
                    return@launch
                }
                if (task.is_completed && isCompleted) return@launch
                if (!isCompleted && task.is_completed) {
                    errorMessage.value = "Нельзя отметить выполненную задачу как невыполненную"
                    Log.w(TAG, "Attempt to uncheck completed task: $id")
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
                val response = RetrofitClient.apiService.updateTask(id, taskRequest)
                if (response.isSuccessful) {
                    if (isCompleted) {
                        updateExperienceAndStats(task)
                    }
                    loadData()
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

    fun addCategory(category: CategoryRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createCategory(category)
                if (response.isSuccessful) {
                    loadData()
                    onSuccess()
                } else {
                    errorMessage.value = "Ошибка при создании категории: ${response.code()}"
                    Log.e(TAG, "Add category failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun updateCategory(id: Int, category: CategoryRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateCategory(id, category)
                if (response.isSuccessful) {
                    loadData()
                    onSuccess()
                } else {
                    errorMessage.value = "Ошибка при обновлении категории: ${response.code()}"
                    Log.e(TAG, "Update category failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteCategory(id)
                if (response.isSuccessful) {
                    loadData()
                } else {
                    errorMessage.value = "Ошибка при удалении категории: ${response.code()}"
                    Log.e(TAG, "Delete category failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun addStat(stat: StatRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createStat(stat)
                if (response.isSuccessful) {
                    loadData()
                    onSuccess()
                } else {
                    errorMessage.value = "Ошибка при создании характеристики: ${response.code()}"
                    Log.e(TAG, "Add stat failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun updateStat(id: Int, stat: StatRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateStat(id, stat)
                if (response.isSuccessful) {
                    loadData()
                    onSuccess()
                } else {
                    errorMessage.value = "Ошибка при обновлении характеристики: ${response.code()}"
                    Log.e(TAG, "Update stat failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun deleteStat(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteStat(id)
                if (response.isSuccessful) {
                    loadData()
                } else {
                    errorMessage.value = "Ошибка при удалении характеристики: ${response.code()}"
                    Log.e(TAG, "Delete stat failed: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun logout(navController: NavController) {
        viewModelScope.launch {
            TokenManager.removeTokens(context)
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
            Log.d(TAG, "Logout successful")
        }
    }

    private fun updateExperienceAndStats(task: Task) {
        val experiencePoints = task.priority ?: 50
        experience.value += experiencePoints

        if (experience.value >= nextLevelExp.value) {
            level.value += 1
            experience.value -= nextLevelExp.value
            nextLevelExp.value = (level.value + 1) * (level.value + 1) * 100
        }

        updateExperienceProgress()
        Log.d(TAG, "Experience updated: $experience.value / $nextLevelExp.value")

        task.stat_id?.let { statId ->
            stats.value?.find { it.id == statId }?.let { stat ->
                viewModelScope.launch {
                    try {
                        val statRequest = StatRequest(
                            name = stat.name,
                            description = stat.description,
                            color = stat.color,
                            value = stat.value + 1
                        )
                        val response = RetrofitClient.apiService.updateStat(statId, statRequest)
                        if (!response.isSuccessful) {
                            errorMessage.value = "Ошибка при обновлении характеристики: ${response.code()}"
                            Log.e(TAG, "Update stat failed: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        errorMessage.value = "Ошибка сети: ${e.message}"
                        Log.e(TAG, "Network error: ${e.message}", e)
                    }
                }
            }
        }

        stats.value?.find { it.name == "Experience Gained" }?.let { expStat ->
            viewModelScope.launch {
                try {
                    val statRequest = StatRequest(
                        name = expStat.name,
                        description = expStat.description,
                        color = expStat.color,
                        value = experience.value
                    )
                    val response = RetrofitClient.apiService.updateStat(expStat.id, statRequest)
                    if (!response.isSuccessful) {
                        errorMessage.value = "Ошибка при обновлении опыта: ${response.code()}"
                        Log.e(TAG, "Update experience stat failed: ${response.code()}")
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Ошибка сети: ${e.message}"
                    Log.e(TAG, "Network error: ${e.message}", e)
                }
            }
        }

        stats.value?.find { it.name == "Current Level" }?.let { levelStat ->
            viewModelScope.launch {
                try {
                    val statRequest = StatRequest(
                        name = levelStat.name,
                        description = levelStat.description,
                        color = levelStat.color,
                        value = level.value
                    )
                    val response = RetrofitClient.apiService.updateStat(levelStat.id, statRequest)
                    if (!response.isSuccessful) {
                        errorMessage.value = "Ошибка при обновлении уровня: ${response.code()}"
                        Log.e(TAG, "Update level stat failed: ${response.code()}")
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Ошибка сети: ${e.message}"
                    Log.e(TAG, "Network error: ${e.message}", e)
                }
            }
        }
    }
}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}