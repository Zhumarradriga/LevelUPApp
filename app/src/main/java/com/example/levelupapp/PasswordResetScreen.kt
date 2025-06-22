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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.levelupapp.viewmodel.PasswordResetViewModel

@Composable
fun PasswordResetScreen(navController: NavController) {
    val viewModel: PasswordResetViewModel = viewModel(
        factory = PasswordResetViewModelFactory(LocalContext.current)
    )

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
            fontSize = 50.sp,
            modifier = Modifier.padding(bottom = 60.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Сброс пароля",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = { navController.navigate("login") }) {
                Text(
                    text = "Вход →",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = viewModel.email.value,
            onValueChange = { viewModel.email.value = it },
            label = { Text("Электронная почта") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { viewModel.requestPasswordReset { navController.navigate("login") } },
            enabled = !viewModel.isLoading.value,
            modifier = Modifier
                .width(248.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (viewModel.isLoading.value) "Загрузка..." else "Отправить письмо",
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
    }
}