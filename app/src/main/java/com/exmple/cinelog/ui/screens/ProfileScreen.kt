package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exmple.cinelog.data.local.entity.Badge
import com.exmple.cinelog.ui.theme.PlayfairDisplayFont

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userScore = uiState.userProfile?.xp ?: 0
    val badgeGrid = uiState.badges
    val totalFilmsLogged = uiState.totalFilms
    val totalHours = uiState.totalHours
    val streak = uiState.userProfile?.currentStreak ?: 0

    val level = uiState.userProfile?.level ?: 1
    val currentLevelXpFloor = when(level) { 1 -> 0; 2 -> 500; 3 -> 1000; 4 -> 2000; else -> 4000 }
    val nextLevelXp = when(level) { 1 -> 500; 2 -> 1000; 3 -> 2000; 4 -> 4000; else -> 10000 }
    val progressInLevel = if (nextLevelXp > currentLevelXpFloor) {
        ((userScore - currentLevelXpFloor).toFloat() / (nextLevelXp - currentLevelXpFloor)).coerceIn(0f, 1f)
    } else 1f
    
    val levelName = uiState.levelName

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Deepest black/grey
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 100.dp)
    ) {
        // --- 1. Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Triple stacked mini rect icon for logo
                Icon(Icons.Filled.Movie, contentDescription = "Logo", tint = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CINELOG", style = MaterialTheme.typography.titleMedium.copy(fontFamily = PlayfairDisplayFont, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.primaryContainer)
            }
            // XP Pill
            Box(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$userScore XP • $streak 🔥",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }

        // --- 2. Hero Section ---
        Text("CURRENT STATUS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Level $level: ", style = MaterialTheme.typography.displaySmall.copy(fontFamily = PlayfairDisplayFont, fontWeight = FontWeight.Bold, fontSize = 36.sp), color = MaterialTheme.colorScheme.onSurface)
            Text(levelName, style = MaterialTheme.typography.displaySmall.copy(fontFamily = PlayfairDisplayFont, fontStyle = FontStyle.Italic, fontSize = 36.sp), color = MaterialTheme.colorScheme.primaryContainer)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- 3. Streak Card ---
        val streakLevelName = when {
            streak >= 30 -> "Cinema Legend"
            streak >= 7 -> "Cinema Buff"
            streak >= 3 -> "Cinema Devotee"
            streak > 0 -> "Cinema Enthusiast"
            else -> "New Spectator"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.LocalFireDepartment, contentDescription = "Streak", tint = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("${streak}-DAY STREAK", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(2.dp))
                Text(streakLevelName, style = MaterialTheme.typography.titleMedium.copy(fontFamily = PlayfairDisplayFont, fontWeight = FontWeight.Bold, fontSize = 18.sp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. XP Progression ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("XP PROGRESSION", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Text(
                "$userScore / $nextLevelXp XP", 
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), 
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progressInLevel },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(0.dp)),
            color = MaterialTheme.colorScheme.primaryContainer,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        // --- 5. Genre Passport ---
        GenrePassportCard(uiState.topGenres)

        Spacer(modifier = Modifier.height(24.dp))

        // --- 6. Total Stats Blocks ---
        ProfileStatBlock(title = "TOTAL FILMS", value = totalFilmsLogged.toString())
        Spacer(modifier = Modifier.height(16.dp))
        ProfileStatBlock(title = "HOURS WATCHED", value = totalHours.toString())
        Spacer(modifier = Modifier.height(16.dp))
        ProfileStatBlock(title = "AVG. RATING", value = String.format("%.1f", uiState.avgRating), suffixIcon = Icons.Filled.Star)

        Spacer(modifier = Modifier.height(48.dp))

        // --- 7. Badge Showcase ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("BADGE SHOWCASE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Text("VIEW GALLERY", style = MaterialTheme.typography.titleMedium.copy(fontFamily = PlayfairDisplayFont, fontStyle = FontStyle.Italic, fontSize = 14.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primaryContainer)
        }
        Spacer(modifier = Modifier.height(20.dp))
        
        val badgesToDisplay = badgeGrid.take(6) // Only want 6 slots to match mockup visually (3x2)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (row in badgesToDisplay.chunked(3)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { badge ->
                        ProfileBadgeSolid(
                            badge = badge,
                            onBadgeClick = {
                                selectedBadge = it
                                showBottomSheet = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (showBottomSheet && selectedBadge != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                BadgeDetailContent(selectedBadge!!)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- 8. Monthly Challenge ---
        val actChal = uiState.activeChallenge
        if (actChal != null) {
            MonthlyChallengeCard(
                title = actChal.title,
                desc = actChal.description,
                current = actChal.currentCount,
                target = actChal.targetCount
            )
            Spacer(modifier = Modifier.height(48.dp))
        }

        // --- 9. Bottom Data Rows ---
        BottomDataRow("FAVORITE DECADE", uiState.favoriteDecade, Icons.Outlined.Schedule)
        Spacer(modifier = Modifier.height(12.dp))
        BottomDataRow("TOP DIRECTOR", uiState.topDirector, Icons.Outlined.Person)
        Spacer(modifier = Modifier.height(12.dp))
        
        // Streak Card variant for bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(4.dp).height(80.dp).background(MaterialTheme.colorScheme.primaryContainer))
            Column(modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 20.dp, end = 16.dp).weight(1f)) {
                Text("RECENT STREAK", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(4.dp))
                Text("$streak Days", style = MaterialTheme.typography.titleMedium.copy(fontFamily = PlayfairDisplayFont, fontWeight = FontWeight.Bold, fontSize = 18.sp), color = MaterialTheme.colorScheme.onSurface)
            }
            Icon(Icons.Outlined.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(end = 20.dp))
        }
    }
}

@Composable
fun GenrePassportCard(genres: List<Pair<String, Float>>) {
    val topGenre = genres.firstOrNull() ?: ("EMPTY" to 0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
            .padding(24.dp)
    ) {
        Text("GENRE PASSPORT", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Donut Chart
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            val unColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
            val fillCol = MaterialTheme.colorScheme.primaryContainer
            val sweep = 360f * (if(genres.isEmpty()) 0f else (topGenre.second / 100f))
            
            Canvas(modifier = Modifier.size(130.dp)) {
                drawArc(
                    color = unColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
                )
                drawArc(
                    color = fillCol,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${topGenre.second.toInt()}%", style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp))
                Text(topGenre.first.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Mini Progress lists
        if (genres.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GenreMiniRow(genres[0].first, genres[0].second, isTop = true)
                    if (genres.size > 2) GenreMiniRow(genres[2].first, genres[2].second, isTop = false)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (genres.size > 1) GenreMiniRow(genres[1].first, genres[1].second, isTop = false)
                    if (genres.size > 3) GenreMiniRow(genres[3].first, genres[3].second, isTop = false)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                Text("LOG MOVIES TO BUILD YOUR ARCHIVE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun GenreMiniRow(name: String, value: Float, isTop: Boolean) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
            Text("${value.toInt()}%", style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
            color = if (isTop) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun ProfileStatBlock(title: String, value: String, suffixIcon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = MaterialTheme.typography.displaySmall.copy(fontFamily = PlayfairDisplayFont, fontWeight = FontWeight.Bold, fontSize = 32.sp))
            if (suffixIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(suffixIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun ProfileBadgeSolid(
    badge: Badge,
    onBadgeClick: (Badge) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked = badge.isUnlocked
    val badgeId = badge.badgeId
    val name = badge.name

    val borderColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    val textColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    
    // Rename standard DB badges to match mockups visually if preferred, else standard DB name
    val displayName = name.uppercase()

    Column(
        modifier = modifier
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onBadgeClick(badge) }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val icon = getBadgeIcon(badgeId)
        Icon(
            imageVector = icon,
            contentDescription = displayName,
            tint = textColor,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(displayName, style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = textColor, maxLines = 1)
    }
}

@Composable
fun BadgeDetailContent(badge: Badge) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = getBadgeIcon(badge.badgeId)
        val isUnlocked = badge.isUnlocked
        val color = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = badge.name.uppercase(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = PlayfairDisplayFont,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isUnlocked) "UNLOCKED" else "LOCKED",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = color
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = badge.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        if (isUnlocked && badge.unlockedDate != null) {
            Spacer(modifier = Modifier.height(16.dp))
            val date = java.time.Instant.ofEpochMilli(badge.unlockedDate!!)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            Text(
                text = "Achieved on $date",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun MonthlyChallengeCard(title: String, desc: String, current: Int, target: Int) {
    val pct = (current.toFloat() / target).coerceIn(0f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
            .padding(24.dp)
    ) {
        Text("MONTHLY CHALLENGE", style = MaterialTheme.typography.titleMedium.copy(fontFamily = PlayfairDisplayFont, fontStyle = FontStyle.Italic, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.primaryContainer)
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontFamily = PlayfairDisplayFont, fontWeight = FontWeight.Bold, fontSize = 24.sp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(desc, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$current / $target LOGGED", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurface)
            Text("${(pct * 100).toInt()}% COMPLETE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 9.sp), color = MaterialTheme.colorScheme.primaryContainer)
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val sections = target.coerceAtLeast(1)
            repeat(sections) { idx ->
                val filled = idx < current
                val secColor = if(filled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(secColor))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {}, 
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text("VIEW ELIGIBLE FILMS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 11.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun BottomDataRow(label: String, value: String, trailingIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontFamily = PlayfairDisplayFont, fontWeight = FontWeight.Bold, fontSize = 18.sp), color = MaterialTheme.colorScheme.onSurface)
        }
        Icon(trailingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
    }
}

// Retain similar logic but match exact mockup vectors roughly for the preview
fun getBadgeIcon(badgeId: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when(badgeId) {
        "first_log" -> Icons.Filled.DarkMode
        "centurion" -> Icons.Filled.Explore
        "horror_fiend" -> Icons.Filled.Movie
        "old_soul" -> Icons.Outlined.CalendarToday
        "binge_king" -> Icons.Outlined.Assignment
        "marathon" -> Icons.Outlined.MenuBook
        else -> Icons.Outlined.StarBorder
    }
}
