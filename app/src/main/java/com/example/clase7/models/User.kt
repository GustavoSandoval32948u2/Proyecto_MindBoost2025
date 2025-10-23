package com.example.clase7.models

data class User(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val habits: Map<String, Any> = emptyMap(),
    val streakCount: Int = 0,
    val lastDailyDate: String = ""
)
