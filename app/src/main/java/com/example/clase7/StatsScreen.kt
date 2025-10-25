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
import com.example.clase7.models.User
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(navController: NavController, user: User?) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var habitLogs by remember { mutableStateOf<Map<String, Map<String, Boolean>>>(emptyMap()) }
    var startDate by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    val scale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.8f, animationSpec = tween(600))
    val alpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, animationSpec = tween(800))

    val stats_average = stringResource(R.string.stats_average_label)

    LaunchedEffect(uid) {
        uid?.let { userId ->
            try {
                val userRef = db.collection("users").document(userId)
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
            } catch (e: Exception) { }
            finally { isLoading = false }
        }
    }

    val activeHabits = user?.habits?.keys ?: emptySet()
    val totalCompleted = habitLogs.values.sumOf { log -> log.filterKeys { it in activeHabits }.values.count { it } }
    val totalPossible = habitLogs.values.sumOf { log -> log.filterKeys { it in activeHabits }.size }
    val averageDaily = if (activeHabits.isNotEmpty() && habitLogs.isNotEmpty()) totalCompleted.toFloat() / habitLogs.size else 0f
    val completionRate = if (totalPossible > 0) (totalCompleted.toFloat() / totalPossible * 100).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(MindBoostBackground, Color(0xFFE3EEFA)))
            )
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale).alpha(alpha)) {
                    CircularProgressIndicator(color = MindBoostPrimary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.stats_loading),
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

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { navController.navigate("home") { popUpTo("stats") { inclusive = true } } },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.stats_back), tint = MindBoostPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(R.string.stats_title), style = MaterialTheme.typography.headlineLarge, color = MindBoostPrimary, fontWeight = FontWeight.Bold)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(60.dp).background(MindBoostSecondary.copy(alpha = 0.1f), RoundedCornerShape(30.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = stringResource(R.string.stats_progress), tint = MindBoostSecondary, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.stats_your_progress), style = MaterialTheme.typography.headlineMedium, color = MindBoostText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$completionRate% ${stringResource(R.string.stats_completed_habits)}", style = MaterialTheme.typography.titleMedium, color = MindBoostText.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(title = stringResource(R.string.stats_completed), value = totalCompleted.toString(), icon = Icons.Default.CheckCircle, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                    StatCard(title = stringResource(R.string.stats_possible), value = totalPossible.toString(), icon = Icons.Default.TrackChanges, color = MindBoostPrimary, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = stringResource(R.string.stats_daily_average), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = String.format("%.1f", averageDaily), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = stringResource(R.string.stats_average_icon), tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                startDate?.let {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = stringResource(R.string.stats_period_icon), tint = MindBoostAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "${stringResource(R.string.stats_from)}: $it", style = MaterialTheme.typography.bodyMedium, color = MindBoostText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (activeHabits.isNotEmpty() && habitLogs.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = stringResource(R.string.stats_bar_label), style = MaterialTheme.typography.titleLarge, color = MindBoostText, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                            val entries = activeHabits.mapIndexed { index, habit ->
                                val habitTotal = habitLogs.values.count { it[habit] == true }
                                BarEntry(index.toFloat(), habitTotal.toFloat())
                            }.takeIf { it.isNotEmpty() } ?: emptyList()

                            if (entries.isNotEmpty()) {
                                val dataSet = BarDataSet(entries, stringResource(R.string.stats_bar_label)).apply {
                                    colors = entries.map { entry ->
                                        val pct = if (habitLogs.isNotEmpty()) entry.y / habitLogs.size else 0f
                                        when {
                                            pct >= 0.8f -> Color(0xFF4CAF50).hashCode()
                                            pct >= 0.6f -> Color(0xFF8BC34A).hashCode()
                                            pct >= 0.4f -> Color(0xFFFF9800).hashCode()
                                            pct >= 0.2f -> Color(0xFFFFC107).hashCode()
                                            else -> Color(0xFFE57373).hashCode()
                                        }
                                    }
                                    setDrawValues(true)
                                    valueTextSize = 12f
                                    valueTextColor = Color.White.hashCode()
                                    valueFormatter = object : ValueFormatter() {
                                        override fun getFormattedValue(value: Float): String = "${value.toInt()}"
                                    }
                                    setDrawIcons(false)
                                    barBorderWidth = 0f
                                    setHighLightColor(Color(0xFF7B61FF).hashCode())
                                }

                                val barData = BarData(dataSet).apply { barWidth = 0.7f }

                                AndroidView(factory = { context ->
                                    BarChart(context).apply {
                                        data = barData
                                        description.isEnabled = false
                                        axisRight.isEnabled = false
                                        setTouchEnabled(true)
                                        setDragEnabled(true)
                                        setScaleEnabled(true)
                                        setPinchZoom(true)
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
                                            val limitLine = LimitLine(averageDaily, "${stats_average}: ${String.format("%.1f", averageDaily)}")
                                            limitLine.lineWidth = 3f
                                            limitLine.lineColor = MindBoostAccent.hashCode()
                                            limitLine.textSize = 10f
                                            limitLine.textColor = MindBoostAccent.hashCode()
                                            limitLine.enableDashedLine(10f, 5f, 0f)
                                            addLimitLine(limitLine)
                                        }
                                        xAxis.apply {
                                            granularity = 1f
                                            setDrawGridLines(false)
                                            textSize = 11f
                                            textColor = MindBoostText.hashCode()
                                            setDrawAxisLine(true)
                                            axisLineWidth = 2f
                                            axisLineColor = MindBoostText.hashCode()
                                            setDrawLabels(true)
                                            setLabelCount(activeHabits.size, false)
                                            valueFormatter = object : ValueFormatter() {
                                                override fun getFormattedValue(value: Float): String {
                                                    val habitName = activeHabits.elementAtOrNull(value.toInt()) ?: ""
                                                    return if (habitName.length <= 6) habitName else habitName.take(6) + "..."
                                                }
                                            }
                                            labelRotationAngle = -30f
                                            yOffset = 10f
                                        }
                                        legend.isEnabled = false
                                        setFitBars(true)
                                        setBackgroundColor(Color.Transparent.hashCode())
                                        setDrawGridBackground(false)
                                        animateY(1000)
                                        animateX(1000)
                                        setDrawValueAboveBar(true)
                                        setMaxVisibleValueCount(10)
                                        invalidate()
                                    }
                                }, modifier = Modifier.fillMaxWidth().height(350.dp))

                                Spacer(modifier = Modifier.height(16.dp))

                                // Leyenda
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = stringResource(R.string.stats_legend), style = MaterialTheme.typography.titleSmall, color = MindBoostText, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                            LegendItem(color = Color(0xFF4CAF50), label = stringResource(R.string.stats_legend_excellent), modifier = Modifier.weight(1f))
                                            LegendItem(color = Color(0xFF8BC34A), label = stringResource(R.string.stats_legend_very_good), modifier = Modifier.weight(1f))
                                            LegendItem(color = Color(0xFFFF9800), label = stringResource(R.string.stats_legend_good), modifier = Modifier.weight(1f))
                                            LegendItem(color = Color(0xFFFFC107), label = stringResource(R.string.stats_legend_regular), modifier = Modifier.weight(1f))
                                            LegendItem(color = Color(0xFFE57373), label = stringResource(R.string.stats_legend_improve), modifier = Modifier.weight(1f))
                                        }
                                    }
                                }

                            } else {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    Text(text = stringResource(R.string.stats_no_data), style = MaterialTheme.typography.bodyMedium, color = MindBoostText.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.BarChart, contentDescription = stringResource(R.string.stats_no_data_icon), tint = MindBoostText.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = stringResource(R.string.stats_no_stats), style = MaterialTheme.typography.titleMedium, color = MindBoostText, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = stringResource(R.string.stats_start_logging), style = MaterialTheme.typography.bodyMedium, color = MindBoostText.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(text = stringResource(R.string.stats_motivational), style = MaterialTheme.typography.bodyMedium, color = MindBoostText.copy(alpha = 0.6f), textAlign = TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(horizontal = 16.dp))
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