package com.exmple.cinelog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.exmple.cinelog.data.remote.RemoteMovie
import com.exmple.cinelog.ui.theme.bounceClick
import com.exmple.cinelog.ui.theme.shimmerEffect
import com.exmple.cinelog.ui.theme.glassCard
import com.exmple.cinelog.ui.theme.glassSurface
import com.exmple.cinelog.ui.theme.regalDivider

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToWatchlist: () -> Unit,
    onNavigateToProjectionist: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
) {
    val trendingMovies by viewModel.trendingMovies.collectAsState()
    val popularMovies by viewModel.popularMovies.collectAsState()
    val nowPlayingMovies by viewModel.nowPlayingMovies.collectAsState()
    val topRatedMovies by viewModel.topRatedMovies.collectAsState()
    val totalFilms by viewModel.totalFilmsLogged.collectAsState()
    val totalMinutes by viewModel.totalMinutesLogged.collectAsState()
    val watchlistCount by viewModel.watchlistCount.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToProjectionist,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.AutoStories, contentDescription = "The Projectionist")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(48.dp)
        ) {
        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.LocalMovies, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer)
                Text("CINELOG", style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 4.sp), color = MaterialTheme.colorScheme.primaryContainer)
            }
        }

        // ── Trending Now ──
        MovieCarouselSection(
            title = "Trending Now",
            subtitle = "ARCHIVED WEEKLY",
            movies = trendingMovies.take(10),
            badgeText = "TRENDING",
            onMovieClick = onMovieClick
        )

        // ── Watchlist Nudge Card ──
        val recommended = popularMovies.firstOrNull()
        if (recommended != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerRadius = 16.dp)
                    .padding(28.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(16.dp))
                        Text("WATCHLIST JOURNEY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primaryContainer)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    if (watchlistCount > 0) {
                        Text("$watchlistCount movies waiting in your archive.", style = MaterialTheme.typography.headlineMedium, lineHeight = 34.sp)
                    } else {
                        Text("Start your collection. Add movies to your watchlist.", style = MaterialTheme.typography.headlineMedium, lineHeight = 34.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(recommended.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToWatchlist,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("OPEN LIBRARY", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                    }
                }
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${recommended.poster_path}",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(140.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                )
            }
        }

        // ── Now Playing ──
        MovieCarouselSection(
            title = "Now in Theaters",
            subtitle = "CURRENTLY SHOWING",
            movies = nowPlayingMovies.take(10),
            badgeText = "NOW",
            onMovieClick = onMovieClick
        )

        // ── Bento Stats (Dynamic) ──
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BentoStatCard(title = "Films Logged", value = totalFilms.toString(), icon = Icons.Default.HistoryEdu, modifier = Modifier.weight(1f))
            val minutesDisplay = if (totalMinutes >= 1000) "%.1fk".format(totalMinutes / 1000f) else totalMinutes.toString()
            BentoStatCard(title = "Minutes Archive", value = minutesDisplay, icon = Icons.Default.Timer, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BentoStatCard(title = "In Watchlist", value = watchlistCount.toString(), icon = Icons.Default.Bookmark, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
        }

        // ── Top Rated ──
        MovieCarouselSection(
            title = "Top Rated of All Time",
            subtitle = "HIGHEST SCORES",
            movies = topRatedMovies.take(10),
            badgeText = null,
            showRating = true,
            onMovieClick = onMovieClick
        )

        // ── Popular ──
        MovieCarouselSection(
            title = "Popular Right Now",
            subtitle = "FAN FAVORITES",
            movies = popularMovies.take(10),
            badgeText = "POPULAR",
            onMovieClick = onMovieClick
        )

        Spacer(modifier = Modifier.height(60.dp)) // Bottom nav clearance
        }
    }
}

// ── Reusable Section ──
@Composable
fun MovieCarouselSection(
    title: String,
    subtitle: String,
    movies: List<RemoteMovie>,
    badgeText: String? = null,
    showRating: Boolean = false,
    onMovieClick: (Int) -> Unit = {}
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).regalDivider())
        Spacer(modifier = Modifier.height(24.dp))

        // ── Placeholder Skeleton Loading ──
        if (movies.isEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(5) {
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .aspectRatio(2f/3f)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmerEffect()
                    )
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(movies) { movie ->
                    TrendingMovieCard(movie, badgeText = badgeText, showRating = showRating, onClick = { onMovieClick(movie.id) })
                }
            }
        }
    }
}

// ── Stat Card ──
@Composable
fun BentoStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .glassCard(cornerRadius = 16.dp)
            .padding(24.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(value, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Movie Card ──
@Composable
fun TrendingMovieCard(movie: RemoteMovie, badgeText: String? = null, showRating: Boolean = false, onClick: () -> Unit = {}) {
    Column(modifier = Modifier.width(160.dp).bounceClick { onClick() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Badge or Rating
            val label = when {
                showRating -> "★ ${"%.1f".format(movie.vote_average)}"
                badgeText != null -> badgeText
                else -> null
            }
            if (label != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(movie.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(movie.release_date?.take(4) ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
