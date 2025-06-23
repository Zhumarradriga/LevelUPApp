package com.example.levelupapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.levelupapp.data.Task
import com.example.levelupapp.data.TaskRequest
import com.example.levelupapp.viewmodel.MainViewModel
import com.example.levelupapp.viewmodel.MainViewModelFactory
import com.example.levelupapp.viewmodel.TaskViewModel
import com.example.levelupapp.viewmodel.TaskViewModelFactory
import com.example.levelupapp.viewmodel.parseDueDateToLocalDate
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context))
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(context))
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        mainViewModel.loadData()
        taskViewModel.loadTasks()
    }

    LaunchedEffect(taskViewModel.errorMessage.value) {
        taskViewModel.errorMessage.value?.let {
            snackbarHostState.showSnackbar(it)
            taskViewModel.errorMessage.value = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LevelUp",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E27C0)
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Привет, ${mainViewModel.username.value}!",
                                fontSize = 14.sp,
                                color = Color(0xFF1D0C51)
                            )
                            ProgressBar(
                                progress = mainViewModel.experienceProgress.value / 100,
                                level = mainViewModel.level.value,
                                exp = mainViewModel.experience.value,
                                nextExp = mainViewModel.nextLevelExp.value
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        AsyncImage(
                            model = mainViewModel.avatarUrl.value ?: "",
                            contentDescription = "Аватар",
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEADDFF), CircleShape)
                                .border(4.dp, Color(0xFF52399C), CircleShape)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = { TaskBottomNavigationBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F1FE))
                .padding(padding)
                .padding(16.dp)
        ) {
            // Убрана секция DateSection

            // Улучшенный CalendarView
            CalendarView(
                selectedDate = selectedDate,
                taskViewModel = taskViewModel,
                onDateSelected = { date ->
                    selectedDate = date
                    taskViewModel.selectedDate.value = date
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Секция с задачами для выбранной даты
            TasksForSelectedDate(
                selectedDate = selectedDate,
                taskViewModel = taskViewModel,
                onEditTask = { task ->
                    // Реализуйте логику редактирования задачи
                },
                onViewTask = { task ->
                    // Реализуйте логику просмотра задачи
                }
            )
        }
    }
}

@Composable
fun ProgressBar(progress: Float, level: Int, exp: Int, nextExp: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .width(100.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF562BD7),
            trackColor = Color(0xFFC9BEE6)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$exp/$nextExp",
            fontSize = 12.sp,
            color = Color.White
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFF562BD7), CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = level.toString(),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DateSection(
    selectedDate: LocalDate,
    taskViewModel: TaskViewModel,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val days = (-3..3).map { today.plusDays(it.toLong()) }
    val tasksForDate = taskViewModel.getTasksForDate()?.count {
        it.due_date?.let { date -> parseDueDateToLocalDate(date)?.isEqual(selectedDate) } == true
    } ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "$tasksForDate задач на сегодня",
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF110730)
        )
        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D0C51)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { date ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .padding(horizontal = 4.dp)
                        .clickable { onDateSelected(date) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (date == selectedDate) Color(0xFF6A3BE8) else Color(0xFF562BD7)
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = date.month.getDisplayName(TextStyle.FULL, Locale("ru")).uppercase(),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru")).uppercase(),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarView(
    selectedDate: LocalDate,
    taskViewModel: TaskViewModel,
    onDateSelected: (LocalDate) -> Unit
) {
    val tasks by remember { mutableStateOf(taskViewModel.tasks.value ?: emptyList()) }

    // Вычисляем дни с задачами для декорации
    val daysWithTasks = tasks
        .mapNotNull { parseDueDateToLocalDate(it.due_date) }
        .toSet()
    val daysWithCompletedTasks = tasks
        .filter { it.is_completed }
        .mapNotNull { parseDueDateToLocalDate(it.due_date) }
        .toSet()
    val daysWithOnlyPendingTasks = daysWithTasks - daysWithCompletedTasks

    AndroidView(
        factory = { context ->
            MaterialCalendarView(context).apply {
                setSelectedDate(
                    CalendarDay.from(
                        selectedDate.year,
                        selectedDate.monthValue - 1,
                        selectedDate.dayOfMonth
                    )
                )
                setOnDateChangedListener { _, date, _ ->
                    val selectedLocalDate = LocalDate.of(date.year, date.month + 1, date.day)
                    onDateSelected(selectedLocalDate)
                }
                setHeaderTextAppearance(R.style.CalendarHeader)
                setWeekDayTextAppearance(R.style.CalendarWeekDay)
                setDateTextAppearance(R.style.CalendarDate)

                // Добавляем декораторы для дней с задачами
                addDecorator(object : com.prolificinteractive.materialcalendarview.DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay): Boolean {
                        val localDate = LocalDate.of(day.year, day.month + 1, day.day)
                        return localDate in daysWithCompletedTasks
                    }

                    override fun decorate(view: com.prolificinteractive.materialcalendarview.DayViewFacade) {
                        view.setBackgroundDrawable(
                            android.graphics.drawable.GradientDrawable().apply {
                                shape = android.graphics.drawable.GradientDrawable.OVAL
                                setColor(android.graphics.Color.parseColor("#28a745")) // Зеленый для выполненных
                            }
                        )
                    }
                })

                addDecorator(object : com.prolificinteractive.materialcalendarview.DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay): Boolean {
                        val localDate = LocalDate.of(day.year, day.month + 1, day.day)
                        return localDate in daysWithOnlyPendingTasks
                    }

                    override fun decorate(view: com.prolificinteractive.materialcalendarview.DayViewFacade) {
                        view.setBackgroundDrawable(
                            android.graphics.drawable.GradientDrawable().apply {
                                shape = android.graphics.drawable.GradientDrawable.OVAL
                                setColor(android.graphics.Color.parseColor("#562BD7")) // Фиолетовый для невыполненных
                            }
                        )
                    }
                })
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp))
    )
}

@Composable
fun TasksForSelectedDate(
    selectedDate: LocalDate,
    taskViewModel: TaskViewModel,
    onEditTask: (Task) -> Unit,
    onViewTask: (Task) -> Unit
) {
    val tasksForDate = remember(selectedDate) {
        taskViewModel.tasks.value?.filter { task ->
            parseDueDateToLocalDate(task.due_date)?.isEqual(selectedDate) == true
        } ?: emptyList()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Задачи на ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru")))}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D0C51)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (tasksForDate.isEmpty()) {
                Text(
                    text = "Нет задач на выбранную дату",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn {
                    items(tasksForDate) { task ->
                        TaskTaskItem(
                            task = task,
                            viewModel = taskViewModel,
                            onEditTask = onEditTask,
                            onViewTask = onViewTask
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskTaskItem(
    task: Task,
    viewModel: TaskViewModel,
    onEditTask: (Task) -> Unit,
    onViewTask: (Task) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onViewTask(task) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.is_completed,
                onCheckedChange = { viewModel.toggleTask(task.id.toString(), it) },
                enabled = !task.is_completed,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF562BD7))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.is_completed) Color.Gray else Color(0xFF1D0C51),
                    textDecoration = if (task.is_completed) TextDecoration.LineThrough else null
                )

                task.due_date?.let {
                    val time = parseDueDateTime(it, "HH:mm")
                    if (time != null) {
                        Text(
                            text = time,
                            fontSize = 12.sp,
                            color = Color(0xFF7F7F7F)
                        )
                    }
                }
            }

            IconButton(
                onClick = { onEditTask(task) },
                enabled = !task.is_completed
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать")
            }
        }
    }
}
