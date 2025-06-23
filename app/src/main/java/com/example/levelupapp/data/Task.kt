package com.example.levelupapp.data

data class Task(
    val id: Int,
    val title: String,
    val description: String?,
    val due_date: String?,
    val is_completed: Boolean,
    val category_id: Int?,
    val stat_id: Int?,
    val priority: Int
)