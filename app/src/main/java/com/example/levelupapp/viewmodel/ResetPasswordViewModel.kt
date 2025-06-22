package com.example.levelupapp.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelupapp.data.ResetPasswordRequest
import com.example.levelupapp.data.RetrofitClient
import com.example.levelupapp.data.TokenManager
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val context: Context) : ViewModel() {
    val newPassword = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val errorMessage = mutableStateOf<String?>(null)
    val successMessage = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)
    var token: String? = null

    fun resetPassword(onSuccess: () -> Unit) {
        errorMessage.value = null
        successMessage.value = null

        if (newPassword.value != confirmPassword.value) {
            errorMessage.value = "Пароли не совпадают"
            return
        }
        if (newPassword.value.length < 8) {
            errorMessage.value = "Пароль должен содержать минимум 8 символов"
            return
        }
        if (token == null) {
            errorMessage.value = "Недействительная ссылка для сброса пароля"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.confirmPasswordReset(
                    ResetPasswordRequest(newPassword.value.trim(), token!!)
                )
                if (response.isSuccessful) {
                    successMessage.value = response.body()?.message ?: "Пароль успешно сброшен. Теперь вы можете войти."
                    TokenManager.removeTokens(context) // Очищаем токены после сброса
                    onSuccess()
                } else {
                    val errorBody = response.body()
                    errorMessage.value = errorBody?.non_field_errors?.firstOrNull() ?: errorBody?.error ?: "Произошла ошибка при сбросе пароля"
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}

