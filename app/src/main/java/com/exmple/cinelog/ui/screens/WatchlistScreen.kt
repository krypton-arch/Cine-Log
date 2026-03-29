package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.exmple.cinelog.data.local.dao.WatchlistItemWithMovie
import com.exmple.cinelog.data.remote.RemoteMovie
import com.exmple.cinelog.ui.theme.bounceClick
import com.exmple.cinelog.ui.theme.glassCard
import com.exmple.cinelog.ui.theme.glassSurface
import com.exmple.cinelog.ui.theme.regalDivider
import com.exmple.cinelog.ui.theme.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onMovieClick: (Int) -> Unit = {},
    viewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val watchlist by viewModel.watchlist.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedMovieForLog by remember { mutableStateOf<WatchlistItemWithMovie?>(null) }
    var movieToRemove by remember { mutableStateOf<WatchlistItemWithMovie?>(null) }
    
    // Log Movie Sheet
    if (selectedMovieForLog != null) {
        LogMovieSheet(
            movie = selectedMovieForLog!!.movie,
            wasOnWatchlist = true,
            onLogComplete = {
                viewModel.removeFromWatchlist(selectedMovieForLog!!)
                selectedMovieForLog = null
            },
            onDismissRequest = { selectedMovieForLog = null }
        )
    }

    // Remove Confirmation Dialog
    if (movieToRemove != null) {
        AlertDialog(
            onDismissRequest = { movieToRemove = null },
            containerColor = Color(0xFF1A1A18),
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            title = {
                Text(
                    "Remove from Library?",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Text(
                    "\"${movieToRemove!!.movie.title}\" will be removed from your watchlist.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeFromWatchlist(movieToRemove!!)
                        movieToRemove = null
                    }
                ) {
                    Text("REMOVE", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { movieToRemove = null }) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        // ── Header ──
        Text(
            "LIBRARY",
            style = MaterialTheme.typography.displaySmall.copy(letterSpacing = 4.sp),
            color = MaterialTheme.colorScheme.primaryContainer
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${watchlist.size} artifacts in your private archive.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // ── Search Bar ──
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.searchMovies(it)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            placeholder = {
                Text(
                    "Search to add to archive...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer)
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // ── Regal Divider ──
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).regalDivider())
        
        Spacer(modifier = Modifier.height(24.dp))

        if (searchQuery.isNotEmpty()) {
            // ═══ SEARCH RESULTS ═══
            if (isSearching) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(5) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassCard(cornerRadius = 14.dp, alpha = 0.2f)
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .aspectRatio(2f / 3f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .shimmerEffect()
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth(0.75f).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                                Box(modifier = Modifier.fillMaxWidth(0.3f).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.fillMaxWidth(0.9f).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                                Box(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                            }
                        }
                    }
                }
            } else if (searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No films found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.animateContentSize()
                ) {
                    items(searchResults, key = { it.id }) { movie ->
                        SearchResultCard(
                            movie = movie,
                            onAdd = {
                                viewModel.addToWatchlist(movie)
                                searchQuery = ""
                            }
                        )
                    }
                }
            }
        } else if (watchlist.isEmpty()) {
            // ═══ EMPTY STATE ═══
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📽️", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Your library is empty",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Search above to discover and archive films.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // ═══ WATCHLIST ITEMS ═══
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.animateContentSize()
            ) {
                items(
                    items = watchlist,
                    key = { it.watchlistEntry.id }
                ) { item ->
                    WatchlistMovieCard(
                        item = item,
                        onTap = { onMovieClick(item.movie.movieId) },
                        onLog = { selectedMovieForLog = item },
                        onRemove = { movieToRemove = item }
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════
// Watchlist Movie Card — Rich Detail View
// ═════════════════════════════════════════════════════════

@Composable
fun WatchlistMovieCard(
    item: WatchlistItemWithMovie,
    onTap: () -> Unit,
    onLog: () -> Unit,
    onRemove: () -> Unit
) {
    val movie = item.movie
    val genres = movie.genres.split(",").filter { it.isNotBlank() }.take(2)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 16.dp)
            .clickable(onClick = onTap)
    ) {
        // ── Main Content Row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Poster
            Box {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w200${movie.posterPath}",
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(85.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                )
            }
            
            // Details Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title
                Text(
                    movie.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Year • Runtime
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    movie.releaseYear?.let { year ->
                        Text(
                            year,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (movie.runtime != null && movie.runtime > 0) {
                        Text(
                            "•",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            "${movie.runtime} MIN",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Genre Tags
                if (genres.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        genres.forEach { genre ->
                            Box(
                                modifier = Modifier
                                    .glassSurface(cornerRadius = 6.dp, alpha = 0.3f)
                            ) {
                                Text(
                                    genre.trim().uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                // Overview Snippet
                if (!movie.overview.isNullOrBlank()) {
                    Text(
                        movie.overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        
        // ── Action Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassSurface(cornerRadius = 0.dp, alpha = 0.2f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // View Details hint
            Text(
                "TAP TO VIEW DETAILS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            
            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Remove Button
                Box(
                    modifier = Modifier
                        .glassSurface(cornerRadius = 8.dp, alpha = 0.3f)
                        .bounceClick { onRemove() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.BookmarkRemove,
                            contentDescription = "Remove",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            "REMOVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                
                // Log Button
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .bounceClick { onLog() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.HistoryEdu,
                            contentDescription = "Log",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            "LOG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════
// Search Result Card — Enhanced Detail View
// ═════════════════════════════════════════════════════════

@Composable
fun SearchResultCard(
    movie: RemoteMovie,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 14.dp)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Poster
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w200${movie.poster_path}",
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(70.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
        )
        
        // Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                movie.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                movie.release_date?.take(4)?.let { year ->
                    Text(
                        year,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (movie.vote_average > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primaryContainer
                        )
                        Text(
                            "%.1f".format(movie.vote_average),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            }
            
            // Overview snippet
            if (!movie.overview.isNullOrBlank()) {
                Text(
                    movie.overview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
        
        // Add Button
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                .bounceClick { onAdd() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                "ADD",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
