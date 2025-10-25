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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.clase7.models.User
import com.example.clase7.notifications.NotificationUtils
import com.example.clase7.screens.DailyLogScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        } else {
            scheduleNotificationsSafe()
        }

        setContent {
            Clase7Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreens()
                }
            }
        }
    }

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
                    getString(R.string.notifications_permission_message),
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scheduleNotificationsSafe()
        }
    }

    private fun scheduleNotificationsSafe() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                NotificationUtils.scheduleDailyNotifications(this)
            } else {
                Log.w("MainActivity", getString(R.string.exact_alarms_not_allowed))
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
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // --- Cargar user actual desde Firebase ---
    var currentUser by remember { mutableStateOf<User?>(null) }
    LaunchedEffect(userId) {
        userId?.let { uid ->
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    currentUser = User(
                        name = doc.getString("name") ?: "",
                        age = doc.getLong("age")?.toInt() ?: 0,
                        gender = doc.getString("gender") ?: "",
                        habits = doc.get("habits") as? Map<String, Any> ?: emptyMap(),
                        streakCount = doc.getLong("streakCount")?.toInt() ?: 0,
                        lastDailyDate = doc.getString("lastDailyDate") ?: ""
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_loading_user, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --- Determinar pantalla inicial ---
    LaunchedEffect(Unit) {
        startDestination = try {
            val currentFirebaseUser = auth.currentUser
            if (currentFirebaseUser == null) "login"
            else {
                currentFirebaseUser.reload().await()
                val doc = db.collection("users").document(currentFirebaseUser.uid).get().await()
                if (doc.exists()) "dailylog" else "createprofile"
            }
        } catch (e: Exception) {
            auth.signOut()
            Toast.makeText(
                context,
                e.message ?: context.getString(R.string.default_error),
                Toast.LENGTH_SHORT
            ).show()
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
                Text(
                    text = stringResource(R.string.loading_session),
                    fontWeight = FontWeight.Bold
                )
            }
        }

    } else {
        NavHost(navController = navController, startDestination = startDestination!!) {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("createprofile") { CreateProfileScreen(navController) }
            composable("home") { HomeScreen(navController, skipDailyRedirect) }
            composable("profile") { ProfileScreen(navController) }
            composable("createhabit") {
                CreateHabitScreen(navController = navController, userId = userId)
            }

            composable("stats") {
                currentUser?.let { user ->
                    StatsScreen(navController = navController, user = user)
                }
            }

            composable("recommendations") {
                RecommendationsScreen(navController = navController, userId = userId)
            }
            composable("medals") {
                MedalsScreen(navController = navController, userId = userId)
            }
            composable("dailylog") {
                DailyLogScreen(navController = navController, userId = userId)
            }
        }
    }
}
