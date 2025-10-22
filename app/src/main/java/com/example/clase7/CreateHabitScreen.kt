package com.example.clase7

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.clase7.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun CreateHabitScreen(navController: NavController, userId: String?) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var userProfileHabits by remember { mutableStateOf<List<String>>(emptyList()) } // Hábitos del perfil
    var userSavedHabits: Map<String, String> by remember { mutableStateOf(emptyMap()) } // Hábitos guardados
    val selectedHabits = remember { mutableStateListOf<String>() } // Selección actual
    val unitSelections = remember { mutableStateMapOf<String, String>() } // "min" o "h"
    val durationInputs = remember { mutableStateMapOf<String, String>() } // duración ingresada

    var showLimitToast by remember { mutableStateOf(false) }
    var showMinToast by remember { mutableStateOf(false) }

    val maxHabits = 4

    // Strings
    val create_max = stringResource(R.string.create_habit_max_limit)
    val create_saved = stringResource(R.string.create_habit_saved)
    val create_min = stringResource(R.string.create_habit_min_limit)
    val duration_placeholder = stringResource(R.string.create_habit_duration_placeholder)
    val unit_min = stringResource(R.string.create_habit_unit_min)
    val unit_hr = stringResource(R.string.create_habit_unit_hr)
    val save_button_text = stringResource(R.string.create_habit_save_button)
    val cancel_button_text = stringResource(R.string.create_habit_cancel_button)
    val error_loading = stringResource(R.string.error_loading_habits)
    val error_saving = stringResource(R.string.error_saving_habits)

    // 🔄 Cargar hábitos del usuario
    LaunchedEffect(userId) {
        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val profileHabitsList = doc.get("profileHabits") as? List<String> ?: emptyList()
                        userProfileHabits = profileHabitsList

                        val habitsMapRaw = doc.get("habits") as? Map<*, *> ?: emptyMap<Any?, Any?>()
                        val parsedHabits = habitsMapRaw.mapNotNull { (key, value) ->
                            val name = key as? String ?: return@mapNotNull null
                            val duration = value?.toString() ?: "0"
                            name to duration
                        }.toMap()

                        userSavedHabits = parsedHabits
                        selectedHabits.clear()
                        selectedHabits.addAll(parsedHabits.keys)

                        parsedHabits.forEach { (habit, value) ->
                            val minutes = value.toIntOrNull() ?: 0
                            durationInputs[habit] = value
                            unitSelections[habit] = if (minutes >= 60) "h" else "min"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, e.message ?: error_loading, Toast.LENGTH_SHORT).show()
                }
        }
    }

    val allHabits = listOf(
        stringResource(R.string.habit_sleep),
        stringResource(R.string.habit_study),
        stringResource(R.string.habit_reading),
        stringResource(R.string.habit_exercise),
        stringResource(R.string.habit_meditation),
        stringResource(R.string.habit_learning),
        stringResource(R.string.habit_journaling),
        stringResource(R.string.habit_coding),
        stringResource(R.string.habit_drinking_water)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.create_habit_screen_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0D47A1),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(R.string.create_habit_select_max),
            color = Color.Gray,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        allHabits.forEach { habit ->
            val alreadySelected = selectedHabits.contains(habit)
            val isOriginal = userProfileHabits.contains(habit)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        when {
                            alreadySelected && !isOriginal -> {
                                if (selectedHabits.size > 1) {
                                    selectedHabits.remove(habit)
                                    durationInputs.remove(habit)
                                    unitSelections.remove(habit)
                                } else showMinToast = true
                            }
                            !alreadySelected -> {
                                if (selectedHabits.size < maxHabits) {
                                    selectedHabits.add(habit)
                                    durationInputs[habit] = ""
                                    unitSelections[habit] = "min"
                                } else showLimitToast = true
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (alreadySelected) Color(0xFF1976D2) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = habit,
                        color = if (alreadySelected) Color.White else Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (alreadySelected) {
                        OutlinedTextField(
                            value = durationInputs[habit] ?: "",
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) durationInputs[habit] = input
                            },
                            placeholder = { Text(duration_placeholder) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.White,
                                unfocusedIndicatorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            RadioButton(
                                selected = unitSelections[habit] == "min",
                                onClick = { unitSelections[habit] = "min" },
                                colors = RadioButtonDefaults.colors(selectedColor = Color.Yellow)
                            )
                            Text(unit_min, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(
                                selected = unitSelections[habit] == "h",
                                onClick = { unitSelections[habit] = "h" },
                                colors = RadioButtonDefaults.colors(selectedColor = Color.Yellow)
                            )
                            Text(unit_hr, color = Color.White)
                        }
                    }
                }
            }
        }

        if (showLimitToast) {
            LaunchedEffect(showLimitToast) {
                Toast.makeText(context, create_max, Toast.LENGTH_SHORT).show()
                showLimitToast = false
            }
        }

        if (showMinToast) {
            LaunchedEffect(showMinToast) {
                Toast.makeText(context, create_min, Toast.LENGTH_SHORT).show()
                showMinToast = false
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val hasValidHabits = selectedHabits.any { durationInputs[it]?.toIntOrNull() != null && (durationInputs[it]?.toInt() ?: 0) > 0 }

        Button(
            onClick = {
                userId?.let { uid ->
                    val toSave = selectedHabits.associateWith { habit ->
                        val raw = durationInputs[habit]?.toIntOrNull() ?: 0
                        val unit = unitSelections[habit]
                        if (unit == "h") (raw * 60).toString() else raw.toString()
                    }
                    db.collection("users").document(uid)
                        .update("habits", toSave)
                        .addOnSuccessListener {
                            Toast.makeText(context, create_saved, Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("createhabit") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, e.message ?: error_saving, Toast.LENGTH_SHORT).show()
                        }
                }
            },
            enabled = hasValidHabits,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasValidHabits) Color(0xFF43A047) else Color.Gray,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text(text = save_button_text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("home") {
                    popUpTo("createhabit") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text(text = cancel_button_text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
