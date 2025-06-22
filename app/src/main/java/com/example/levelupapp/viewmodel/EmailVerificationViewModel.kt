package com.example.levelupapp.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelupapp.data.RetrofitClient
import com.example.levelupapp.data.TokenManager
import kotlinx.coroutines.launch

class EmailVerificationViewModel(private val context: Context) : ViewModel() {
    val errorMessage = mutableStateOf<String?>(null)
    val successMessage = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)
    var token: String? = null

    fun verifyEmail(onSuccess: () -> Unit) {
        errorMessage.value = null
        successMessage.value = null

        if (token == null) {
            errorMessage.value = "Недействительная ссылка для подтверждения"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.confirmEmail(token!!)
                if (response.isSuccessful) {
                    successMessage.value = response.body()?.message ?: "Email успешно подтвержден. Теперь вы можете войти."
                    TokenManager.removeTokens(context) // Очищаем токены после подтверждения
                    onSuccess()
                } else {
                    val errorBody = response.body()
                    errorMessage.value = errorBody?.non_field_errors?.firstOrNull() ?: errorBody?.error ?: "Произошла ошибка при подтверждении email"
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}