package com.example.levelupapp.data

data class Stat(
    val id: Int,
    val name: String,
    val description: String?,
    val value: Int,
    val is_default: Boolean=true,
    val color: String?=null
)