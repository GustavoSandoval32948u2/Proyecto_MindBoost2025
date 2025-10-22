package com.example.clase7.models

data class Achievement(
    val id: String,
    val titleRes: Int,
    val descRes: Int,
    val lockedDrawableRes: Int,
    val unlockedDrawableRes: Int,
    val initiallyUnlocked: Boolean = false
)