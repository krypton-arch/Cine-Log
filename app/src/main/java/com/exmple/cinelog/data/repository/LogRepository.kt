package com.exmple.cinelog.data.repository

import com.exmple.cinelog.data.local.dao.LogDao
import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.dao.MovieDao
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val logDao: LogDao,
    private val movieDao: MovieDao
) {
    fun getAllLogs(): Flow<List<LogWithMovie>> {
        return logDao.getAllLogs()
    }

    fun getLogsByDateRange(startDate: Long, endDate: Long): Flow<List<LogWithMovie>> {
        return logDao.getLogsByDateRange(startDate, endDate)
    }

    fun getTotalFilmsWatched(): Flow<Int> {
        return logDao.getTotalFilmsWatched()
    }

    fun getTotalMinutesWatched(): Flow<Int?> {
        return logDao.getTotalMinutesWatched()
    }

    suspend fun logMovie(movie: MovieEntity, logEntry: LogEntry) {
        movieDao.upsertMovie(movie)
        logDao.insertLog(logEntry)
    }
}
