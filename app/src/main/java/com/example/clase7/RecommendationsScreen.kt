package com.example.clase7

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.clase7.ui.theme.MindBoostBackground
import com.example.clase7.ui.theme.MindBoostPrimary
import com.example.clase7.ui.theme.MindBoostSecondary
import com.example.clase7.ui.theme.MindBoostText
import com.google.firebase.firestore.FirebaseFirestore
import data.DeepSeekService
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun RecommendationsScreen(navController: NavController, userId: String?) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var habits by remember { mutableStateOf<List<String>>(emptyList()) }
    var recommendation by remember { mutableStateOf("") }
    var selectedHabit by remember { mutableStateOf<String?>(null) }
    var showErrorToast by remember { mutableStateOf(false) }
    var generalRecommendation by remember { mutableStateOf("") }
    var generalLoading by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }


    val titleRecommendations = stringResource(R.string.recommendations_title)
    val subtitleRecommendations = stringResource(R.string.recommendations_subtitle)
    val noActiveHabits = stringResource(R.string.recommendations_no_active_habits)
    val loadingText = stringResource(R.string.recommendations_loading)
    val backHome = stringResource(R.string.recommendations_back_home)
    val cardTitle = stringResource(R.string.recommendations_card_title)
    val loadError = stringResource(R.string.recommendations_load_error)
    val aiPromptTemplate = stringResource(R.string.recommendations_ai_prompt)
    val generalPrompt = stringResource(R.string.recommendations_general_prompt)
    val recomendations_loading = stringResource(R.string.recommendations_loading)
    val loading_error = stringResource(R.string.recommendations_load_error)
    val recommendationsIconDescription = stringResource(R.string.recommendations_icon_description)

    val habitDisplayMap = mapOf(
        "Sleep" to stringResource(R.string.habit_sleep),
        "Study" to stringResource(R.string.habit_study),
        "Reading" to stringResource(R.string.habit_reading),
        "Exercise" to stringResource(R.string.habit_exercise),
        "Meditation" to stringResource(R.string.habit_meditation),
        "Learning" to stringResource(R.string.habit_learning),
        "Journaling" to stringResource(R.string.habit_journaling),
        "Coding" to stringResource(R.string.habit_coding),
        "DrinkingWater" to stringResource(R.string.habit_drinking_water)
    )

    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                if (userDoc.exists()) {
                    userName = userDoc.getString("name") ?: context.getString(R.string.default_user_name)
                    val savedHabitsMapRaw = userDoc.get("habits") as? Map<String, Map<String, Any?>> ?: emptyMap()
                    val savedHabitsTechnical = savedHabitsMapRaw.keys.toList()
                    habits = savedHabitsTechnical.map { habitDisplayMap[it] ?: it }.distinct()
                }
            } catch (e: Exception) {
                showErrorToast = true
            } finally {
                loading = false
            }
        } else {
            loading = false
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MindBoostPrimary)
        }
        return
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(MindBoostBackground, Color(0xFFE3EEFA))))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MindBoostPrimary.copy(alpha = 0.1f), RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiObjects,
                    contentDescription = recommendationsIconDescription,
                    tint = MindBoostPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "🎯 $titleRecommendations",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MindBoostPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitleRecommendations.replace("%1\$s", userName),
                style = MaterialTheme.typography.titleLarge,
                color = MindBoostText.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (habits.isEmpty()) {
                Text(
                    text = noActiveHabits,
                    color = MindBoostText.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    habits.forEach { habit ->
                        Button(
                            onClick = {
                                selectedHabit = habit
                                recommendation = loadingText
                                scope.launch {
                                    try {
                                        val userDoc = db.collection("users").document(userId!!).get().await()
                                        val age = userDoc.getLong("age") ?: 0
                                        val streak = userDoc.getLong("streakCount") ?: 0L
                                        val habitsMap = userDoc.get("habits") as? Map<String, Map<String, Any?>> ?: emptyMap()
                                        val habitLogsSnap = db.collection("users").document(userId).collection("habitLogs").get().await()
                                        val habitLogs = habitLogsSnap.documents.filter { it.data?.containsKey(habit) == true }

                                        var totalDone = 0
                                        var notesStr = ""
                                        habitLogs.forEach { doc ->
                                            val done = doc.getBoolean(habit) ?: false
                                            if (done) totalDone++
                                            notesStr += doc.getString("notes") ?: ""
                                            notesStr += ". "
                                        }

                                        val durationSec = habitsMap[habit]?.get("minutes")?.toString() ?: "0"
                                        val durationHours = (durationSec.toIntOrNull() ?: 0) / 3600

                                        val prompt = aiPromptTemplate
                                            .replace("{age}", age.toString())
                                            .replace("{streak}", streak.toString())
                                            .replace("{habit}", habit)
                                            .replace("{done}", totalDone.toString())
                                            .replace("{notes}", notesStr)
                                            .replace("{duration}", "$durationSec minutos ($durationHours horas)")

                                        recommendation = DeepSeekService.getRecommendation(prompt)

                                    } catch (e: Exception) {
                                        recommendation = loadError
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MindBoostSecondary),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = habit,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedHabit != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = cardTitle.replace("{habit}", selectedHabit!!),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MindBoostText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MindBoostText.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    generalLoading = true
                    generalError = false
                    generalRecommendation = recomendations_loading
                    scope.launch {
                        try {
                            generalRecommendation = DeepSeekService.getRecommendation(generalPrompt)
                        } catch (e: Exception) {
                            generalRecommendation = loadError
                            generalError = true
                        } finally {
                            generalLoading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MindBoostSecondary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.recommendations_general_button),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            if (generalRecommendation.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = stringResource(R.string.recommendations_general_button),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MindBoostText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (generalLoading) recomendations_loading else generalRecommendation,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (generalError) Color.Red else MindBoostText.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate("home") },
                colors = ButtonDefaults.buttonColors(containerColor = MindBoostPrimary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(
                    text = backHome,
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showErrorToast) {
            LaunchedEffect(showErrorToast) {
                Toast.makeText(context, loadError, Toast.LENGTH_SHORT).show()
                showErrorToast = false
            }
        }
    }
}
