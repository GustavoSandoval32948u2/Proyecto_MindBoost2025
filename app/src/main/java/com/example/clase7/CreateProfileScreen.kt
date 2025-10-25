package com.example.clase7

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.clase7.ui.theme.USERS_COLLECTION

import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.draw.alpha
import com.example.clase7.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    val titleText = stringResource(R.string.create_profile_screen_title)
    val continueButtonText = stringResource(R.string.create_profile_screen_continue)
    val dataSavedMsg = stringResource(R.string.create_profile_screen_saved)
    val habits_limit = stringResource(R.string.create_profile_screen_habits_limit)

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var expandedGender by remember { mutableStateOf(false) }
    val genderOptions = listOf(
        stringResource(R.string.gender_male),
        stringResource(R.string.gender_female),
        stringResource(R.string.gender_other)
    )

    var nameError: Int? by remember { mutableStateOf(null) }
    var ageError: Int? by remember { mutableStateOf(null) }
    var habitsError: Int? by remember { mutableStateOf(null) }

    val habitsList = listOf(
        stringResource(R.string.habit_sleep),
        stringResource(R.string.habit_exercise),
        stringResource(R.string.habit_study),
        stringResource(R.string.habit_reading)
    )
    val selectedHabits = remember { mutableStateListOf<String>() }
    val habitDurations = remember { mutableStateMapOf<String, String>() } // valor ingresado por usuario
    var showMaxHabitsToast by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    val habitUnits = mapOf(
        stringResource(R.string.habit_sleep) to "Horas",
        stringResource(R.string.habit_exercise) to "Minutos",
        stringResource(R.string.habit_study) to "Minutos",
        stringResource(R.string.habit_reading) to "Minutos"
    )

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    val animatedAlpha by animateFloatAsState(if (isVisible) 1f else 0f, tween(800))
    val animatedOffsetY by animateDpAsState(if (isVisible) 0.dp else 50.dp, tween(800))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.icon_register_back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E88E5))
            )
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF43A047))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.create_profile_screen_loading),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)))
                    )
                    .padding(horizontal = 24.dp)
                    .alpha(animatedAlpha)
                    .offset(y = animatedOffsetY),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.fields_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                            isError = nameError != null,
                            supportingText = { nameError?.let { Text(text = stringResource(it), color = Color.Red) } }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text(stringResource(R.string.fields_age)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                            isError = ageError != null,
                            supportingText = { ageError?.let { Text(text = stringResource(it), color = Color.Red) } }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandedGender,
                            onExpandedChange = { expandedGender = !expandedGender }
                        ) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.fields_gender)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedGender,
                                onDismissRequest = { expandedGender = false }
                            ) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            gender = option
                                            expandedGender = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = stringResource(R.string.create_profile_screen_select_habits),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E88E5)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        habitsList.forEach { habit ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .toggleable(
                                            value = selectedHabits.contains(habit),
                                            onValueChange = {
                                                if (it) {
                                                    if (selectedHabits.size < 2) selectedHabits.add(habit)
                                                    else showMaxHabitsToast = true
                                                } else {
                                                    selectedHabits.remove(habit)
                                                    habitDurations.remove(habit)
                                                }
                                            }
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedHabits.contains(habit),
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF43A047),
                                            uncheckedColor = Color.Gray
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = habit, fontSize = 16.sp)
                                }

                                if (selectedHabits.contains(habit)) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = habitDurations[habit] ?: "",
                                        onValueChange = { habitDurations[habit] = it },
                                        label = {
                                            Text("Tiempo (${habitUnits[habit] ?: "Minutos"})")
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                        }

                        if (showMaxHabitsToast) {
                            LaunchedEffect(showMaxHabitsToast) {
                                Toast.makeText(
                                    context,
                                    habits_limit,
                                    Toast.LENGTH_SHORT
                                ).show()
                                showMaxHabitsToast = false
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val nameValid = validateName(name)
                                val ageValid = validateAge(age)
                                val habitsValid = validateHabits(selectedHabits)
                                val hoursValid = validateHabitDurations(habitDurations, habitUnits)
                                val genderValid = gender.isNotEmpty()

                                nameError = nameValid.second
                                ageError = ageValid.second
                                habitsError = habitsValid.second

                                if (nameValid.first && ageValid.first && habitsValid.first && genderValid && hoursValid.first) {
                                    isLoading = true
                                    userId?.let { uid ->

                                        val convertedDurations: Map<String, String> = habitDurations.map { (habit, value) ->
                                            val number = value.toIntOrNull() ?: 0
                                            val inMinutes = if (habitUnits[habit] == "Horas") number * 60 else number
                                            habit to inMinutes.toString()
                                        }.toMap()

                                        val habitsMap = convertedDurations.map { (habit, duration) ->
                                            habit to mapOf(
                                                "first" to habit,
                                                "minutes" to duration
                                            )
                                        }.toMap()

                                        val userData = mapOf(
                                            "name" to name,
                                            "age" to age.toInt(),
                                            "gender" to gender,
                                            "availableHours" to "0",
                                            "habits" to habitsMap,
                                            "streakCount" to 0,
                                            "lastDailyDate" to ""
                                        )


                                        db.collection(USERS_COLLECTION).document(uid)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, dataSavedMsg, Toast.LENGTH_SHORT).show()
                                                navController.navigate("home") {
                                                    popUpTo("createprofile") { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                                            }

                                    }
                                } else {
                                    val combinedError = listOfNotNull(
                                        nameValid.second?.let { context.getString(it) },
                                        ageValid.second?.let { context.getString(it) },
                                        habitsValid.second?.let { context.getString(it) },
                                        if (!genderValid) context.getString(R.string.create_profile_screen_gender_required) else null,
                                        if (!hoursValid.first) context.getString(hoursValid.second ?: R.string.create_profile_screen_habit_duration_invalid) else null
                                    ).joinToString(" ")
                                    Toast.makeText(context, combinedError, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF43A047),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                        ) {
                            Text(
                                text = continueButtonText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
