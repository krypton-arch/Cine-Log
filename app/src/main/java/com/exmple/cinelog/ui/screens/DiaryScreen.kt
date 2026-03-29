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
import com.exmple.cinelog.ui.theme.bounceClick
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onLogNewMovie: () -> Unit = {},
    viewModel: DiaryViewModel = viewModel(
        factory = DiaryViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val logs by viewModel.logs.collectAsState()
    
    // We get today's month natively, and render logs grid
    val today = LocalDate.now()
    val daysInMonth = today.lengthOfMonth()
    
    var selectedDayLogs by remember { mutableStateOf<List<com.exmple.cinelog.data.local.dao.LogWithMovie>?>(null) }

    // Convert logs list to a Map of Day -> List of Logs
    val logMap = logs
        .filter {
            val logDate = Instant.ofEpochMilli(it.logEntry.watchDate).atZone(ZoneId.systemDefault()).toLocalDate()
            logDate.month == today.month && logDate.year == today.year
        }
        .groupBy(
            keySelector = { Instant.ofEpochMilli(it.logEntry.watchDate).atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth }
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) // #131313 matching Noir
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 16.dp) // accommodate navbar
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
                val dayLogs = logMap[day]

                if (!dayLogs.isNullOrEmpty()) {
                    val firstPoster = dayLogs.first().movie.posterPath
                    // Logged Day
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { selectedDayLogs = dayLogs }
                    ) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w200$firstPoster",
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
                        if (dayLogs.size > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .glassSurface(cornerRadius = 4.dp, alpha = 0.5f)
                            ) {
                                Text(
                                    text = "+${dayLogs.size - 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
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
        
        // FAB for logging new movie
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp, end = 24.dp), // Adjust for navigation bar and padding
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = onLogNewMovie,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log New Movie")
            }
        }
    }

    selectedDayLogs?.let { dayLogs ->
        ModalBottomSheet(
            onDismissRequest = { selectedDayLogs = null },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                val logDate = Instant.ofEpochMilli(dayLogs.first().logEntry.watchDate).atZone(ZoneId.systemDefault()).toLocalDate()
                val dateStr = "${logDate.month.name} ${logDate.dayOfMonth}, ${logDate.year}".uppercase()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(dateStr, style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primaryContainer)
                    Text("${dayLogs.size} FILMS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(dayLogs.size) { index ->
                        val logWithMovie = dayLogs[index]
                        Box(
                            modifier = Modifier
                                .aspectRatio(2f/3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w200${logWithMovie.movie.posterPath}",
                                contentDescription = logWithMovie.movie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Rating Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    ))
                                    .padding(top = 24.dp, bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primaryContainer)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (logWithMovie.logEntry.rating > 0) "${logWithMovie.logEntry.rating}" else "—",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
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
