package com.exmple.cinelog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

data class LogWithMovie(
    @androidx.room.Embedded val logEntry: LogEntry,
    @androidx.room.Relation(
        parentColumn = "movieId",
        entityColumn = "movieId"
    )
    val movie: MovieEntity
)

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(logEntry: LogEntry)

    @Query("SELECT * FROM logs ORDER BY watchDate DESC")
    fun getAllLogs(): Flow<List<LogWithMovie>>

    @Query("SELECT * FROM logs WHERE watchDate BETWEEN :startDate AND :endDate ORDER BY watchDate DESC")
    fun getLogsByDateRange(startDate: Long, endDate: Long): Flow<List<LogWithMovie>>
    
    @Query("SELECT COUNT(*) FROM logs")
    fun getTotalFilmsWatched(): Flow<Int>
    
    @Query("SELECT SUM(runtime) FROM logs INNER JOIN movies ON logs.movieId = movies.movieId")
    fun getTotalMinutesWatched(): Flow<Int?>
}
