package com.example.clase7

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clase7.models.Achievement
import com.example.clase7.ui.theme.*

@Composable
fun MedalsScreen(navController: NavController, userId: String?) {
    val context = LocalContext.current
    val medalsAlreadyUnlocked = stringResource(R.string.medals_already_unlocked)
    val medal_locked = stringResource(R.string.medals_locked_msg)

    val allAchievements = listOf(
        Achievement(
            id = "streak_3",
            titleRes = R.string.achv_streak_3_title,
            descRes = R.string.achv_streak_3_desc,
            lockedDrawableRes = R.drawable.medal_streak_locked,
            unlockedDrawableRes = R.drawable.medal_streak_unlocked
        ),
        Achievement(
            id = "streak_7",
            titleRes = R.string.achv_streak_7_title,
            descRes = R.string.achv_streak_7_desc,
            lockedDrawableRes = R.drawable.medal_streak_locked,
            unlockedDrawableRes = R.drawable.medal_week_unlocked
        ),
        Achievement(
            id = "avg_80",
            titleRes = R.string.achv_avg80_title,
            descRes = R.string.achv_avg80_desc,
            lockedDrawableRes = R.drawable.medal_streak_locked,
            unlockedDrawableRes = R.drawable.medal_avg_unlocked
        )
    )

    LaunchedEffect(userId) {
        userId?.let {
            AchievementManager.initialize(allAchievements, it)
        }
    }

    val unlocked = AchievementManager.unlocked
    val unlockedCount = unlocked.values.count { it }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MindBoostBackground, Color(0xFFE3EEFA))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .alpha(alpha)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigate("home") { popUpTo("medals") { inclusive = true } } },
                    modifier = Modifier.background(
                        Color.White.copy(alpha = 0.9f),
                        RoundedCornerShape(12.dp)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MindBoostPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(R.string.medals_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MindBoostPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFFF9800).copy(alpha = 0.1f), RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = stringResource(R.string.medals_icon_desc),
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.medals_collection),
                        style = MaterialTheme.typography.titleLarge,
                        color = MindBoostText,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$unlockedCount de ${allAchievements.size} ${stringResource(R.string.medals_unlocked)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MindBoostText.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = unlockedCount.toFloat() / allAchievements.size,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFFFF9800),
                        trackColor = Color(0xFFFF9800).copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(allAchievements) { ach ->
                    val isUnlocked = unlocked[ach.id] == true

                    MedalCard(
                        achievement = ach,
                        isUnlocked = isUnlocked,
                        onClick = {
                            if (isUnlocked) {
                                Toast.makeText(context, medalsAlreadyUnlocked, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, medal_locked, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    var unlockedAchievement by remember { mutableStateOf<Achievement?>(AchievementManager.lastUnlockedAchievement) }

    LaunchedEffect(AchievementManager.lastUnlockedAchievement) {
        unlockedAchievement = AchievementManager.lastUnlockedAchievement
    }

    unlockedAchievement?.let { ach ->
        AlertDialog(
            onDismissRequest = {
                AchievementManager.clearLastUnlocked()
                unlockedAchievement = null
            },
            title = {
                Text(
                    text = stringResource(R.string.achievement_unlocked_title),
                    fontWeight = FontWeight.Bold,
                    color = MindBoostPrimary
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = ach.unlockedDrawableRes),
                        contentDescription = stringResource(ach.titleRes),
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(ach.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MindBoostText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.achievement_unlocked_msg, stringResource(ach.titleRes)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MindBoostText.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AchievementManager.clearLastUnlocked()
                        unlockedAchievement = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MindBoostPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
fun MedalCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "press_scale"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color.White.copy(alpha = 0.9f) else Color(0xFFF5F5F5).copy(alpha = 0.9f)
        ),
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                val imageRes = if (isUnlocked) achievement.unlockedDrawableRes else achievement.lockedDrawableRes
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = stringResource(achievement.titleRes),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .alpha(if (isUnlocked) 1f else 0.4f)
                )

                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.medal_unlocked),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(achievement.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked) MindBoostText else MindBoostText.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(achievement.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked) MindBoostText.copy(alpha = 0.7f) else MindBoostText.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }
    }
}
