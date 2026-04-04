package com.exmple.cinelog.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.ui.theme.glassCard
import com.exmple.cinelog.ui.theme.glassSurface
import com.exmple.cinelog.ui.theme.regalDivider
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.random.Random

import androidx.hilt.navigation.compose.hiltViewModel
import com.exmple.cinelog.ui.screens.diary.DiaryViewModel
import com.exmple.cinelog.ui.screens.diary.MonthStats
import kotlinx.coroutines.launch

private const val CENTER_INDEX = 600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onLogNewMovie: () -> Unit = {},
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = CENTER_INDEX,
        pageCount = { 1200 }
    )

    // React to pager settling on a new page
    LaunchedEffect(pagerState.settledPage) {
        val yearMonth = pageToYearMonth(pagerState.settledPage, viewModel.initialYearMonth)
        viewModel.onMonthChanged(yearMonth)
    }

    // State for edit mode
    var editingLogEntry by remember { mutableStateOf<LogEntry?>(null) }
    var editingMovie by remember { mutableStateOf<com.exmple.cinelog.data.local.entity.MovieEntity?>(null) }

    // State for delete confirmation
    var logToDelete by remember { mutableStateOf<LogEntry?>(null) }

    // Edit sheet
    if (editingLogEntry != null && editingMovie != null) {
        LogMovieSheet(
            movie = editingMovie!!,
            existingEntry = editingLogEntry,
            onLogComplete = {
                editingLogEntry = null
                editingMovie = null
            },
            onDismissRequest = {
                editingLogEntry = null
                editingMovie = null
            }
        )
    }

    // Delete confirmation dialog
    if (logToDelete != null) {
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            containerColor = Color(0xFF1A1A18),
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            title = {
                Text(
                    "Delete Entry?",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Text(
                    "This log entry will be permanently removed from your archive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        logToDelete?.let { viewModel.deleteLogEntry(it) }
                        logToDelete = null
                    }
                ) {
                    Text("DELETE", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        DiaryAtmosphere(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 110.dp)
        ) {
            Text(
                "NOIR ARCHIVE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.4.sp
                ),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Diary", style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "A chronological archive of your cinematic journey. Every frame remembered.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Stats Bento Mini — dynamic per-month + all-time total
            DiaryMonthMasthead(
                currentYearMonth = uiState.currentYearMonth,
                totalLogged = uiState.allLogs.size,
                monthStats = uiState.monthStats,
                activeDays = uiState.monthLogs.size,
                onPreviousMonth = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNextMonth = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassSurface(cornerRadius = 24.dp, alpha = 0.18f)
                    .padding(horizontal = 14.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "MONTH VIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
                    )

                    Text(
                        "Swipe to browse",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                day,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.4.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    val visibleMonth = pageToYearMonth(pagerState.currentPage, viewModel.initialYearMonth)
                    val visibleMonthRows = calendarRowsForMonth(visibleMonth)
                    val cellSize = (maxWidth - 48.dp) / 7
                    val gridHeight = cellSize * visibleMonthRows + 8.dp * (visibleMonthRows - 1)

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight),
                        beyondViewportPageCount = 1
                    ) { page ->
                        val ym = pageToYearMonth(page, viewModel.initialYearMonth)
                        val daysInMonth = ym.lengthOfMonth()
                        val startOffset = ym.atDay(1).dayOfWeek.value - 1
                        val today = LocalDate.now()
                        val isCurrentMonth = ym == YearMonth.from(today)

                        val logsForPage = if (ym == uiState.currentYearMonth) {
                            uiState.monthLogs
                        } else {
                            emptyMap()
                        }

                        CalendarMonthGrid(
                            daysInMonth = daysInMonth,
                            startOffset = startOffset,
                            today = today,
                            isCurrentMonth = isCurrentMonth,
                            monthLogs = logsForPage,
                            onDayClick = { day ->
                                if (logsForPage.containsKey(day)) {
                                    viewModel.onDaySelected(day)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedVisibility(
                visible = uiState.currentYearMonth != viewModel.initialYearMonth,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(CENTER_INDEX)
                        }
                    },
                    modifier = Modifier.glassSurface(cornerRadius = 999.dp, alpha = 0.18f)
                ) {
                    Text(
                        "BACK TO TODAY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }

            if (false) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DiaryStatComponent(
                    title = "WATCH TIME",
                    value = if (uiState.monthStats.totalMinutes > 0) "${uiState.monthStats.totalMinutes / 60}h" else "—",
                    modifier = Modifier.weight(1f)
                )
                DiaryStatComponent(
                    title = "AVG RATING",
                    value = uiState.monthStats.avgRating,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Month Header with chevrons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.dp, color = Color.Transparent)
                    .drawWithContent {
                        drawRect(
                            color = Color(0xFFF5C518),
                            size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                        )
                        drawContent()
                    }
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous month",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Crossfade(
                    targetState = uiState.currentYearMonth,
                    label = "month_header"
                ) { ym ->
                    Text(
                        ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next month",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month film count
            Text(
                "${uiState.monthStats.totalLogged} MOVIES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Day-of-week headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // HorizontalPager calendar — use BoxWithConstraints for proper height
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                val visibleMonth = pageToYearMonth(pagerState.currentPage, viewModel.initialYearMonth)
                val visibleMonthRows = calendarRowsForMonth(visibleMonth)

                // Calculate grid height from available width
                // 7 columns, 6 gaps of 8dp each
                val cellSize = (maxWidth - 48.dp) / 7
                val gridHeight = cellSize * visibleMonthRows + 8.dp * (visibleMonthRows - 1)

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight),
                    beyondViewportPageCount = 1
                ) { page ->
                    val ym = pageToYearMonth(page, viewModel.initialYearMonth)
                    val daysInMonth = ym.lengthOfMonth()
                    val startOffset = ym.atDay(1).dayOfWeek.value - 1
                    val today = LocalDate.now()
                    val isCurrentMonth = ym == YearMonth.from(today)

                    // Use uiState.monthLogs only for the settled page
                    val logsForPage = if (ym == uiState.currentYearMonth) {
                        uiState.monthLogs
                    } else {
                        emptyMap()
                    }

                    CalendarMonthGrid(
                        daysInMonth = daysInMonth,
                        startOffset = startOffset,
                        today = today,
                        isCurrentMonth = isCurrentMonth,
                        monthLogs = logsForPage,
                        onDayClick = { day ->
                            if (logsForPage.containsKey(day)) {
                                viewModel.onDaySelected(day)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Today" snap-back pill
            AnimatedVisibility(
                visible = uiState.currentYearMonth != viewModel.initialYearMonth,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(CENTER_INDEX)
                        }
                    }
                ) {
                    Text(
                        "● TODAY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
            }
        }

        // FAB for logging new movie
        FloatingActionButton(
            onClick = onLogNewMovie,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Log New Movie")
        }
    }

    // ═══ CINEMATIC DAY BOTTOM SHEET ═══
    uiState.selectedDayLogs?.let { dayLogs ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { viewModel.onDismissSheet() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        ) {
            CinematicDaySheetContent(
                dayLogs = dayLogs,
                currentYearMonth = uiState.currentYearMonth,
                onEdit = { logWithMovie ->
                    editingLogEntry = logWithMovie.logEntry
                    editingMovie = logWithMovie.movie
                    viewModel.onDismissSheet()
                },
                onDelete = { logEntry ->
                    logToDelete = logEntry
                }
            )
        }
    }
}

// ═══ CALENDAR MONTH GRID ═══

@Composable
private fun DiaryAtmosphere(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF111111))
            .drawWithCache {
                val random = Random(17)
                val particleCount = ((size.width * size.height) / 1400f)
                    .toInt()
                    .coerceIn(180, 520)

                val particles = List(particleCount) {
                    Triple(
                        Offset(
                            x = random.nextFloat() * size.width,
                            y = random.nextFloat() * size.height
                        ),
                        0.2f + random.nextFloat() * 1.1f,
                        0.015f + random.nextFloat() * 0.045f
                    )
                }

                onDrawWithContent {
                    drawContent()
                    particles.forEach { (center, radius, alpha) ->
                        drawCircle(
                            color = Color.White.copy(alpha = alpha),
                            radius = radius,
                            center = center
                        )
                    }
                }
            }
    )
}

@Composable
private fun DiaryMonthMasthead(
    currentYearMonth: YearMonth,
    totalLogged: Int,
    monthStats: MonthStats,
    activeDays: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val watchTime = if (monthStats.totalMinutes > 0) "${monthStats.totalMinutes / 60}h" else "--"
    val monthSubtitle = when (monthStats.totalLogged) {
        0 -> "No screenings archived this month"
        1 -> "1 screening archived this month"
        else -> "${monthStats.totalLogged} screenings archived this month"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 28.dp, alpha = 0.48f, borderAlpha = 0.14f)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "PRIVATE LEDGER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.1.sp
                    ),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f)
                )
                Text(
                    "A living record of every screening and reflection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )
            }

            Box(
                modifier = Modifier
                    .glassSurface(cornerRadius = 999.dp, alpha = 0.28f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    "$totalLogged LOGGED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DiaryMonthNavButton(
                icon = Icons.Default.ChevronLeft,
                contentDescription = "Previous month",
                onClick = onPreviousMonth
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Crossfade(
                    targetState = currentYearMonth,
                    label = "masthead_month"
                ) { yearMonth ->
                    Text(
                        yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    monthSubtitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.6.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center
                )
            }

            DiaryMonthNavButton(
                icon = Icons.Default.ChevronRight,
                contentDescription = "Next month",
                onClick = onNextMonth
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .regalDivider()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DiaryMastheadMetric(
                label = "WATCH",
                value = watchTime,
                modifier = Modifier.weight(1f)
            )
            DiaryMastheadMetric(
                label = "RATING",
                value = monthStats.avgRating,
                modifier = Modifier.weight(1f)
            )
            DiaryMastheadMetric(
                label = "DAYS",
                value = activeDays.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DiaryMonthNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .glassSurface(cornerRadius = 14.dp, alpha = 0.22f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DiaryMastheadMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassSurface(cornerRadius = 16.dp, alpha = 0.18f)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

@Composable
private fun CalendarMonthGrid(
    daysInMonth: Int,
    startOffset: Int,
    today: LocalDate,
    isCurrentMonth: Boolean,
    monthLogs: Map<Int, List<LogWithMovie>>,
    onDayClick: (Int) -> Unit
) {
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7 // ceil division

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - startOffset + 1

                    if (cellIndex < startOffset || day > daysInMonth) {
                        // Empty cell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        val dayLogs = monthLogs[day]
                        val hasLogs = !dayLogs.isNullOrEmpty()
                        val isToday = isCurrentMonth && day == today.dayOfMonth

                        if (hasLogs) {
                            LoggedCalendarDayCell(
                                day = day,
                                dayLogs = dayLogs!!,
                                isToday = isToday,
                                onClick = { onDayClick(day) }
                            )
                            if (false) {
                            // ═══ LOGGED DAY — poster thumbnail ═══
                            val firstPoster = dayLogs!!.first().movie.posterPath
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .then(
                                        if (isToday) Modifier.border(
                                            2.dp,
                                            Color(0xFFF5C518),
                                            RoundedCornerShape(8.dp)
                                        ) else Modifier
                                    )
                                    .clickable { onDayClick(day) }
                            ) {
                                // Movie poster (desaturated)
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w200$firstPoster",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    colorFilter = ColorFilter.tint(
                                        Color.Gray,
                                        blendMode = BlendMode.Saturation
                                    )
                                )
                                // Dark overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f))
                                )
                                // Day number
                                Text(
                                    text = String.format("%02d", day),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp),
                                    color = Color.White
                                )
                                // Multi-film badge
                                if (dayLogs.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .glassSurface(
                                                cornerRadius = 4.dp,
                                                alpha = 0.5f
                                            )
                                    ) {
                                        Text(
                                            text = "+${dayLogs.size - 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White,
                                            modifier = Modifier.padding(
                                                horizontal = 4.dp,
                                                vertical = 2.dp
                                            )
                                        )
                                    }
                                }
                            }
                            }
                        } else {
                            EmptyCalendarDayCell(
                                day = day,
                                isToday = isToday
                            )
                            if (false) {
                            // ═══ EMPTY DAY ═══
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isToday) Color(0xFFF5C518).copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.3f)
                                    )
                                    .then(
                                        if (isToday) Modifier.border(
                                            1.dp,
                                            Color(0xFFF5C518).copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", day),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isToday) Color(0xFFF5C518)
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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

// ═══ CINEMATIC DAY SHEET CONTENT ═══

@Composable
private fun RowScope.LoggedCalendarDayCell(
    day: Int,
    dayLogs: List<LogWithMovie>,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val firstPoster = dayLogs.first().movie.posterPath

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                width = if (isToday) 1.5.dp else 1.dp,
                color = if (isToday) Color(0xFFF5C518) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w200$firstPoster",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(
                Color.Gray,
                blendMode = BlendMode.Saturation
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.24f),
                            Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
        )

        if (isToday) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5C518).copy(alpha = 0.08f))
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .glassSurface(
                    cornerRadius = 6.dp,
                    alpha = if (isToday) 0.42f else 0.3f
                )
        ) {
            Text(
                text = String.format("%02d", day),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFFF5C518).copy(alpha = 0.68f))
        )

        if (dayLogs.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .glassSurface(
                        cornerRadius = 999.dp,
                        alpha = 0.5f
                    )
            ) {
                Text(
                    text = "+${dayLogs.size - 1}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5C518).copy(alpha = 0.8f))
            )
        }
    }
}

@Composable
private fun RowScope.EmptyCalendarDayCell(
    day: Int,
    isToday: Boolean
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isToday) {
                        listOf(
                            Color(0xFFF5C518).copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.46f)
                        )
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.24f),
                            Color(0xFF171715).copy(alpha = 0.42f)
                        )
                    }
                )
            )
            .border(
                width = if (isToday) 1.25.dp else 1.dp,
                color = if (isToday) {
                    Color(0xFFF5C518).copy(alpha = 0.58f)
                } else {
                    Color.White.copy(alpha = 0.05f)
                },
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%02d", day),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.8.sp
            ),
            color = if (isToday) Color(0xFFF5C518)
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.46f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CinematicDaySheetContent(
    dayLogs: List<LogWithMovie>,
    currentYearMonth: YearMonth,
    onEdit: (LogWithMovie) -> Unit,
    onDelete: (LogEntry) -> Unit
) {
    val firstLog = dayLogs.firstOrNull() ?: return
    val logDate = java.time.Instant.ofEpochMilli(firstLog.logEntry.watchDate)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    val dateStr = "${logDate.month.name} ${logDate.dayOfMonth}, ${logDate.year}".uppercase()
    val filmLabel = if (dayLogs.size == 1) "1 FILM" else "${dayLogs.size} FILMS"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Date header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                dateStr,
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primaryContainer
            )
            Text(
                filmLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (dayLogs.size == 1) {
            // Single film — no pager
            CinematicFilmCard(
                logWithMovie = dayLogs.first(),
                onEdit = onEdit,
                onDelete = onDelete
            )
        } else {
            // Multi-film — swipeable pager
            val filmPagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { dayLogs.size }
            )

            HorizontalPager(
                state = filmPagerState,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = 16.dp
            ) { page ->
                CinematicFilmCard(
                    logWithMovie = dayLogs[page],
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Page indicator dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(dayLogs.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (index == filmPagerState.currentPage) 8.dp else 6.dp)
                            .background(
                                color = if (index == filmPagerState.currentPage)
                                    Color(0xFFF5C518)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

// ═══ CINEMATIC FILM CARD ═══

@Composable
private fun CinematicFilmCard(
    logWithMovie: LogWithMovie,
    onEdit: (LogWithMovie) -> Unit,
    onDelete: (LogEntry) -> Unit
) {
    val movie = logWithMovie.movie
    val entry = logWithMovie.logEntry

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 16.dp)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Poster + Info row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Poster
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w342${movie.posterPath}",
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(110.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    )
            )

            // Movie details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    movie.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Year • Runtime • Director
                val metaParts = mutableListOf<String>()
                movie.releaseYear?.let { metaParts.add(it) }
                movie.runtime?.let { if (it > 0) metaParts.add("${it}m") }
                movie.director?.let { if (it.isNotBlank()) metaParts.add(it) }

                if (metaParts.isNotEmpty()) {
                    Text(
                        metaParts.joinToString(" • "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Star rating (read-only)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) { index ->
                        val starValue = index + 1f
                        Icon(
                            imageVector = if (entry.rating >= starValue)
                                Icons.Default.Star
                            else
                                Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = Color(0xFFF5C518),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (entry.rating > 0) entry.rating.toString() else "—",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF5C518)
                    )
                }
            }
        }

        // Mood tag pill
        if (!entry.moodTag.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    entry.moodTag!!.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }

        // Review text
        if (!entry.review.isNullOrBlank()) {
            Column {
                Text(
                    "JOURNAL ENTRY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontSize = 9.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    entry.review!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
            }
        }

        // Action row — Edit and Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit button
            OutlinedButton(
                onClick = { onEdit(logWithMovie) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "EDIT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Delete button
            TextButton(
                onClick = { onDelete(entry) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "DELETE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

// ═══ UTILITY ═══

private fun pageToYearMonth(page: Int, initialYearMonth: YearMonth): YearMonth {
    return initialYearMonth.plusMonths((page - CENTER_INDEX).toLong())
}

private fun calendarRowsForMonth(yearMonth: YearMonth): Int {
    val startOffset = yearMonth.atDay(1).dayOfWeek.value - 1
    val totalCells = startOffset + yearMonth.lengthOfMonth()
    return (totalCells + 6) / 7
}

// ═══ STAT COMPONENT (preserved from original) ═══

@Composable
fun DiaryStatComponent(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .glassCard(cornerRadius = 14.dp)
            .padding(24.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineLarge,
            color = if (title == "TOTAL LOGGED") MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
