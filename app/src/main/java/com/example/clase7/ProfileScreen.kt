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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<Int?>(null) }
    var ageError by remember { mutableStateOf<Int?>(null) }

    val profile_update = stringResource(R.string.profile_updated)

    LaunchedEffect(Unit) {
        userId?.let { uid ->
            db.collection(USERS_COLLECTION).document(uid).get()
                .addOnSuccessListener { document ->
                    val data = document.data
                    if (data != null) {
                        val habitsMap = when (val h = data["habits"]) {
                            is Map<*, *> -> h.mapKeys { it.key.toString() }.mapValues { it.value.toString() }
                            is List<*> -> (h.filterIsInstance<String>()).associateWith { "0" }
                            else -> emptyMap()
                        }

                        user = User(
                            name = data["name"] as? String ?: "",
                            age = (data["age"] as? Long)?.toInt() ?: 0,
                            gender = data["gender"] as? String ?: "",
                            habits = habitsMap,
                            streakCount = (data["streakCount"] as? Long)?.toInt() ?: 0,
                            lastDailyDate = data["lastDailyDate"] as? String ?: ""
                        )

                        name = user!!.name
                        age = user!!.age.toString()
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, it.message ?: "Error", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFF4CAF50)))
            )
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigate("home") { popUpTo("profile") { inclusive = true } } }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_home),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.profile_screen_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = stringResource(R.string.profile_screen_icon_desc),
                    tint = Color.White,
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                user?.let { u ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA726)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
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

                            nameError = nameValid.second
                            ageError = ageValid.second

                            if (nameValid.first && ageValid.first) {
                                userId?.let { uid ->
                                    val updatedUser = User(
                                        name = name,
                                        age = age.toInt(),
                                        gender = user!!.gender,
                                        habits = user!!.habits,
                                        streakCount = user!!.streakCount,
                                        lastDailyDate = user!!.lastDailyDate
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
                        modifier = Modifier.fillMaxWidth().height(55.dp)
                    ) {
                        Text(text = stringResource(R.string.profile_save_button), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { isEditing = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().height(55.dp)
                    ) {
                        Text(text = stringResource(R.string.profile_cancel_button), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                } else {
                    user?.let { u ->
                        val profileItems = listOf(
                            ProfileInfoItem(Icons.Default.Person, stringResource(R.string.profile_screen_name), u.name, Color(0xFF4CAF50)),
                            ProfileInfoItem(Icons.Default.Cake, stringResource(R.string.profile_screen_age), u.age.toString(), Color(0xFF9C27B0)),
                            ProfileInfoItem(Icons.Default.Male, stringResource(R.string.profile_screen_gender), u.gender, Color(0xFFFF9800)),
                            ProfileInfoItem(Icons.Default.Email, stringResource(R.string.profile_screen_email), auth.currentUser?.email ?: "", Color(0xFF03A9F4)),
                            ProfileInfoItem(Icons.Default.Today, stringResource(R.string.profile_screen_lastdaily), u.lastDailyDate, Color(0xFF009688)),
                            ProfileInfoItem(Icons.Default.Star, stringResource(R.string.profile_screen_habits), u.habits.keys.joinToString(", "), Color(0xFFFF5722))
                        )
                        ProfileInfoSection(stringResource(R.string.profile_screen_info_title), profileItems)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🔹 Botones Editar y Cerrar sesión
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
fun ProfileInfoSection(title: String, items: List<ProfileInfoItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .heightIn(min = 350.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MindBoostText,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            items.forEach { item ->
                ProfileInfoRow(item = item)
                if (item != items.last()) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoRow(item: ProfileInfoItem) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = item.icon, contentDescription = item.label, tint = item.color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.label, style = MaterialTheme.typography.bodyMedium, color = MindBoostText.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = item.value, style = MaterialTheme.typography.titleMedium, color = MindBoostText, fontWeight = FontWeight.SemiBold)
        }
    }
}

data class ProfileInfoItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val color: Color
)
