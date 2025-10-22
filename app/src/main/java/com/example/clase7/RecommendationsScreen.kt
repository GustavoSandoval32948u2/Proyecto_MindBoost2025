package com.example.clase7

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.clase7.ui.theme.*

@Composable
fun RecommendationsScreen(navController: NavController) {
    var isEarlyAccessEnabled by remember { mutableStateOf(false) }
    var isNotificationEnabled by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MindBoostBackground, Color(0xFFE3EEFA))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = { navController.navigate("home") { popUpTo("recommendations") { inclusive = true } } },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_home),
                            tint = MindBoostPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.recommendations_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = MindBoostPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // IA Badge / Próximamente
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MindBoostAccent.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = "AI",
                                tint = MindBoostAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recomendaciones impulsadas por IA",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MindBoostText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Próximamente",
                                style = MaterialTheme.typography.labelLarge,
                                color = MindBoostAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Estamos construyendo un motor de recomendaciones con IA que analizará tu progreso, hábitos y disponibilidad para sugerir acciones personalizadas que maximicen tu bienestar y constancia.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MindBoostText.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Qué hará la IA
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "¿Qué hará la IA?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MindBoostText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureRow(icon = Icons.Filled.Recommend, color = MindBoostPrimary, text = "Sugerencias de hábitos personalizadas según tu progreso y horas disponibles.")
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureRow(icon = Icons.Filled.AutoAwesome, color = MindBoostSecondary, text = "Rutinas inteligentes para metas semanales y recordatorios adaptativos.")
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureRow(icon = Icons.Filled.Bolt, color = Color(0xFFFF9800), text = "Insights accionables: qué hábito impacta más tu racha y bienestar.")
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureRow(icon = Icons.Filled.Lightbulb, color = MindBoostAccent, text = "Consejos motivacionales generados por IA basados en tu historial.")
                }
            }

            // Configuración de notificaciones y acceso anticipado
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.titleLarge,
                        color = MindBoostText,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Switch de Acceso Anticipado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acceso Anticipado",
                                style = MaterialTheme.typography.titleMedium,
                                color = MindBoostText,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Únete al beta privado cuando esté disponible",
                                style = MaterialTheme.typography.bodySmall,
                                color = MindBoostText.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = isEarlyAccessEnabled,
                            onCheckedChange = { isEarlyAccessEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MindBoostPrimary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Switch de Notificaciones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notificaciones",
                                style = MaterialTheme.typography.titleMedium,
                                color = MindBoostText,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Recibe alertas cuando la IA esté lista",
                                style = MaterialTheme.typography.bodySmall,
                                color = MindBoostText.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isNotificationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MindBoostSecondary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }
            }

            // Botón de acción principal
            Button(
                onClick = { 
                    // Aquí se podría implementar la lógica para guardar las preferencias
                    // Por ahora solo mostramos un mensaje de confirmación
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MindBoostAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = if (isEarlyAccessEnabled || isNotificationEnabled) {
                        "Guardar Preferencias"
                    } else {
                        "Notificarme cuando esté listo"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Vista previa mock de recomendaciones
        Text(
                text = "Vista previa (mock)",
                style = MaterialTheme.typography.titleMedium,
                color = MindBoostText.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
            )

            RecommendationMockCard(
                title = "Hoy: 20 min de Lectura",
                reason = "Tu concentración estuvo alta ayer y llevas 3 días sin leer",
                color = MindBoostSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            RecommendationMockCard(
                title = "Mañana: Dormir 7h",
                reason = "Tu racha mejora 32% cuando duermes más de 7 horas",
                color = MindBoostPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
                onClick = { navController.navigate("home") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MindBoostPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = stringResource(R.string.recommendations_back_home), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MindBoostText)
    }
}

@Composable
private fun RecommendationMockCard(title: String, reason: String, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MindBoostText)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = reason, style = MaterialTheme.typography.bodyMedium, color = MindBoostText.copy(alpha = 0.75f))
        }
    }
}
