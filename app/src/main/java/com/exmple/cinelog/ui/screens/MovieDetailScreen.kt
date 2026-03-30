package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.exmple.cinelog.ui.theme.bounceClick
import com.exmple.cinelog.ui.theme.glassCard
import com.exmple.cinelog.ui.theme.glassSurface
import com.exmple.cinelog.ui.theme.shimmerEffect

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    onBack: () -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogSheet by remember { mutableStateOf(false) }

    // LogMovieSheet
    if (showLogSheet) {
        val movieEntity = viewModel.toMovieEntity()
        if (movieEntity != null) {
            LogMovieSheet(
                movie = movieEntity,
                wasOnWatchlist = false,
                onLogComplete = { showLogSheet = false },
                onDismissRequest = { showLogSheet = false }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (uiState.isLoading) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().height(420.dp).shimmerEffect())
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(40.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.width(60.dp).height(24.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                        Box(modifier = Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        Box(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(20.dp)).shimmerEffect())
                }
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Failed to load movie details", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val detail = uiState.detail!!
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Hero Poster with Gradient Overlay & Parallax
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .graphicsLayer {
                            translationY = scrollState.value * 0.5f // Parallax effect
                        }
                ) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w780${detail.poster_path}",
                        contentDescription = detail.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    val darkenAlpha = (scrollState.value / 600f).coerceIn(0f, 0.85f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = darkenAlpha))
                    )

                    // Gradient overlay from bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                                )
                            )
                    )
                    // Back button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(top = 40.dp, start = 16.dp)
                            .align(Alignment.TopStart)
                            .glassSurface(cornerRadius = 12.dp, alpha = 0.5f)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    // Rating badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 24.dp, bottom = 24.dp)
                            .glassCard(cornerRadius = 10.dp, alpha = 0.7f, borderAlpha = 0.12f)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                "${"%.1f".format(detail.vote_average)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Title
                    Text(
                        detail.title.uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Black
                        )
                    )

                    // Metadata row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        detail.release_date?.take(4)?.let { year ->
                            MetadataChip(year)
                        }
                        detail.runtime?.let { runtime ->
                            MetadataChip("$runtime MIN")
                        }
                        if (detail.genres.isNotEmpty()) {
                            MetadataChip(detail.genres.first().name.uppercase())
                        }
                    }

                    // Genre tags
                    if (detail.genres.size > 1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            detail.genres.drop(1).take(3).forEach { genre ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier.glassSurface(cornerRadius = 16.dp, alpha = 0.3f)
                                ) {
                                    Text(
                                        genre.name,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Overview
                    if (!detail.overview.isNullOrBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "SYNOPSIS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                            Text(
                                detail.overview,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 26.sp
                            )
                        }
                    }

                    // Action buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard(cornerRadius = 20.dp, alpha = 0.5f)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Log to Diary button (Bounce enabled)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                .bounceClick { showLogSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimary)
                                Text("LOG TO DIARY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }

                        // Add to Watchlist button (Bounce enabled)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(
                                    1.dp,
                                    if (uiState.isInWatchlist) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .bounceClick { viewModel.addToWatchlist() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    if (uiState.isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (uiState.isInWatchlist) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (uiState.isInWatchlist) "ADDED TO LIBRARY" else "ADD TO LIBRARY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                                    color = if (uiState.isInWatchlist) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun MetadataChip(text: String) {
    Box(
        modifier = Modifier
            .glassSurface(cornerRadius = 8.dp, alpha = 0.4f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
