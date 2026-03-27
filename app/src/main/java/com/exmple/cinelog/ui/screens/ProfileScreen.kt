package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exmple.cinelog.ui.theme.glassCard
import com.exmple.cinelog.ui.theme.glassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val userScore = uiState.userProfile?.xp ?: 0
    val badgeGrid = uiState.badges
    val totalFilmsLogged = uiState.totalFilms
    val totalHours = uiState.totalHours
    val streak = uiState.userProfile?.currentStreak ?: 0

    val level = uiState.userProfile?.level ?: 1
    // XP progress within current level tier
    val currentLevelXpFloor = when(level) { 1 -> 0; 2 -> 500; 3 -> 1000; 4 -> 2000; else -> 4000 }
    val nextLevelXp = when(level) { 1 -> 500; 2 -> 1000; 3 -> 2000; 4 -> 4000; else -> 10000 }
    val progressInLevel = if (nextLevelXp > currentLevelXpFloor) {
        ((userScore - currentLevelXpFloor).toFloat() / (nextLevelXp - currentLevelXpFloor)).coerceIn(0f, 1f)
    } else 1f
    
    val levelName = uiState.levelName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 100.dp)
    ) {
        // Hero: Level & Progress
        Text("Current Status", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Level $level: ", style = MaterialTheme.typography.displaySmall)
            Text(levelName, style = MaterialTheme.typography.displaySmall.copy(fontStyle = FontStyle.Italic), color = MaterialTheme.colorScheme.primaryContainer)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("XP PROGRESSION", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp))
            Text("$userScore / $nextLevelXp XP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primaryContainer, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progressInLevel },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primaryContainer,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        // Stat Cards (Right Column analogous)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileStatCard(title = "Total Films", value = totalFilmsLogged.toString(), modifier = Modifier.weight(1f))
            ProfileStatCard(title = "Hours Watched", value = totalHours.toString(), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileStatCard(title = "Current Streak", value = "$streak 🔥", modifier = Modifier.weight(1f))
            ProfileStatCard(title = "Avg. Rating", value = String.format("%.1f", uiState.avgRating), modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(48.dp))

        // Badge Showcase
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Badge Showcase", style = MaterialTheme.typography.labelSmall)
            Text("VIEW GALLERY", style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primaryContainer)
        }
        Spacer(modifier = Modifier.height(20.dp))
        
        // 3 columns grid
        val badges = badgeGrid.take(8)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (row in badges.chunked(3)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { badge ->
                        ProfileBadgeCard(badge.name, badge.iconRes, badge.isUnlocked, modifier = Modifier.weight(1f))
                    }
                    // Fill remaining slots
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .glassCard(cornerRadius = 16.dp)
            .padding(24.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(value, style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun ProfileBadgeCard(name: String, emoji: String, isUnlocked: Boolean, modifier: Modifier = Modifier) {
    val borderAlpha = if (isUnlocked) 0.12f else 0.03f
    val textColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .then(
                if (isUnlocked) Modifier.glassCard(cornerRadius = 14.dp, borderAlpha = borderAlpha)
                else Modifier.glassSurface(cornerRadius = 14.dp, alpha = 0.2f)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = textColor, maxLines = 1)
        if (!isUnlocked) {
            Text("LOCKED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        }
    }
}
