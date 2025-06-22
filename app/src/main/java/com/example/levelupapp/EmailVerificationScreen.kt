package com.example.levelupapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.levelupapp.viewmodel.EmailVerificationViewModel

@Composable
fun EmailVerificationScreen(navController: NavController) {
    val viewModel: EmailVerificationViewModel = viewModel(
        factory = EmailVerificationViewModelFactory(LocalContext.current)
    )

    // Получение токена из аргументов навигации
    val token = navController.currentBackStackEntry?.arguments?.getString("token")
    if (token != null && viewModel.token == null) {
        viewModel.token = token
        viewModel.verifyEmail { navController.navigate("login") }
    } else if (token == null && viewModel.token == null) {
        // Если токен отсутствует, сразу показываем сообщение об ошибке
        viewModel.errorMessage.value = "Недействительная ссылка для подтверждения"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "LevelUp",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Text(
            text = "Подтверждение Email",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        viewModel.errorMessage.value?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .background(Color(0xFFFFEBEE))
                    .padding(10.dp),
                fontSize = 14.sp
            )
        }

        viewModel.successMessage.value?.let {
            Text(
                text = it,
                color = Color(0xFF4CAF50),
                modifier = Modifier
                    .background(Color(0xFFE8F5E9))
                    .padding(10.dp),
                fontSize = 14.sp
            )
        }

        TextButton(onClick = { navController.navigate("login") }) {
            Text(
                text = "Вернуться к входу →",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}