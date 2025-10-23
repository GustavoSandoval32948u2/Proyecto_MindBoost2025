package com.example.clase7.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clase7.R
import com.example.clase7.AchievementManager
import com.example.clase7.models.Achievement
import com.example.clase7.ui.theme.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyLogScreen(
    navController: NavController,
    userId: String?
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val formattedDate = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()) }

    var loading by remember { mutableStateOf(true) }
    var habits by remember { mutableStateOf<List<String>>(emptyList()) }
    val checked = remember { mutableStateMapOf<String, Boolean>() }
    val durations = remember { mutableStateMapOf<String, String>() }
    var notes by remember { mutableStateOf("") }

    var showSavedToast by remember { mutableStateOf(false) }
    var showErrorToast by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val userProfileHabits = remember { mutableStateListOf<String>() }
    var showDaily by remember { mutableStateOf(true) }

    val toastSaved = stringResource(R.string.dailylog_toast_saved)
    val toastError = stringResource(R.string.dailylog_toast_error)

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    val scale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.8f, animationSpec = tween(600))
    val alpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, animationSpec = tween(800))

    // AchievementManager
    LaunchedEffect(userId) {
        if (userId != null) {
            AchievementManager.initialize(
                achievements = listOf(
                    Achievement(
                        id = "streak_3",
                        titleRes = R.string.achievement_streak3_title,
                        descRes = R.string.achievement_streak3_desc,
                        lockedDrawableRes = R.drawable.medal_streak_locked,
                        unlockedDrawableRes = R.drawable.medal_streak_unlocked
                    ),
                    Achievement(
                        id = "streak_7",
                        titleRes = R.string.achievement_streak7_title,
                        descRes = R.string.achievement_streak7_desc,
                        lockedDrawableRes = R.drawable.medal_streak_locked,
                        unlockedDrawableRes = R.drawable.medal_week_unlocked
                    )
                ),
                userId = userId
            )
        }
    }

    // Cargar hábitos
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val todayDoc = db.collection("users").document(userId)
                    .collection("habitLogs").document(today).get().await()

                if (todayDoc.exists()) {
                    navController.navigate("home") {
                        popUpTo("dailylog") { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    val userDoc = db.collection("users").document(userId).get().await()
                    if (userDoc.exists()) {
                        val habitsMapRaw = userDoc.get("habits") as? Map<String, Map<String, Any?>> ?: emptyMap()
                        val allHabitsFromFirebase = habitsMapRaw.keys.toList()

                        userProfileHabits.clear()
                        userProfileHabits.addAll(allHabitsFromFirebase)

                        habits = allHabitsFromFirebase.distinct()

                        allHabitsFromFirebase.forEach { habit ->
                            checked[habit] = false
                            val duration = habitsMapRaw[habit]?.get("second")?.toString() ?: "0"
                            durations[habit] = duration
                        }
                    }
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

    if (showDaily) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(MindBoostBackground, Color(0xFFE3EEFA))))
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .scale(scale)
                    .alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Fecha
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MindBoostPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.dailylog_header, formattedDate),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MindBoostPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = stringResource(R.string.dailylog_myhabits), style = MaterialTheme.typography.titleLarge, color = MindBoostText, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))

                habits.forEach { habit ->
                    var localDuration by remember { mutableStateOf("") } // estado local para el TextField

                    HabitCard(
                        habit = habit,
                        isChecked = checked[habit] ?: false,
                        duration = localDuration,
                        onCheckedChange = { checked[habit] = it },
                        onDurationChange = { localDuration = it },
                        unit = if (habit == "Sleep" ) "horas" else "minutos"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = MindBoostAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.dailylog_notes_title), style = MaterialTheme.typography.titleMedium, color = MindBoostText, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text(text = stringResource(R.string.dailylog_notes_placeholder), color = MindBoostText.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MindBoostAccent,
                                unfocusedBorderColor = MindBoostText.copy(alpha = 0.3f),
                                focusedTextColor = MindBoostText,
                                unfocusedTextColor = MindBoostText
                            ),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // BOTONES
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Guardar
                    Button(
                        onClick = {
                            if (checked.values.none { it }) { showErrorToast = true; return@Button }
                            userId?.let { uid ->
                                coroutineScope.launch {
                                    try {
                                        val dataToSave = mutableMapOf<String, Any>()
                                        checked.forEach { (habit, done) ->
                                            dataToSave[habit] = done
                                            val enteredDuration = durations[habit]?.toLongOrNull() ?: 0L
                                            val durationInSeconds = when (habit) {
                                                "Sleep" -> enteredDuration * 3600      // Convertir horas a segundos
                                                else -> enteredDuration * 60           // Convertir minutos a segundos
                                            }
                                            dataToSave["${habit}_duration"] = durationInSeconds
                                        }
                                        dataToSave["notes"] = notes
                                        dataToSave["timestamp"] = FieldValue.serverTimestamp()
                                        dataToSave["skipped"] = false

                                        db.collection("users").document(uid)
                                            .collection("habitLogs").document(today)
                                            .set(dataToSave)
                                            .await()

                                        val doc = db.collection("users").document(uid).get().await()
                                        val currentStreak = doc.getLong("streakCount") ?: 0L
                                        val lastDate = doc.getString("lastDailyDate") ?: ""
                                        val newStreak = if (lastDate == today) currentStreak else currentStreak + 1

                                        db.collection("users").document(uid)
                                            .update(
                                                mapOf(
                                                    "streakCount" to newStreak,
                                                    "lastDailyDate" to today
                                                )
                                            ).await()

                                        AchievementManager.checkAchievements(streakDays = newStreak.toInt())
                                        delay(200)

                                        showSuccessAnimation = true
                                        showDaily = false
                                        delay(1500)
                                        navController.navigate("stats") {
                                            popUpTo("dailylog") { inclusive = true }
                                        }

                                    } catch (e: Exception) {
                                        showErrorToast = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MindBoostSecondary, contentColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(text = stringResource(R.string.dailylog_btn_save), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Omitir
                    Button(
                        onClick = {
                            userId?.let { uid ->
                                coroutineScope.launch {
                                    try {
                                        db.collection("users").document(uid)
                                            .update(mapOf("streakCount" to 0, "lastDailyDate" to today))
                                            .await()
                                        showSavedToast = true
                                        showDaily = false
                                        delay(300)
                                        navController.navigate("home") {
                                            popUpTo("dailylog") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } catch (e: Exception) {
                                        showErrorToast = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0B0B0), contentColor = MindBoostText),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(text = stringResource(R.string.dailylog_btn_skip), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = stringResource(R.string.dailylog_motivation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MindBoostText.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    if (showSavedToast) {
                        LaunchedEffect(showSavedToast) {
                            Toast.makeText(context, toastSaved, Toast.LENGTH_SHORT).show()
                            showSavedToast = false
                        }
                    }
                    if (showErrorToast) {
                        LaunchedEffect(showErrorToast) {
                            Toast.makeText(context, toastError, Toast.LENGTH_SHORT).show()
                            showErrorToast = false
                        }
                    }
                    if (showSuccessAnimation) {
                        LaunchedEffect(showSuccessAnimation) {
                            Toast.makeText(context, toastSaved, Toast.LENGTH_SHORT).show()
                            showSuccessAnimation = false
                        }
                    }

                    AchievementManager.lastUnlockedAchievement?.let { ach ->
                        AlertDialog(
                            onDismissRequest = { AchievementManager.clearLastUnlocked() },
                            title = { Text(text = stringResource(R.string.achievement_unlocked_title), fontWeight = FontWeight.Bold) },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = stringResource(ach.titleRes), tint = MindBoostAccent, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = stringResource(ach.titleRes), fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = stringResource(R.string.achievement_unlocked_msg, stringResource(ach.titleRes)))
                                }
                            },
                            confirmButton = {
                                Button(onClick = { AchievementManager.clearLastUnlocked() }, colors = ButtonDefaults.buttonColors(containerColor = MindBoostPrimary)) {
                                    Text(text = stringResource(R.string.ok))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun HabitCard(
    habit: String,
    isChecked: Boolean,
    duration: String,
    onCheckedChange: (Boolean) -> Unit,
    onDurationChange: (String) -> Unit,
    unit: String
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "press_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onCheckedChange(!isChecked)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono del hábito
                Icon(
                    imageVector = getHabitIcon(habit),
                    contentDescription = habit,
                    tint = getHabitColor(habit),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Nombre del hábito
                Text(
                    text = habit,
                    style = MaterialTheme.typography.titleMedium,
                    color = MindBoostText,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                // Checkbox
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MindBoostPrimary,
                        uncheckedColor = MindBoostText.copy(alpha = 0.5f)
                    )
                )
            }

            // Campo de duración solo si está marcado
            if (isChecked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = stringResource(R.string.dailylog_duration_label),
                        tint = MindBoostSecondary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = duration, // siempre vacío al iniciar
                        onValueChange = onDurationChange,
                        placeholder = {
                            Text(
                                text = unit, // ← aquí pasas "horas" o "minutos"
                                color = MindBoostText.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MindBoostSecondary,
                            unfocusedBorderColor = MindBoostText.copy(alpha = 0.3f),
                            focusedTextColor = MindBoostText,
                            unfocusedTextColor = MindBoostText
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(120.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        singleLine = true
                    )

                }
            }
        }
    }
}

// Funciones auxiliares para iconos y colores
fun getHabitIcon(habit: String): ImageVector {
    return when (habit.lowercase()) {
        "sueño", "dormir", "sleep" -> Icons.Default.Nightlight
        "estudiar", "estudio", "study" -> Icons.Default.School
        "leer", "lectura", "reading" -> Icons.Default.MenuBook
        "ejercicio", "exercise" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Circle
    }
}

fun getHabitColor(habit: String): Color {
    return when (habit.lowercase()) {
        "sueño", "dormir", "sleep" -> Color(0xFF42A5F5)
        "estudiar", "estudio", "study" -> Color(0xFF66BB6A)
        "leer", "lectura", "reading" -> Color(0xFFFFCA28)
        "ejercicio", "exercise" -> Color(0xFFEF5350)
        else -> MindBoostText
    }
}
