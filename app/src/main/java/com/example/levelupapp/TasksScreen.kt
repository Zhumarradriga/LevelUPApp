package com.example.levelupapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.levelupapp.data.Category
import com.example.levelupapp.data.Stat
import com.example.levelupapp.data.Task
import com.example.levelupapp.data.TaskRequest
import com.example.levelupapp.viewmodel.MainViewModel
import com.example.levelupapp.viewmodel.MainViewModelFactory
import com.example.levelupapp.viewmodel.TaskViewModel
import com.example.levelupapp.viewmodel.TaskViewModelFactory
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(navController: NavController) {
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(LocalContext.current))
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(LocalContext.current))
    var showTaskDialog by remember { mutableStateOf(false) }
    var showTaskInfoDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(taskViewModel.errorMessage.value) {
        taskViewModel.errorMessage.value?.let {
            snackbarHostState.showSnackbar(it)
            taskViewModel.errorMessage.value = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LevelUp", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Привет, ${mainViewModel.username.value}!",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { mainViewModel.experienceProgress.value / 100 },
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF562BD7),
                                    trackColor = Color(0xFFC9BEE6)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${mainViewModel.experience.value}/${mainViewModel.nextLevelExp.value}",
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${mainViewModel.level.value}",
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .background(Color(0xFF562BD7), CircleShape)
                                        .padding(4.dp),
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        AsyncImage(
                            model = mainViewModel.avatarUrl.value ?: "",
                            contentDescription = "Аватар",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC9BEE6))
                                .clickable { showProfileDialog = true }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Открыть боковое меню */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Меню")
                    }
                }
            )
        },
        bottomBar = { TaskBottomNavigationBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Учитываем padding от Scaffold
                .padding(horizontal = 16.dp)
        ) {
            item {
                if (taskViewModel.isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
                } else {
                    CalendarSection(taskViewModel) { date ->
                        Log.d("TasksScreen", "Selected date: $date")
                        taskViewModel.selectedDate.value = date
                    }
                }
            }
            item {
                CharacteristicsSection(mainViewModel) { showProfileDialog = true }
            }
            item {
                TasksTodaySection(
                    taskViewModel = taskViewModel,
                    mainViewModel = mainViewModel,
                    onAddTask = { showTaskDialog = true; selectedTask = null },
                    onEditTask = { task -> selectedTask = task; showTaskDialog = true },
                    onViewTask = { task -> selectedTask = task; showTaskInfoDialog = true }
                )
            }
            item {
                mainViewModel.errorMessage.value?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE))
                            .padding(10.dp)
                            .fillMaxWidth(),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    if (showTaskDialog) {
        TaskDialog(
            task = selectedTask,
            viewModel = taskViewModel,
            selectedDate = taskViewModel.selectedDate.value,
            onDismiss = { showTaskDialog = false; selectedTask = null }
        )
    }

    if (showTaskInfoDialog) {
        TaskInfoDialog(
            task = selectedTask,
            viewModel = taskViewModel,
            onDismiss = { showTaskInfoDialog = false; selectedTask = null }
        )
    }

    if (showProfileDialog) {
        TaskProfileDialog(
            viewModel = mainViewModel,
            onDismiss = { showProfileDialog = false },
            navController = navController,
            onAddStat = { /* Обрабатывается в ProfileDialog */ },
            onSelectStat = { /* Обрабатывается в ProfileDialog */ }
        )
    }
}

@Composable
fun CalendarSection(taskViewModel: TaskViewModel, onDateSelected: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val days = (-3..3).map { today.plusDays(it.toLong()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { date ->
            val taskCount = taskViewModel.getTasksForDate()?.count { parseDueDateToLocalDate(it.due_date)?.isEqual(date) == true } ?: 0
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .padding(horizontal = 4.dp)
                    .clickable { onDateSelected(date) },
                colors = CardDefaults.cardColors(
                    containerColor = if (date == taskViewModel.selectedDate.value) Color(0xFF6A3BE8) else Color(0xFF562BD7)
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = date.month.getDisplayName(
                            java.time.format.TextStyle.FULL,
                            Locale("ru")
                        ).uppercase(),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 28.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = date.dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.SHORT,
                            Locale("ru")
                        ).uppercase(),
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$taskCount задач",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CharacteristicsSection(viewModel: MainViewModel, onOpenProfile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Характеристики",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${viewModel.stats.value?.count { it.is_default && it.name != "Current Level" } ?: 0}",
                fontSize = 16.sp,
                modifier = Modifier
                    .background(Color(0xFF562BD7), CircleShape)
                    .padding(8.dp),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .heightIn(max = 150.dp) // Ограничение максимальной высоты
                .fillMaxWidth()
        ) {
            items(viewModel.stats.value?.filter { it.is_default && it.name != "Current Level" } ?: emptyList()) { stat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(android.graphics.Color.parseColor(stat.color ?: "#562BD7"))
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable { onOpenProfile() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stat.name} ${stat.value}",
                            fontSize = 14.sp,
                            color = TaskgetContrastColor(stat.color ?: "#562BD7"),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TasksTodaySection(
    taskViewModel: TaskViewModel,
    mainViewModel: MainViewModel,
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    onViewTask: (Task) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Задачи на ${taskViewModel.selectedDate.value.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru")))}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${taskViewModel.getTasksForDate()?.size ?: 0}",
                fontSize = 16.sp,
                modifier = Modifier
                    .background(Color(0xFF562BD7), CircleShape)
                    .padding(8.dp),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        val filteredTasks = taskViewModel.getTasksForDate() ?: emptyList()
        if (filteredTasks.isEmpty()) {
            Text(
                text = "Задач нет",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .fillMaxWidth()
            ) {
                items(filteredTasks) { task ->
                    TaskItem(
                        task = task,
                        taskViewModel = taskViewModel, // Явно указываем taskViewModel
                        mainViewModel = mainViewModel, // Явно указываем mainViewModel
                        onEditTask = onEditTask,
                        onViewTask = onViewTask
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "+ Добавить задачу",
            fontSize = 16.sp,
            color = Color(0xFF562BD7),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { onAddTask() }
                .padding(8.dp)
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    taskViewModel: TaskViewModel,
    mainViewModel: MainViewModel,
    onEditTask: (Task) -> Unit,
    onViewTask: (Task) -> Unit
) {
    val stat = mainViewModel.stats.value?.find { it.id == task.stat_id }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onViewTask(task) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.is_completed,
                onCheckedChange = { taskViewModel.toggleTask(task.id.toString(), it) },
                enabled = !task.is_completed,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF562BD7))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1D0C51)
                )
                task.due_date?.let {
                    val formattedTime = parseDueDateTime(it, "HH:mm")
                    Text(
                        text = formattedTime ?: "Неверный формат даты",
                        fontSize = 12.sp,
                        color = Color(0xFF7F7F7F)
                    )
                }
            }
            Row {
                IconButton(onClick = { onEditTask(task) }, enabled = !task.is_completed) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                }
                IconButton(onClick = { taskViewModel.deleteTask(task.id.toString()) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        Color(android.graphics.Color.parseColor(stat?.color ?: "#562BD7")),
                        RoundedCornerShape(5.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${task.priority / 50}",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
@Composable
fun TaskInfoDialog(
    task: Task?,
    viewModel: TaskViewModel, // Изменили тип
    onDismiss: () -> Unit
) {
    task ?: return
    val category = Category(id = 0, name = "Без категории", icon = "fas fa-folder", description = null) // Мок-данные
    val stat = Stat(id = 0, name = "Без характеристики", value = 0, color = "#000000", is_default = true, description = null) // Мок-данные

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF0EBF5),
            modifier = Modifier
                .width(400.dp)
                .border(2.dp, Color(0xFF000080), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }
                Text(
                    text = task.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Описание: ${task.description ?: "Без описания"}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Дедлайн: ${
                        parseDueDateTime(task.due_date, "dd.MM.yyyy HH:mm") ?: "Без дедлайна"
                    }",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Приоритет: ${task.priority}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Категория: ${category.name}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Характеристика: ${stat.name}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Очки: +${task.priority / 50}",
                    fontSize = 14.sp,
                    color = Color(android.graphics.Color.parseColor(stat.color ?: "#562BD7"))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TaskDialog(
    task: Task? = null,
    viewModel: TaskViewModel, // Изменили тип
    selectedDate: LocalDate,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var dueDate by remember { mutableStateOf(
        task?.due_date ?: selectedDate.atTime(12, 0).atOffset(OffsetDateTime.now().offset).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    ) }
    var categoryId by remember { mutableStateOf(task?.category_id?.toString() ?: "") }
    var statId by remember { mutableStateOf(task?.stat_id?.toString() ?: "") }
    var priority by remember { mutableStateOf(task?.priority?.toString() ?: "50") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF0EBF5),
            modifier = Modifier
                .width(400.dp)
                .border(2.dp, Color(0xFF000080), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок задачи") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание задачи") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Предполагается, что категории и статистики можно получить из MainViewModel
                TaskDropdownMenu(
                    items = mapOf("1" to "Без категории"), // Замените на реальные данные из MainViewModel
                    selectedItem = categoryId,
                    onItemSelected = { categoryId = it },
                    label = "Категория"
                )
                Spacer(modifier = Modifier.height(8.dp))
                TaskDropdownMenu(
                    items = mapOf("1" to "Без характеристики"), // Замените на реальные данные из MainViewModel
                    selectedItem = statId,
                    onItemSelected = { statId = it },
                    label = "Характеристика"
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = parseDueDateTime(dueDate, "dd.MM.yyyy HH:mm") ?: "",
                    onValueChange = { newValue ->
                        try {
                            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale("ru"))
                            val parsed = java.time.LocalDateTime.parse(newValue, formatter)
                            dueDate = parsed.atOffset(OffsetDateTime.now().offset)
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        } catch (e: Exception) {
                            // Игнорируем некорректный ввод
                        }
                    },
                    label = { Text("Дедлайн (дд.ММ.гггг ЧЧ:мм)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TaskDropdownMenu(
                    items = mapOf(
                        "50" to "Низкий",
                        "150" to "Средний",
                        "400" to "Высокий"
                    ),
                    selectedItem = priority,
                    onItemSelected = { priority = it },
                    label = "Приоритет"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            viewModel.errorMessage.value = "Введите заголовок задачи"
                            return@Button
                        }
                        val taskRequest = TaskRequest(
                            title = title,
                            description = description.takeIf { it.isNotBlank() },
                            due_date = dueDate,
                            category_id = categoryId.toIntOrNull(),
                            stat_id = statId.toIntOrNull(),
                            priority = priority.toIntOrNull() ?: 50
                        )
                        if (task != null) {
                            viewModel.updateTask(task.id.toString(), taskRequest) { onDismiss() }
                        } else {
                            viewModel.addTask(taskRequest) { onDismiss() }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF562BD7))
                ) {
                    Text(
                        text = if (task != null) "Сохранить" else "Создать",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TaskProfileDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    navController: NavController,
    onAddStat: () -> Unit,
    onSelectStat: (Stat) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF6F1FE),
            modifier = Modifier
                .width(350.dp)
                .height(600.dp)
                .border(2.dp, Color(0xFF000080), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }
                AsyncImage(
                    model = viewModel.avatarUrl.value ?: "",
                    contentDescription = "Аватар",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFC9BEE6))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Привет,",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = viewModel.username.value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${viewModel.level.value}",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .background(Color(0xFF562BD7), CircleShape)
                            .padding(8.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { viewModel.progressPercentage.value / 100 },
                        modifier = Modifier
                            .width(150.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF562BD7),
                        trackColor = Color(0xFFC9BEE6)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${viewModel.experience.value}/${viewModel.nextLevelExp.value}",
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Характеристики",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    items(viewModel.stats.value?.filter { it.is_default && it.name != "Current Level" } ?: emptyList()) { stat ->
                        TaskStatItem(
                            stat = stat,
                            viewModel = viewModel,
                            onEditStat = { selectedStat -> onSelectStat(selectedStat); onAddStat() },
                            onSelectStat = onSelectStat
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onAddStat) {
                    Text("+ Добавить характеристику", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.logout(navController) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF562BD7))
                ) {
                    Text("Выход", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TaskBottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("main") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Главная") },
            label = { Text("Главная") }
        )
        NavigationBarItem(
            selected = true,
            onClick = { /* Уже на экране задач */ },
            icon = { Icon(Icons.Default.Add, contentDescription = "Задачи") },
            label = { Text("Задачи") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("calendar") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Календарь") },
            label = { Text("Календарь") }
        )
    }
}

@Composable
fun TaskDropdownMenu(
    items: Map<String, String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = items[selectedItem] ?: "",
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.Add, contentDescription = "Раскрыть")
                }
            }
        )
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { (key, value) ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(value) },
                    onClick = {
                        onItemSelected(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TaskStatItem(
    stat: Stat,
    viewModel: MainViewModel,
    onEditStat: (Stat) -> Unit,
    onSelectStat: (Stat) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelectStat(stat) },
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(stat.color ?: "#562BD7"))
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${stat.name}: ${stat.value}",
                fontSize = 14.sp,
                color = TaskgetContrastColor(stat.color ?: "#562BD7"),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onEditStat(stat) }) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать характеристику")
            }
        }
    }
}

fun parseDueDateTime(dueDate: String?, pattern: String): String? {
    if (dueDate == null) return null
    return try {
        val dateTime = OffsetDateTime.parse(dueDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern(pattern, Locale("ru")))
    } catch (e: DateTimeParseException) {
        null
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

@Composable
fun TaskgetContrastColor(hexColor: String): Color {
    val color = try {
        android.graphics.Color.parseColor(hexColor)
    } catch (e: Exception) {
        return Color.Black
    }
    val r = android.graphics.Color.red(color)
    val g = android.graphics.Color.green(color)
    val b = android.graphics.Color.blue(color)
    val brightness = (r * 299 + g * 587 + b * 114) / 1000
    return if (brightness > 128) Color(0xFF110730) else Color.White
}