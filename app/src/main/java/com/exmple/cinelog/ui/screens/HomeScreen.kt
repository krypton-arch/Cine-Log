package com.exmple.cinelog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Timer
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exmple.cinelog.data.remote.RemoteMovie
import com.exmple.cinelog.ui.screens.home.HomeViewModel
import com.exmple.cinelog.ui.theme.NoirBackdrop
import com.exmple.cinelog.ui.theme.bounceClick
import com.exmple.cinelog.ui.theme.glassCard
import com.exmple.cinelog.ui.theme.glassSurface
import com.exmple.cinelog.ui.theme.regalDivider
import com.exmple.cinelog.ui.theme.shimmerEffect

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
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
        ) {
            NoirBackdrop(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                Text(
                    "FEATURE REEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.4.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primaryContainer
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalMovies,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primaryContainer
                        )
                        Text(
                            "CINELOG",
                            style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 4.sp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }

                HomeHeroMasthead(
                    totalFilms = totalFilms,
                    totalMinutes = totalMinutes,
                    watchlistCount = watchlistCount,
                    onNavigateToWatchlist = onNavigateToWatchlist
                )

                MovieCarouselSection(
                    title = "Trending Now",
                    subtitle = "ARCHIVED WEEKLY",
                    movies = trendingMovies.take(10),
                    badgeText = "TRENDING",
                    onMovieClick = onMovieClick
                )

                val recommended = popularMovies.firstOrNull()
                if (recommended != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard(cornerRadius = 20.dp, alpha = 0.42f, borderAlpha = 0.1f)
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "WATCHLIST JOURNEY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                )
                            }

                            Text(
                                if (watchlistCount > 0) {
                                    "$watchlistCount films are waiting in your private library."
                                } else {
                                    "Start your collection. Add films to your private library."
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                lineHeight = 34.sp
                            )

                            Text(
                                recommended.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f)
                            )

                            OutlinedButton(
                                onClick = onNavigateToWatchlist,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.align(Alignment.Start)
                            ) {
                                Text(
                                    "OPEN LIBRARY",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    )
                                )
                            }
                        }

                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w500${recommended.poster_path}",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(128.dp)
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }

                MovieCarouselSection(
                    title = "Now in Theaters",
                    subtitle = "CURRENTLY SHOWING",
                    movies = nowPlayingMovies.take(10),
                    badgeText = "NOW",
                    onMovieClick = onMovieClick
                )

                MovieCarouselSection(
                    title = "Top Rated of All Time",
                    subtitle = "HIGHEST SCORES",
                    movies = topRatedMovies.take(10),
                    badgeText = null,
                    showRating = true,
                    onMovieClick = onMovieClick
                )

                MovieCarouselSection(
                    title = "Popular Right Now",
                    subtitle = "FAN FAVORITES",
                    movies = popularMovies.take(10),
                    badgeText = "POPULAR",
                    onMovieClick = onMovieClick
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun HomeHeroMasthead(
    totalFilms: Int,
    totalMinutes: Int,
    watchlistCount: Int,
    onNavigateToWatchlist: () -> Unit
) {
    val minutesDisplay = if (totalMinutes >= 1000) "%.1fk".format(totalMinutes / 1000f) else totalMinutes.toString()
    val headline = if (watchlistCount > 0) {
        "Your next screening is already waiting."
    } else {
        "Start building your next screening queue."
    }
    val supporting = if (watchlistCount > 0) {
        "$watchlistCount films are lined up in your private library."
    } else {
        "Explore a few titles and give the archive something to anticipate."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 28.dp, alpha = 0.44f, borderAlpha = 0.12f)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "TONIGHT'S LEDGER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                Text(
                    headline,
                    style = MaterialTheme.typography.headlineMedium,
                    lineHeight = 34.sp
                )
                Text(
                    supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )
            }

            Box(
                modifier = Modifier
                    .glassSurface(cornerRadius = 999.dp, alpha = 0.24f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    "HOME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeMetricPill(
                title = "LOGGED",
                value = totalFilms.toString(),
                icon = Icons.Default.HistoryEdu,
                modifier = Modifier.weight(1f)
            )
            HomeMetricPill(
                title = "MINUTES",
                value = minutesDisplay,
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f)
            )
            HomeMetricPill(
                title = "LIBRARY",
                value = watchlistCount.toString(),
                icon = Icons.Default.Bookmark,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedButton(
            onClick = onNavigateToWatchlist,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text(
                "OPEN LIBRARY",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
            )
        }
    }
}

@Composable
private fun HomeMetricPill(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassSurface(cornerRadius = 16.dp, alpha = 0.16f)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(18.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            title,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        )
    }
}

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .regalDivider()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (movies.isEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(5) {
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmerEffect()
                    )
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(movies) { movie ->
                    TrendingMovieCard(
                        movie = movie,
                        badgeText = badgeText,
                        showRating = showRating,
                        onClick = { onMovieClick(movie.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TrendingMovieCard(
    movie: RemoteMovie,
    badgeText: String? = null,
    showRating: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .bounceClick { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                    RoundedCornerShape(12.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

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
                        .glassSurface(cornerRadius = 6.dp, alpha = 0.36f)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            movie.title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            movie.release_date?.take(4) ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
