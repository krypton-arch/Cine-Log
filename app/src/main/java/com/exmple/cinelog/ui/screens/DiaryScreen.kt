package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.exmple.cinelog.ui.theme.glassCard
import com.exmple.cinelog.ui.theme.glassSurface
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = viewModel(
        factory = DiaryViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val logs by viewModel.logs.collectAsState()
    
    // We get today's month natively, and render logs grid
    val today = LocalDate.now()
    val daysInMonth = today.lengthOfMonth()
    
    // Convert logs list to a Map of Day -> PosterUrl
    val logMap = logs
        .filter {
            val logDate = Instant.ofEpochMilli(it.logEntry.watchDate).atZone(ZoneId.systemDefault()).toLocalDate()
            logDate.month == today.month && logDate.year == today.year
        }
        .associateBy(
            keySelector = { Instant.ofEpochMilli(it.logEntry.watchDate).atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth },
            valueTransform = { it.movie.posterPath }
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) // #131313 matching Noir
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 100.dp) // accommodate navbar
    ) {
        // Top Header
        Text("Diary", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "A chronological archive of your cinematic journey. Every frame remembered.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        // Compute dynamic stats
        val thisMonthCount = logMap.size
        val avgRating = if (logs.isNotEmpty()) {
            val rated = logs.filter { it.logEntry.rating > 0 }
            if (rated.isNotEmpty()) "%.1f".format(rated.map { it.logEntry.rating }.average()) else "—"
        } else "—"

        // Stats Bento Mini
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DiaryStatComponent(title = "TOTAL LOGGED", value = logs.size.toString(), modifier = Modifier.weight(1f))
            DiaryStatComponent(title = "THIS MONTH", value = thisMonthCount.toString(), modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DiaryStatComponent(title = "LOGGED FILMS", value = logs.size.toString(), modifier = Modifier.weight(1f))
            DiaryStatComponent(title = "AVG RATING", value = avgRating, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Current Month Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.dp, color = Color.Transparent)
                .drawWithContent {
                    drawRect(color = Color(0xFFF5C518), size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height))
                    drawContent()
                }
                .padding(start = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${today.year}", style = MaterialTheme.typography.headlineLarge)
            Text("${logs.size} MOVIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid Calendar
        // Headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Calculate offset (Assume month starts on 1st day of week for simplicity here, real logic requires java.time.DayOfWeek calculation)
        val startOffset = today.withDayOfMonth(1).dayOfWeek.value - 1

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(startOffset) {
                Box(modifier = Modifier.aspectRatio(1f)) // Empty block
            }
            items(daysInMonth) { index ->
                val day = index + 1
                val poster = logMap[day]

                if (poster != null) {
                    // Logged Day
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w200$poster",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Gray, blendMode = androidx.compose.ui.graphics.BlendMode.Saturation)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                        )
                        Text(
                            text = String.format("%02d", day),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp),
                            color = Color.White
                        )
                    }
                } else {
                    // Empty Day
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .glassSurface(cornerRadius = 8.dp, alpha = 0.2f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d", day),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryStatComponent(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .glassCard(cornerRadius = 14.dp)
            .padding(24.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
        Text(value, style = MaterialTheme.typography.headlineLarge, color = if (title == "TOTAL LOGGED") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurface)
    }
}
