package com.exmple.cinelog.ui.screens.diary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.utils.rethrowIfCancellation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: LogRepository
) : ViewModel() {

    private val _logs = MutableStateFlow<List<LogWithMovie>>(emptyList())
    val logs: StateFlow<List<LogWithMovie>> = _logs.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllLogs()
                .catch { error ->
                    error.rethrowIfCancellation()
                    Log.e("DiaryViewModel", "Failed to load diary logs", error)
                    _logs.value = emptyList()
                }
                .collect { dbLogs ->
                    _logs.value = dbLogs
                }
        }
    }
}
