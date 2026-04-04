package com.exmple.cinelog.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.dao.WatchlistItemWithMovie
import com.exmple.cinelog.data.local.entity.AiInsightEntity
import com.exmple.cinelog.data.local.entity.Badge
import com.exmple.cinelog.data.local.entity.Challenge
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.data.local.entity.UserProfile
import com.exmple.cinelog.data.repository.AiRepository
import com.exmple.cinelog.data.repository.ArchiveGamificationRepository
import com.exmple.cinelog.data.repository.GeminiRepository
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.data.repository.WatchlistRepository
import com.exmple.cinelog.domain.GamificationManager
import com.exmple.cinelog.domain.MonthlyChallengeSnapshot
import com.exmple.cinelog.domain.ProjectionistContext
import com.exmple.cinelog.domain.PromptAssembler
import com.exmple.cinelog.utils.rethrowIfCancellation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val levelName: String = "Cinephile",
    val badges: List<Badge> = emptyList(),
    val totalHours: Int = 0,
    val avgRating: Float = 0f,
    val totalFilms: Int = 0,
    val topGenres: List<Pair<String, Float>> = emptyList(),
    val monthlyChallenge: MonthlyChallengeSnapshot? = null,
    val favoriteDecade: String = "N/A",
    val topDirector: String = "N/A",
    val dailyInsight: String? = null
)

private data class ProfileSourceData(
    val profile: UserProfile?,
    val badges: List<Badge>,
    val logs: List<LogWithMovie>,
    val watchlistItems: List<WatchlistItemWithMovie>,
    val challenges: List<Challenge>
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val archiveGamificationRepository: ArchiveGamificationRepository,
    private val logRepository: LogRepository,
    private val watchlistRepository: WatchlistRepository,
    private val gamificationManager: GamificationManager,
    private val aiRepository: AiRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private var insightRefreshInFlight = false

    init {
        viewModelScope.launch {
            gamificationManager.syncMonthlyChallenge()
        }

        viewModelScope.launch {
            combine(
                combine(
                    archiveGamificationRepository.getUserProfile(),
                    archiveGamificationRepository.getAllBadges(),
                    logRepository.getAllLogs(),
                    watchlistRepository.getAllWatchlistItems(),
                    archiveGamificationRepository.getAllChallenges()
                ) { profile, badges, logs, watchlistItems, challenges ->
                    ProfileSourceData(
                        profile = profile,
                        badges = badges,
                        logs = logs,
                        watchlistItems = watchlistItems,
                        challenges = challenges
                    )
                },
                aiRepository.getDailyInsight()
            ) { sourceData, cachedInsight ->
                val profile = sourceData.profile
                val badges = sourceData.badges
                val logs = sourceData.logs
                val watchlistItems = sourceData.watchlistItems
                val challenges = sourceData.challenges

                val levelName = profile?.level?.let { gamificationManager.getLevelName(it) } ?: "Cinephile"

                var totalMinutes = 0
                var totalRating = 0f
                var countWithRating = 0
                val genreCounts = mutableMapOf<String, Int>()
                val directorCounts = mutableMapOf<String, Int>()
                val decadeCounts = mutableMapOf<Int, Int>()

                logs.forEach { logWithMovie ->
                    totalMinutes += logWithMovie.movie.runtime ?: 0
                    if (logWithMovie.logEntry.rating > 0) {
                        totalRating += logWithMovie.logEntry.rating
                        countWithRating++
                    }

                    val genresList = logWithMovie.movie.genres
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    genresList.forEach { genre ->
                        genreCounts[genre] = genreCounts.getOrDefault(genre, 0) + 1
                    }

                    val director = logWithMovie.movie.director
                    if (!director.isNullOrBlank()) {
                        directorCounts[director] = directorCounts.getOrDefault(director, 0) + 1
                    }

                    val yearStr = logWithMovie.movie.releaseYear
                    if (!yearStr.isNullOrBlank()) {
                        val year = yearStr.take(4).toIntOrNull()
                        if (year != null && year > 1800) {
                            val decade = (year / 10) * 10
                            decadeCounts[decade] = decadeCounts.getOrDefault(decade, 0) + 1
                        }
                    }
                }

                val totalHours = totalMinutes / 60
                val avgRating = if (countWithRating > 0) totalRating / countWithRating else 0f
                val topGenres = genreCounts.entries
                    .sortedByDescending { it.value }
                    .take(4)
                    .map { it.key to (it.value.toFloat() / maxOf(1, genreCounts.values.sum())) * 100 }

                val topDirector = directorCounts.maxByOrNull { it.value }?.key ?: "NONE"
                val favoriteDecade = decadeCounts.maxByOrNull { it.value }?.key?.let { "${it}s" } ?: "NONE"
                val monthlyChallenge = gamificationManager.buildCurrentMonthlyChallengeSnapshot(
                    challenges = challenges,
                    logs = logs
                )

                if (shouldRefreshInsight(logs.isNotEmpty(), cachedInsight)) {
                    requestDailyInsight(
                        recentLogs = logs.take(10).map { it.movie.title },
                        watchlistTop5 = watchlistItems.take(5).map { it.movie },
                        total = logs.size,
                        genre = topGenres.firstOrNull()?.first ?: "Unknown",
                        decade = favoriteDecade,
                        director = topDirector
                    )
                }

                ProfileUiState(
                    userProfile = profile,
                    levelName = levelName,
                    badges = badges,
                    totalHours = totalHours,
                    avgRating = avgRating,
                    totalFilms = logs.size,
                    topGenres = topGenres,
                    monthlyChallenge = monthlyChallenge,
                    favoriteDecade = favoriteDecade,
                    topDirector = topDirector,
                    dailyInsight = cachedInsight?.insightText
                )
            }.catch { error ->
                error.rethrowIfCancellation()
                Log.e("ProfileViewModel", "Failed to load profile state", error)
            }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    private fun shouldRefreshInsight(
        hasLogs: Boolean,
        cachedInsight: AiInsightEntity?
    ): Boolean {
        if (!hasLogs || insightRefreshInFlight) {
            return false
        }

        val generatedAt = cachedInsight?.generatedAt ?: return true
        return isCacheStale(generatedAt)
    }

    private fun isCacheStale(generatedAt: Long): Boolean {
        val twentyFourHours = 24 * 60 * 60 * 1000L
        return System.currentTimeMillis() - generatedAt > twentyFourHours
    }

    private fun requestDailyInsight(
        recentLogs: List<String>,
        watchlistTop5: List<MovieEntity>,
        total: Int,
        genre: String,
        decade: String,
        director: String
    ) {
        insightRefreshInFlight = true
        viewModelScope.launch {
            try {
                val context = ProjectionistContext(
                    recentLogs = recentLogs,
                    topGenre = genre,
                    topDirector = director,
                    favoriteDecade = decade,
                    watchlistTop5 = watchlistTop5,
                    totalFilmsLogged = total
                )
                val prompt = PromptAssembler.build(context)
                val result = geminiRepository.sendMessage(
                    prompt,
                    "Give me one short, cryptic cinematic insight about my archive. Maximum 2 sentences."
                )

                val insightText = result.getOrNull()
                if (insightText != null) {
                    aiRepository.saveInsight(
                        insightText = insightText,
                        generatedAt = System.currentTimeMillis()
                    )
                } else {
                    result.exceptionOrNull()?.let { error ->
                        error.rethrowIfCancellation()
                        Log.e("ProfileViewModel", "Failed to generate daily insight", error)
                    }
                }
            } catch (error: Throwable) {
                error.rethrowIfCancellation()
                Log.e("ProfileViewModel", "Failed to generate daily insight", error)
            } finally {
                insightRefreshInFlight = false
            }
        }
    }
}
