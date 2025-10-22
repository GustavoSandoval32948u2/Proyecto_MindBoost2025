package com.example.clase7

import androidx.compose.runtime.mutableStateMapOf
import com.example.clase7.models.Achievement
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AchievementManager {

    private val firestore = FirebaseFirestore.getInstance()

    // Estado global de logros desbloqueados
    val unlocked = mutableStateMapOf<String, Boolean>()

    // Último logro desbloqueado automáticamente
    var lastUnlockedAchievement: Achievement? = null
        private set

    // Lista de logros y usuario actual
    private lateinit var achievementsList: List<Achievement>
    private lateinit var currentUserId: String

    /**
     * Inicializa la lista de logros y carga estado desde Firestore
     */
    fun initialize(achievements: List<Achievement>, userId: String) {
        achievementsList = achievements
        currentUserId = userId
        loadUnlockedAchievements()
    }

    private fun loadUnlockedAchievements() {
        CoroutineScope(Dispatchers.IO).launch {
            achievementsList.forEach { ach ->
                val doc = firestore.collection("users")
                    .document(currentUserId)
                    .collection("achievements")
                    .document(ach.id)
                    .get()
                    .await()
                unlocked[ach.id] = doc.exists()
            }
        }
    }

    /**
     * Comprueba y actualiza logros según condiciones
     */
    fun checkAchievements(
        streakDays: Int = 0,
        averageScore: Float = 0f
    ) {
        // ✅ Asumimos que currentUserId ya está inicializado
        CoroutineScope(Dispatchers.IO).launch {
            achievementsList.forEach { ach ->
                when (ach.id) {
                    "streak_3" -> updateAchievement(ach, streakDays >= 3)
                    "streak_7" -> updateAchievement(ach, streakDays >= 7)
                    "avg_80"   -> updateAchievement(ach, averageScore >= 80f)
                }
            }
        }
    }

    private suspend fun updateAchievement(ach: Achievement, shouldUnlock: Boolean) {
        val currentlyUnlocked = unlocked[ach.id] == true
        val userRef = firestore.collection("users")
            .document(currentUserId)
            .collection("achievements")
            .document(ach.id)

        if (shouldUnlock && !currentlyUnlocked) {
            // Desbloquea el logro
            userRef.set(mapOf("unlockedAt" to System.currentTimeMillis())).await()
            unlocked[ach.id] = true
            lastUnlockedAchievement = ach
        } else if (!shouldUnlock && currentlyUnlocked) {
            // Bloquea el logro si antes estaba desbloqueado
            userRef.delete().await()
            unlocked[ach.id] = false
            if (lastUnlockedAchievement?.id == ach.id) lastUnlockedAchievement = null
        }
    }

    fun clearLastUnlocked() {
        lastUnlockedAchievement = null
    }

    fun getAchievementById(id: String): Achievement? {
        return achievementsList.find { it.id == id }
    }
}


