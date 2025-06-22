package com.example.levelupapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.levelupapp.viewmodel.PasswordResetViewModel

class PasswordResetViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordResetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PasswordResetViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}