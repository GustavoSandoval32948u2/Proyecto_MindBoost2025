package com.example.clase7

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.clase7.models.User
import com.example.clase7.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.clase7.ui.theme.USERS_COLLECTION
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Variables de edición
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var availableHours by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<Int?>(null) }
    var ageError by remember { mutableStateOf<Int?>(null) }
    var hoursError by remember { mutableStateOf<Int?>(null) }

    val profile_update = stringResource(R.string.profile_updated)

    // Cargar datos del usuario desde Firestore
    LaunchedEffect(Unit) {
        userId?.let { uid ->
            db.collection(USERS_COLLECTION).document(uid).get()
                .addOnSuccessListener { document ->
                    val data = document.data
                    if (data != null) {
                        // Convertir habits a Map<String, String>
                        val habitsMap = when (val h = data["habits"]) {
                            is Map<*, *> -> h.mapKeys { it.key.toString() }
                                .mapValues { it.value.toString() }
                            is List<*> -> {
                                // Si es lista, la convertimos a mapa con valores por defecto
                                (h.filterIsInstance<String>()).associateWith { "0" }
                            }
                            else -> emptyMap()
                        }

                        user = User(
                            name = data["name"] as? String ?: "",
                            age = (data["age"] as? Long)?.toInt() ?: 0,
                            gender = data["gender"] as? String ?: "",
                            availableHours = data["availableHours"] as? String ?: "0",
                            habits = habitsMap,
                            profileHabits = (data["profileHabits"] as? List<String>) ?: emptyList(),
                            streakCount = (data["streakCount"] as? Long)?.toInt() ?: 0,
                            lastDailyDate = data["lastDailyDate"] as? String ?: ""
                        )

                        // Inicializar los campos editables
                        name = user!!.name
                        age = user!!.age.toString()
                        availableHours = user!!.availableHours
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                )
            )
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF43A047))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(R.string.profile_loading), fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Flecha de regreso y título
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigate("home") { popUpTo("profile") { inclusive = true } } }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_home),
                            tint = Color(0xFF0D47A1)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.profile_screen_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0D47A1)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔥 Racha actual
                user?.let { u ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA726)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.profile_streak_label),
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${u.streakCount} ${stringResource(R.string.profile_streak_days)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    // Campos editables
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.fields_name)) },
                        isError = nameError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { nameError?.let { Text(text = stringResource(it), color = Color.Red) } }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text(stringResource(R.string.fields_age)) },
                        isError = ageError != null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { ageError?.let { Text(text = stringResource(it), color = Color.Red) } }
                    )
                    Spacer(modifier = Modifier.height(12.dp))


                    Button(
                        onClick = {
                            val nameValid = validateName(name)
                            val ageValid = validateAge(age)
                            val hoursValid = validateHours(availableHours)

                            nameError = nameValid.second
                            ageError = ageValid.second
                            hoursError = hoursValid.second

                            if (nameValid.first && ageValid.first && hoursValid.first) {
                                userId?.let { uid ->
                                    val updatedUser = User(
                                        name = name,
                                        age = age.toInt(),
                                        gender = user!!.gender,
                                        availableHours = availableHours,
                                        habits = user!!.habits,
                                        streakCount = user!!.streakCount
                                    )
                                    db.collection(USERS_COLLECTION).document(uid)
                                        .set(updatedUser)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, profile_update, Toast.LENGTH_SHORT).show()
                                            user = updatedUser
                                            isEditing = false
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047), contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                    ) {
                        Text(text = stringResource(R.string.profile_save_button), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { isEditing = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                    ) {
                        Text(text = stringResource(R.string.profile_cancel_button), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                } else {
                    // Mostrar datos
                    user?.let { u ->
                        ProfileDataCard(label = stringResource(R.string.profile_screen_name), value = u.name)
                        ProfileDataCard(label = stringResource(R.string.profile_screen_age), value = u.age.toString())
                        ProfileDataCard(label = stringResource(R.string.profile_screen_gender), value = u.gender)
                        ProfileDataCard(label = stringResource(R.string.profile_screen_habits), value = u.habits.keys.joinToString(", "))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botones Editar y Cerrar sesión
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.profile_edit_button), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                auth.signOut()
                                navController.navigate("login") { popUpTo("profile") { inclusive = true } }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.profile_logout_button), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDataCard(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontWeight = FontWeight.SemiBold, color = Color(0xFF555555))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = value, fontWeight = FontWeight.Bold, color = Color(0xFF000000))
            }
        }
    }
}


@Composable
fun ProfileInfoSection(
    title: String,
    items: List<ProfileInfoItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MindBoostText,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            items.forEach { item ->
                ProfileInfoRow(item = item)
                if (item != items.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(item: ProfileInfoItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícono
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = item.color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Información
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MindBoostText.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.value,
                style = MaterialTheme.typography.titleMedium,
                color = MindBoostText,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

data class ProfileInfoItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val value: String,
    val color: Color
)
