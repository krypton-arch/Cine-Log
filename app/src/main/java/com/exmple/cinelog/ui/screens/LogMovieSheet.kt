package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.ui.theme.glassCard
import com.exmple.cinelog.ui.theme.glassSurface
import java.time.LocalDate

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMovieSheet(
    movie: MovieEntity,
    wasOnWatchlist: Boolean = false,
    existingEntry: LogEntry? = null,
    viewModel: LoggingViewModel = hiltViewModel(),
    onLogComplete: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val rating by viewModel.rating.collectAsState()
    val reviewText by viewModel.reviewText.collectAsState()
    val selectedAtmosphere by viewModel.selectedAtmosphere.collectAsState()

    val isEditMode = existingEntry != null

    // Pre-fill fields when in edit mode
    LaunchedEffect(existingEntry, movie.movieId) {
        if (existingEntry != null) {
            viewModel.prefillFromEntry(existingEntry)
        } else {
            viewModel.resetForm()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF131313), // Strict Surface Noir color
        scrimColor = Color.Black.copy(alpha = 0.8f), // Immersive dark
        tonalElevation = 0.dp
    ) {
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section with Save Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w200${movie.posterPath}",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(60.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column {
                        Text(
                            text = movie.title.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            movie.releaseYear ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Quick Save Action in Header
                IconButton(
                    onClick = {
                        if (isEditMode) {
                            val updated = existingEntry!!.copy(
                                rating = rating,
                                review = reviewText,
                                moodTag = selectedAtmosphere
                            )
                            viewModel.updateEntry(updated) {
                                onLogComplete()
                            }
                        } else {
                            viewModel.logMovie(movie, wasOnWatchlist) {
                                onLogComplete()
                            }
                        }
                    },
                    modifier = Modifier.glassSurface(cornerRadius = 12.dp, alpha = 0.5f)
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Save", tint = MaterialTheme.colorScheme.primaryContainer)
                }
            }

            // Glass Panel Card for Inputs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerRadius = 20.dp, alpha = 0.5f)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // Log Date Minimal
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("LOG DATE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    OutlinedTextField(
                        value = if (isEditMode) {
                            java.time.Instant.ofEpochMilli(existingEntry!!.watchDate)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                        } else {
                            LocalDate.now().toString()
                        },
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        readOnly = true,
                        colors = defaultTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Rating Space
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("RATING", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        Text(rating.toString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primaryContainer)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassSurface(cornerRadius = 12.dp, alpha = 0.3f)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            repeat(5) { index ->
                                val starValue = index + 1f
                                Icon(
                                    imageVector = if (rating >= starValue) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = "Star $starValue",
                                    tint = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { viewModel.updateRating(starValue) }
                                )
                            }
                        }
                        Text("Tap stars", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }

                // Atmosphere Tags
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TAGS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    ) {
                        val tags = movie.genres.split(",").filter { it.isNotBlank() }.take(5).map { it.trim() }
                        val displayTags = if (tags.isNotEmpty()) tags else listOf("Intense", "Mind-bending", "Masterpiece")
                        
                        displayTags.forEach { tag ->
                            val isSelected = selectedAtmosphere == tag
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (isSelected) Modifier.glassCard(cornerRadius = 16.dp, alpha = 0.6f, borderAlpha = 0.15f)
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                        else Modifier.glassSurface(cornerRadius = 16.dp, alpha = 0.25f)
                                    )
                                    .clickable { viewModel.toggleAtmosphere(tag) }
                            ) {
                                Text(
                                    text = tag.uppercase(),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Journal Entry text
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("JOURNAL ENTRY", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { viewModel.updateReviewText(it) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = defaultTextFieldColors(),
                        placeholder = { Text("Thoughts on this viewing...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // CTA
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (isEditMode) {
                                val updated = existingEntry!!.copy(
                                    rating = rating,
                                    review = reviewText,
                                    moodTag = selectedAtmosphere
                                )
                                viewModel.updateEntry(updated) {
                                    onLogComplete()
                                }
                            } else {
                                viewModel.logMovie(movie, wasOnWatchlist) {
                                    onLogComplete()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(
                                if (isEditMode) "UPDATE ENTRY" else "SAVE ARCHIVE ENTRY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                                maxLines = 1
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (isEditMode) "ENTRY WILL BE UPDATED" else "ARTIFACT WILL BE ARCHIVED",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun defaultTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
    focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
    unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)
