package com.exmple.cinelog.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.domain.GamificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlin.coroutines.cancellation.CancellationException

data class MonthStats(
    val totalLogged: Int = 0,
    val avgRating: String = "—",
    val totalMinutes: Int = 0
)

data class DiaryUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val allLogs: List<LogWithMovie> = emptyList(),
    val monthLogs: Map<Int, List<LogWithMovie>> = emptyMap(),
    val monthStats: MonthStats = MonthStats(),
    val selectedDayLogs: List<LogWithMovie>? = null
)

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: LogRepository,
    private val gamificationManager: GamificationManager
) : ViewModel() {

    val initialYearMonth: YearMonth = YearMonth.now()

    private val _uiState = MutableStateFlow(DiaryUiState(currentYearMonth = initialYearMonth))
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllLogs()
                .catch { e ->
                    if (e is CancellationException) throw e
                    // Log error silently
                }
                .collect { dbLogs ->
                    _uiState.update { state ->
                        val filtered = filterLogsForMonth(dbLogs, state.currentYearMonth)
                        state.copy(
                            allLogs = dbLogs,
                            monthLogs = filtered,
                            monthStats = computeMonthStats(dbLogs, state.currentYearMonth)
                        )
                    }
                }
        }
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _uiState.update { state ->
            val filtered = filterLogsForMonth(state.allLogs, yearMonth)
            state.copy(
                currentYearMonth = yearMonth,
                monthLogs = filtered,
                monthStats = computeMonthStats(state.allLogs, yearMonth)
            )
        }
    }

    fun onDaySelected(day: Int) {
        _uiState.update { state ->
            state.copy(selectedDayLogs = state.monthLogs[day])
        }
    }

    fun onDismissSheet() {
        _uiState.update { state ->
            state.copy(selectedDayLogs = null)
        }
    }

    fun deleteLogEntry(logEntry: LogEntry) {
        viewModelScope.launch {
            repository.deleteLogEntry(logEntry)
            gamificationManager.syncMonthlyChallenge()
            // After deletion, the Flow from getAllLogs() will re-emit automatically.
            // If the selected day's logs are now empty after the reactive update,
            // dismiss the sheet.
            _uiState.update { state ->
                val updatedDayLogs = state.selectedDayLogs?.filter { it.logEntry.logId != logEntry.logId }
                state.copy(
                    selectedDayLogs = if (updatedDayLogs.isNullOrEmpty()) null else updatedDayLogs
                )
            }
        }
    }

    fun updateLogEntry(logEntry: LogEntry) {
        viewModelScope.launch {
            repository.updateLogEntry(logEntry)
            gamificationManager.syncMonthlyChallenge()
            // The Flow from getAllLogs() will re-emit with updated data automatically.
        }
    }

    private fun filterLogsForMonth(
        allLogs: List<LogWithMovie>,
        yearMonth: YearMonth
    ): Map<Int, List<LogWithMovie>> {
        val startOfMonth = yearMonth.atDay(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = yearMonth.atEndOfMonth()
            .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return allLogs
            .filter { it.logEntry.watchDate in startOfMonth..endOfMonth }
            .groupBy { log ->
                Instant.ofEpochMilli(log.logEntry.watchDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .dayOfMonth
            }
    }

    private fun computeMonthStats(
        allLogs: List<LogWithMovie>,
        yearMonth: YearMonth
    ): MonthStats {
        val startOfMonth = yearMonth.atDay(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = yearMonth.atEndOfMonth()
            .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val monthLogs = allLogs.filter { it.logEntry.watchDate in startOfMonth..endOfMonth }

        val totalLogged = monthLogs.size
        val rated = monthLogs.filter { it.logEntry.rating > 0 }
        val avgRating = if (rated.isNotEmpty()) {
            "%.1f".format(rated.map { it.logEntry.rating.toDouble() }.average())
        } else "—"
        val totalMinutes = monthLogs.sumOf { it.movie.runtime ?: 0 }

        return MonthStats(
            totalLogged = totalLogged,
            avgRating = avgRating,
            totalMinutes = totalMinutes
        )
    }
}
