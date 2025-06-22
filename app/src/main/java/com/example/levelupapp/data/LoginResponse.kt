package com.example.levelupapp.data

data class LoginResponse(
    val access: String? = null,
    val refresh: String? = null,
    val message: String? = null,
    val error: String? = null,
    val detail: String? = null
)