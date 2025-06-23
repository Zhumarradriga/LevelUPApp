package com.example.levelupapp.data

data class TaskRequest(
    val title: String,
    val description: String? = null,
    val due_date: String? = null,
    val category_id: Int? = null,
    val stat_id: Int? = null,
    val priority: Int = 50,
    val is_completed: Boolean? = null
)