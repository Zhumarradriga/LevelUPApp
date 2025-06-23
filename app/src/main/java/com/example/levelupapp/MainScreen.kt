package com.example.levelupapp

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
import com.example.levelupapp.data.CategoryRequest
import com.example.levelupapp.data.Stat
import com.example.levelupapp.data.StatRequest
import com.example.levelupapp.data.Task
import com.example.levelupapp.data.TaskRequest
import com.example.levelupapp.viewmodel.MainViewModel
import com.example.levelupapp.viewmodel.MainViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current)
    )
    var showTaskDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showStatDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedStat by remember { mutableStateOf<Stat?>(null) }

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
                                text = "Привет, ${viewModel.username.value}!",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { viewModel.progressPercentage.value / 100 },
                                    modifier = Modifier
                                        .width(100.dp)
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
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${viewModel.level.value}",
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
                            model = viewModel.avatarUrl.value ?: "",
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC9BEE6))
                                .clickable { showProfileDialog = true }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Открыть боковое меню или навигацию */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.isLoading.value) {
                CircularProgressIndicator()
            } else {
                DailySummary(viewModel) { showTaskDialog = true }
                InProgressSection(viewModel) { task -> selectedTask = task; showTaskDialog = true }
                TaskCategoriesSection(viewModel) { category -> selectedCategory = category; showCategoryDialog = true }
            }

            viewModel.errorMessage.value?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .background(Color(0xFFFFEBEE))
                        .padding(10.dp),
                    fontSize = 14.sp
                )
            }
        }
    }

    if (showTaskDialog) {
        TaskDialog(
            task = selectedTask,
            viewModel = viewModel,
            onDismiss = { showTaskDialog = false; selectedTask = null }
        )
    }

    if (showCategoryDialog) {
        CategoryDialog(
            category = selectedCategory,
            viewModel = viewModel,
            onDismiss = { showCategoryDialog = false; selectedCategory = null }
        )
    }

    if (showProfileDialog) {
        ProfileDialog(
            viewModel = viewModel,
            onDismiss = { showProfileDialog = false },
            navController = navController,
            onAddStat = { showStatDialog = true; selectedStat = null },
                    onSelectStat = { stat -> selectedStat = stat }
        )
    }

    if (showStatDialog) {
        StatDialog(
            stat = selectedStat,
            viewModel = viewModel,
            onDismiss = { showStatDialog = false; selectedStat = null }
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = { /* Already on MainScreen */ },
            icon = { Icon(Icons.Default.Add, contentDescription = "Главная") },
            label = { Text("Главная") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("tasks") },
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
fun DailySummary(viewModel: MainViewModel, onAddTask: () -> Unit) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    val formattedDate = today.format(formatter)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Скорее начинай выполнять сегодняшние задачи!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Задачи на $formattedDate",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddTask,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF562BD7))
                ) {
                    Text("Добавить задачу на сегодня", color = Color.White)
                }
            }
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF562BD7).copy(alpha = viewModel.progressPercentage.value / 100),
                                Color(0xFF6038E9).copy(alpha = 0.31f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${viewModel.progressPercentage.value.toInt()}%",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InProgressSection(viewModel: MainViewModel, onEditTask: (Task) -> Unit) {
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
                text = "В процессе",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${viewModel.todayTasksCount.value}",
                fontSize = 16.sp,
                modifier = Modifier
                    .background(Color(0xFF562BD7), CircleShape)
                    .padding(8.dp),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        val todayTasks = viewModel.tasks.value?.filter { it.due_date?.startsWith(LocalDate.now().toString()) == true } ?: emptyList()
        if (todayTasks.isEmpty()) {
            Text(
                text = "Задач нет",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn {
                items(todayTasks) { task ->
                    TaskItem(task, viewModel, onEditTask)
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, viewModel: MainViewModel, onEditTask: (Task) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { /* Открыть детали задачи */ },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.is_completed,
                onCheckedChange = { viewModel.toggleTask(task.id, it) },
                enabled = !task.is_completed
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = task.title,
                fontSize = 16.sp,
                color = if (task.is_completed) Color.Gray else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textDecoration = if (task.is_completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )
            IconButton(onClick = { onEditTask(task) }, enabled = !task.is_completed) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun TaskCategoriesSection(viewModel: MainViewModel, onEditCategory: (Category?) -> Unit) {
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
                text = "Категории задач",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${viewModel.categories.value?.size ?: 0}",
                fontSize = 16.sp,
                modifier = Modifier
                    .background(Color(0xFF562BD7), CircleShape)
                    .padding(8.dp),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            val uncategorizedTasks = viewModel.tasks.value?.filter { it.category_id == null } ?: emptyList()
            if (uncategorizedTasks.isNotEmpty()) {
                item {
                    CategoryItem(
                        category = Category(id = 0, name = "Без категории", icon = "fas fa-folder"),
                        taskCount = uncategorizedTasks.size,
                        viewModel = viewModel,
                        onEditCategory = { /* Нет редактирования для "Без категории" */ }
                    )
                }
            }
            items(viewModel.categories.value ?: emptyList()) { category ->
                val taskCount = viewModel.tasks.value?.count { task -> task.category_id == category.id } ?: 0
                CategoryItem(category, taskCount, viewModel, onEditCategory)
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onEditCategory(null) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Добавить категорию",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, taskCount: Int, viewModel: MainViewModel, onEditCategory: (Category?) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { /* Открыть задачи категории */ },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add, // Замените на иконку из FontAwesome
                contentDescription = category.name,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$taskCount задач",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (category.id != 0) { // Исключаем "Без категории"
                IconButton(onClick = { onEditCategory(category) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { viewModel.deleteCategory(category.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun TaskDialog(
    task: Task? = null,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var dueDate by remember { mutableStateOf(task?.due_date ?: LocalDate.now().toString()) }
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
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
                DropdownMenu(
                    items = viewModel.categories.value?.associate { it.id.toString() to it.name } ?: emptyMap(),
                    selectedItem = categoryId,
                    onItemSelected = { categoryId = it },
                    label = "Категория"
                )
                Spacer(modifier = Modifier.height(8.dp))
                DropdownMenu(
                    items = viewModel.stats.value?.filter { it.is_default && it.name != "Current Level" }
                        ?.associate { it.id.toString() to it.name } ?: emptyMap(),
                    selectedItem = statId,
                    onItemSelected = { statId = it },
                    label = "Характеристика"
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Дедлайн") },
                    modifier = Modifier.fillMaxWidth()
                    // Здесь можно добавить DatePicker
                )
                Spacer(modifier = Modifier.height(8.dp))
                DropdownMenu(
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
                            viewModel.updateTask(task.id, taskRequest) { onDismiss() }
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
fun CategoryDialog(
    category: Category? = null,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "") }

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
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название категории") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Иконка (FontAwesome класс)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            viewModel.errorMessage.value = "Введите название категории"
                            return@Button
                        }
                        val categoryRequest = CategoryRequest(name, icon.takeIf { it.isNotBlank() })
                        if (category != null) {
                            viewModel.updateCategory(category.id, categoryRequest) { onDismiss() }
                        } else {
                            viewModel.addCategory(categoryRequest) { onDismiss() }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF562BD7))
                ) {
                    Text(
                        text = if (category != null) "Сохранить" else "Создать",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileDialog(
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
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                AsyncImage(
                    model = viewModel.avatarUrl.value ?: "",
                    contentDescription = "Avatar",
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
                        StatItem(
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
fun StatItem(
    stat: Stat,
    viewModel: MainViewModel,
    onEditStat: (Stat) -> Unit,
    onSelectStat: (Stat) -> Unit
) {
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${stat.name} ${stat.value}",
                fontSize = 14.sp,
                color = getContrastColor(stat.color ?: "#562BD7"),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onSelectStat(stat); onEditStat(stat) }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = getContrastColor(stat.color ?: "#562BD7")
                )
            }
            IconButton(onClick = { viewModel.deleteStat(stat.id) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = getContrastColor(stat.color ?: "#562BD7")
                )
            }
        }
    }
}

@Composable
fun StatDialog(
    stat: Stat? = null,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(stat?.name ?: "") }
    var description by remember { mutableStateOf(stat?.description ?: "") }
    var color by remember { mutableStateOf(stat?.color ?: "#CD0000") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF6F1FE),
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
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text(
                    text = if (stat != null) "Редактировать характеристику" else "Добавить характеристику",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Цвет (HEX)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            viewModel.errorMessage.value = "Введите название характеристики"
                            return@Button
                        }
                        if (color.isBlank()) {
                            viewModel.errorMessage.value = "Выберите цвет"
                            return@Button
                        }
                        val statRequest = StatRequest(
                            name = name,
                            description = description.takeIf { it.isNotBlank() } ?: name,
                            color = color
                        )
                        if (stat != null) {
                            viewModel.updateStat(stat.id, statRequest) { onDismiss() }
                        } else {
                            viewModel.addStat(statRequest) { onDismiss() }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF562BD7))
                ) {
                    Text(
                        text = if (stat != null) "Сохранить" else "Добавить",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DropdownMenu(
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
                    Icon(Icons.Default.Add, contentDescription = "Expand")
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { (key, value) ->
                DropdownMenuItem(
                    text = { Text(value) },
                    onClick = {
                        onItemSelected(key)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun getContrastColor(hexColor: String): Color {
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