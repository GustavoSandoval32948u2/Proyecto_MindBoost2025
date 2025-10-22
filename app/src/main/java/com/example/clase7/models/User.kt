package com.example.clase7.models

data class User(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val habits: Map<String, String> = emptyMap(), // duración como texto
    val profileHabits: List<String> = emptyList(),
    val streakCount: Int = 0,
    val lastDailyDate: String = "",
    val availableHours: String = "0" // opcional
)
