package com.example.levelupapp.data

data class StatRequest(
    val name: String,
    val description: String? = null,
    val value: Int = 0,
    val is_default: Boolean = true,
    val color: String? = null
)