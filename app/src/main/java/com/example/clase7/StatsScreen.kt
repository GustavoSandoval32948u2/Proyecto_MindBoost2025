package com.example.clase7

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clase7.ui.theme.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var profileHabits by remember { mutableStateOf<List<String>>(emptyList()) }
    var habitLogs by remember { mutableStateOf<Map<String, Map<String, Boolean>>>(emptyMap()) }
    var startDate by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val state_ave = stringResource(R.string.stats_average)

    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(600),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800),
        label = "alpha"
    )

    // 🔹 Carga segura de datos
    LaunchedEffect(uid) {
        uid?.let { userId ->
            try {
                val userRef = db.collection("users").document(userId)

                // Cargar hábitos del perfil
                val userDoc = userRef.get().await()
                profileHabits = userDoc.get("habits") as? List<String> ?: emptyList()

                // Cargar logs
                val snapshot = userRef.collection("habitLogs").get().await()
                val logs = mutableMapOf<String, Map<String, Boolean>>()
                snapshot.documents.forEach { doc ->
                    val data = doc.data
                        ?.filterValues { it is Boolean }
                        ?.mapValues { it.value as Boolean } ?: emptyMap()
                    logs[doc.id] = data
                }
                habitLogs = logs
                startDate = logs.keys.minOrNull()
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    // 🔹 Cálculos protegidos
    val totalCompleted = habitLogs.values.sumOf { it.values.count { v -> v } }
    val totalPossible = habitLogs.values.sumOf { it.size }
    val averageDaily = if (profileHabits.isNotEmpty() && habitLogs.isNotEmpty()) {
        totalCompleted.toFloat() / habitLogs.size
    } else 0f
    val completionRate = if (totalPossible > 0) {
        (totalCompleted.toFloat() / totalPossible * 100).toInt()
    } else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MindBoostBackground,
                        Color(0xFFE3EEFA)
                    )
                )
            )
    ) {
        if (isLoading) {
            // Pantalla de carga moderna
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(scale).alpha(alpha)
                ) {
                    CircularProgressIndicator(
                        color = MindBoostPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando estadísticas...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MindBoostText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
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
                
                // Encabezado con botón de regreso
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigate("home") { popUpTo("stats") { inclusive = true } } },
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.9f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MindBoostPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = stringResource(R.string.stats_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MindBoostPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Resumen principal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Ícono principal
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    MindBoostSecondary.copy(alpha = 0.1f),
                                    RoundedCornerShape(30.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Progreso",
                                tint = MindBoostSecondary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Tu Progreso",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MindBoostText,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "$completionRate% de hábitos completados",
                            style = MaterialTheme.typography.titleMedium,
                            color = MindBoostText.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjetas de estadísticas principales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Hábitos Completados
                    StatCard(
                        title = stringResource(R.string.stats_completed),
                        value = totalCompleted.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Total Posible
                    StatCard(
                        title = stringResource(R.string.stats_possible),
                        value = totalPossible.toString(),
                        icon = Icons.Default.TrackChanges,
                        color = MindBoostPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Promedio diario
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Promedio Diario",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%.1f", averageDaily),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Promedio",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Información de período
                startDate?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Período",
                                tint = MindBoostAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = "${stringResource(R.string.stats_from)}: $it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MindBoostText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Gráfica de barras mejorada
                if (profileHabits.isNotEmpty() && habitLogs.isNotEmpty()) {
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
                                text = "Progreso por Hábito",
                                style = MaterialTheme.typography.titleLarge,
                                color = MindBoostText,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            val entries = profileHabits.mapIndexed { index, habit ->
                                val habitTotal = habitLogs.values.count { it[habit] == true }
                                BarEntry(index.toFloat(), habitTotal.toFloat())
                            }.takeIf { it.isNotEmpty() } ?: emptyList()

                            if (entries.isNotEmpty()) {
                            val dataSet = BarDataSet(entries, stringResource(R.string.stats_bar_label)).apply {
                                colors = entries.map { entry ->
                                    val pct = if (habitLogs.isNotEmpty()) entry.y / habitLogs.size else 0f
                                    when {
                                        pct >= 0.8f -> Color(0xFF4CAF50).hashCode() // Verde éxito
                                        pct >= 0.6f -> Color(0xFF8BC34A).hashCode() // Verde claro
                                        pct >= 0.4f -> Color(0xFFFF9800).hashCode() // Naranja progreso
                                        pct >= 0.2f -> Color(0xFFFFC107).hashCode() // Amarillo mejora
                                        else -> Color(0xFFE57373).hashCode() // Rojo claro
                                    }
                                }
                                setDrawValues(true)
                                valueTextSize = 12f
                                valueTextColor = Color.White.hashCode()
                                valueFormatter = object : ValueFormatter() {
                                    override fun getFormattedValue(value: Float): String {
                                        return "${value.toInt()}"
                                    }
                                }
                                setDrawIcons(false)
                                barBorderWidth = 0f
                                setHighLightColor(Color(0xFF7B61FF).hashCode())
                            }

                                val barData = BarData(dataSet).apply { barWidth = 0.7f }

                                AndroidView(
                                    factory = { context ->
                                        BarChart(context).apply {
                                            data = barData
                                            description.isEnabled = false
                                            axisRight.isEnabled = false
                                            setTouchEnabled(true)
                                            setDragEnabled(true)
                                            setScaleEnabled(true)
                                            setPinchZoom(true)

                                            // Configuración del eje Y izquierdo
                                            axisLeft.apply {
                                                axisMinimum = 0f
                                                granularity = 1f
                                                setDrawGridLines(true)
                                                gridLineWidth = 1f
                                                gridColor = Color(0xFFE0E0E0).hashCode()
                                                textSize = 11f
                                                textColor = MindBoostText.hashCode()
                                                setDrawAxisLine(true)
                                                axisLineWidth = 2f
                                                axisLineColor = MindBoostText.hashCode()
                                                setDrawLabels(true)
                                                setLabelCount(5, true)
                                                
                                                // Línea de promedio mejorada
                                                val limitLine = LimitLine(averageDaily, "Promedio: ${String.format("%.1f", averageDaily)}")
                                                limitLine.lineWidth = 3f
                                                limitLine.lineColor = MindBoostAccent.hashCode()
                                                limitLine.textSize = 10f
                                                limitLine.textColor = MindBoostAccent.hashCode()
                                                limitLine.enableDashedLine(10f, 5f, 0f)
                                                addLimitLine(limitLine)
                                            }

                                            // Configuración del eje X
                                            xAxis.apply {
                                                granularity = 1f
                                                setDrawGridLines(false)
                                                textSize = 11f
                                                textColor = MindBoostText.hashCode()
                                                setDrawAxisLine(true)
                                                axisLineWidth = 2f
                                                axisLineColor = MindBoostText.hashCode()
                                                setDrawLabels(true)
                                                setLabelCount(profileHabits.size, false)
                                                valueFormatter = object : ValueFormatter() {
                                                    override fun getFormattedValue(value: Float): String {
                                                        val habitName = profileHabits.getOrNull(value.toInt()) ?: ""
                                                        return when {
                                                            habitName.length <= 6 -> habitName
                                                            habitName.length <= 10 -> habitName.take(8) + ".."
                                                            else -> habitName.take(6) + "..."
                                                        }
                                                    }
                                                }
                                                labelRotationAngle = -30f
                                                yOffset = 10f
                                            }

                                            // Configuración general del gráfico
                                            legend.isEnabled = false
                                            setFitBars(true)
                                            setBackgroundColor(Color.Transparent.hashCode())
                                            setDrawGridBackground(false)
                                            
                                            // Animación suave
                                            animateY(1000)
                                            animateX(1000)
                                            
                                            // Configuración de valores en las barras
                                            setDrawValueAboveBar(true)
                                            setMaxVisibleValueCount(10)
                                            
                                            invalidate()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(350.dp)
                                )
                                
                                // Leyenda de colores profesional
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF8F9FA)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Leyenda de Progreso",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MindBoostText,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            LegendItem(
                                                color = Color(0xFF4CAF50),
                                                label = "Excelente\n(≥80%)",
                                                modifier = Modifier.weight(1f)
                                            )
                                            LegendItem(
                                                color = Color(0xFF8BC34A),
                                                label = "Muy Bueno\n(≥60%)",
                                                modifier = Modifier.weight(1f)
                                            )
                                            LegendItem(
                                                color = Color(0xFFFF9800),
                                                label = "Bueno\n(≥40%)",
                                                modifier = Modifier.weight(1f)
                                            )
                                            LegendItem(
                                                color = Color(0xFFFFC107),
                                                label = "Regular\n(≥20%)",
                                                modifier = Modifier.weight(1f)
                                            )
                                            LegendItem(
                                                color = Color(0xFFE57373),
                                                label = "Mejorar\n(<20%)",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay datos suficientes para mostrar el gráfico",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MindBoostText.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Estado vacío
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Sin datos",
                                tint = MindBoostText.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No hay estadísticas disponibles",
                                style = MaterialTheme.typography.titleMedium,
                                color = MindBoostText,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Comienza a registrar tus hábitos para ver tu progreso",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MindBoostText.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Frase motivacional
                Text(
                    text = "Los números cuentan tu historia de crecimiento 📈",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MindBoostText.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MindBoostText,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodySmall.lineHeight
        )
    }
}