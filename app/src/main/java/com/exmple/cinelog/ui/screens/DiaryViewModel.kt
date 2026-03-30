package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.exmple.cinelog.data.local.AppDatabase
import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: LogRepository
) : ViewModel() {

    private val _logs = MutableStateFlow<List<LogWithMovie>>(emptyList())
    val logs: StateFlow<List<LogWithMovie>> = _logs.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllLogs()
                .catch { /* Handle error */ }
                .collect { dbLogs ->
                    _logs.value = dbLogs
                }
        }
    }

}
