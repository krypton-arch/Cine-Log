package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.local.AppDatabase
import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DiaryViewModel(
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

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val repository = LogRepository(db.logDao(), db.movieDao())
                @Suppress("UNCHECKED_CAST")
                return DiaryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
