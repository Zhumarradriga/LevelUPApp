package com.example.levelupapp.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelupapp.data.RegisterRequest
import com.example.levelupapp.data.RetrofitClient
import kotlinx.coroutines.launch

class RegisterViewModel(private val context: Context) : ViewModel() {
    val username = mutableStateOf("")
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val errorMessage = mutableStateOf<String?>(null)
    val successMessage = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)

    fun register(onSuccess: () -> Unit) {
        errorMessage.value = null
        successMessage.value = null

        if (password.value != confirmPassword.value) {
            errorMessage.value = "Пароли не совпадают"
            return
        }
        if (password.value.length < 8) {
            errorMessage.value = "Пароль должен содержать минимум 8 символов"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(
                        username = username.value.trim(),
                        email = email.value.trim(),
                        password = password.value.trim()
                    )
                )
                if (response.isSuccessful) {
                    successMessage.value = response.body()?.message ?: "Регистрация успешна. Пожалуйста, подтвердите ваш email."
                    username.value = ""
                    email.value = ""
                    password.value = ""
                    confirmPassword.value = ""
                    onSuccess()
                } else {
                    val errorBody = response.body()
                    errorMessage.value = errorBody?.non_field_errors?.firstOrNull()
                        ?: errorBody?.error
                                ?: errorBody?.detail
                                ?: "Произошла ошибка при регистрации"
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}