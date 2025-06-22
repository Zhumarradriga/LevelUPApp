package com.example.levelupapp.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.levelupapp.data.GenericResponse
import com.example.levelupapp.data.LoginRequest
import com.example.levelupapp.data.RetrofitClient
import com.example.levelupapp.data.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

class LoginViewModel(private val context: Context) : ViewModel() {
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val errorMessage = mutableStateOf<String?>(null)
    val successMessage = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)
    val shouldShowResend = mutableStateOf(false)

    fun login(onSuccess: () -> Unit) {
        errorMessage.value = null
        successMessage.value = null
        shouldShowResend.value = false

        if (email.value.isBlank() || password.value.isBlank()) {
            errorMessage.value = "Пожалуйста, заполните все поля"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(
                        email = email.value.trim(),
                        password = password.value.trim()
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.access != null && body.refresh != null) {
                        TokenManager.saveTokens(context, body.access, body.refresh)
                        successMessage.value = "Вход выполнен успешно"
                        email.value = ""
                        password.value = ""
                        onSuccess()
                    } else {
                        throw Exception("No tokens in response")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = errorBody?.let {
                        Gson().fromJson(it, GenericResponse::class.java)
                    }
                    val errorMsg = errorResponse?.non_field_errors?.firstOrNull()
                        ?: errorResponse?.error
                        ?: errorResponse?.detail
                        ?: errorResponse?.message
                        ?: "Произошла ошибка при входе"
                    errorMessage.value = errorMsg
                    if (errorMsg.contains("Email не подтвержден") || errorMsg.contains("Аккаунт не активирован")) {
                        shouldShowResend.value = true
                    }
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun resendConfirmation(onSuccess: () -> Unit) {
        errorMessage.value = null
        successMessage.value = null

        if (email.value.isBlank()) {
            errorMessage.value = "Email не указан"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.resendConfirmation(
                    LoginRequest(
                        email = email.value.trim(),
                        password = ""
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    successMessage.value = body?.message ?: "Письмо для подтверждения отправлено"
                    shouldShowResend.value = false
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = errorBody?.let {
                        Gson().fromJson(it, GenericResponse::class.java)
                    }
                    val errorMsg = errorResponse?.message
                        ?: errorResponse?.error
                        ?: errorResponse?.detail
                        ?: errorResponse?.non_field_errors?.firstOrNull()
                        ?: "Ошибка при отправке письма"
                    errorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка при отправке письма: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}