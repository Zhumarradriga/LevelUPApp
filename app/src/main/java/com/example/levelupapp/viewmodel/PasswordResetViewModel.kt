package com.example.levelupapp.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelupapp.data.PasswordResetRequest
import com.example.levelupapp.data.RetrofitClient
import com.example.levelupapp.data.TokenManager
import kotlinx.coroutines.launch

class PasswordResetViewModel(private val context: Context) : ViewModel() {
    val email = mutableStateOf("")
    val errorMessage = mutableStateOf<String?>(null)
    val successMessage = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)

    fun requestPasswordReset(onSuccess: () -> Unit) {
        errorMessage.value = null
        successMessage.value = null

        if (email.value.isBlank()) {
            errorMessage.value = "Пожалуйста, введите email"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.requestPasswordReset(
                    PasswordResetRequest(email.value.trim())
                )
                if (response.isSuccessful) {
                    successMessage.value = response.body()?.message ?: "Письмо для сброса пароля отправлено. Проверьте вашу почту."
                    email.value = ""
                    onSuccess()
                } else {
                    val errorBody = response.body()
                    errorMessage.value = errorBody?.non_field_errors?.firstOrNull() ?: errorBody?.error ?: "Произошла ошибка при отправке письма"
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}