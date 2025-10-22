package com.example.clase7

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.clase7.ui.theme.Clase7Theme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.clase7.notifications.NotificationUtils
import com.example.clase7.screens.DailyLogScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔔 Pedir permiso en Android 13+ o programar notificaciones de manera segura
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        } else {
            scheduleNotificationsSafe()
        }

        // 🧭 Cargar interfaz
        setContent {
            Clase7Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreens()
                }
            }
        }
    }

    // --- Permiso de notificaciones Android 13+ ---
    private fun requestNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                scheduleNotificationsSafe()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Toast.makeText(
                    this,
                    "Habilita las notificaciones para recibir recordatorios diarios",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // --- Resultado de solicitud de permiso ---
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scheduleNotificationsSafe()
        }
    }

    // --- Programar notificaciones de forma segura ---
    private fun scheduleNotificationsSafe() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                NotificationUtils.scheduleDailyNotifications(this)
            } else {
                Log.w("MainActivity", "Exact alarms no permitidas")
            }
        } else {
            NotificationUtils.scheduleDailyNotifications(this)
        }
    }
}

@Composable
fun MainScreens() {
    val navController = rememberNavController()
    val auth = Firebase.auth
    val db = Firebase.firestore
    val context = LocalContext.current
    var startDestination by remember { mutableStateOf<String?>(null) }
    val skipDailyRedirect = remember { mutableStateOf(false) }

    // --- Determinar pantalla inicial ---
    LaunchedEffect(Unit) {
        startDestination = try {
            val currentUser = auth.currentUser
            if (currentUser == null) "login"
            else {
                currentUser.reload().await()
                val doc = db.collection("users").document(currentUser.uid).get().await()
                if (doc.exists()) "dailylog" else "createprofile"
            }
        } catch (e: Exception) {
            auth.signOut()
            Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
            "login"
        }
    }

    // --- Pantalla de carga mientras se determina destino ---
    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF43A047))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Iniciando sesión...", fontWeight = FontWeight.Bold)
            }
        }

    } else {
        // --- Navegación principal ---
        NavHost(navController = navController, startDestination = startDestination!!) {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("createprofile") { CreateProfileScreen(navController) }
            composable("home") { HomeScreen(navController, skipDailyRedirect) }
            composable("profile") { ProfileScreen(navController) }
            composable("createhabit") {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                CreateHabitScreen(navController = navController, userId = uid)
            }
            composable("stats") { StatsScreen(navController) }
            composable("recommendations") { RecommendationsScreen(navController) }
            composable("medals") {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                MedalsScreen(navController = navController, userId = uid)
            }
            composable("dailylog") {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                DailyLogScreen(
                    navController = navController,
                    userId = uid
                )
            }

        }
    }
}