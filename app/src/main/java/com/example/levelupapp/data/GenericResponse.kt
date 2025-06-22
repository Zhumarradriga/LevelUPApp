package com.example.levelupapp.data

data class GenericResponse(
    val message: String? = null,
    val error: String? = null,
    val detail: String? = null,
    val non_field_errors: List<String>? = null
)