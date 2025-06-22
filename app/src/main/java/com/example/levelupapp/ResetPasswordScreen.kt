package com.example.levelupapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.levelupapp.viewmodel.ResetPasswordViewModel

@Composable
fun ResetPasswordScreen(navController: NavController) {
    val viewModel: ResetPasswordViewModel = viewModel(
        factory = ResetPasswordViewModelFactory(LocalContext.current)
    )

    // Получение токена из аргументов навигации
    val token = navController.previousBackStackEntry?.arguments?.getString("token") ?: ""
    if (token.isNotEmpty() && viewModel.token == null) {
        viewModel.token = token
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
            text = "Новый пароль",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        OutlinedTextField(
            value = viewModel.newPassword.value,
            onValueChange = { viewModel.newPassword.value = it },
            label = { Text("Новый пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = viewModel.confirmPassword.value,
            onValueChange = { viewModel.confirmPassword.value = it },
            label = { Text("Подтвердите пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.resetPassword { navController.navigate("login") } },
            enabled = !viewModel.isLoading.value,
            modifier = Modifier
                .width(248.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (viewModel.isLoading.value) "Загрузка..." else "Сохранить",
                fontSize = 20.sp
            )
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

        viewModel.successMessage.value?.let {
            Spacer(modifier = Modifier.height(10.dp))
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