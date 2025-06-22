package com.example.levelupapp.data

data class ResetPasswordRequest(
    val new_password: String,
    val token: String
)