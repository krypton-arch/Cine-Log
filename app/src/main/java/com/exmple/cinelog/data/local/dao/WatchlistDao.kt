package com.exmple.cinelog.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.exmple.cinelog.data.local.entity.Priority
import com.exmple.cinelog.data.local.entity.WatchlistEntry
import com.exmple.cinelog.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

data class WatchlistItemWithMovie(
    @androidx.room.Embedded val watchlistEntry: WatchlistEntry,
    @androidx.room.Relation(
        parentColumn = "movieId",
        entityColumn = "movieId"
    )
    val movie: MovieEntity
)

@Dao
interface WatchlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistEntry(entry: WatchlistEntry)

    @Update
    suspend fun updateWatchlistEntry(entry: WatchlistEntry)

    @Delete
    suspend fun deleteWatchlistEntry(entry: WatchlistEntry)

    @Transaction
    @Query("SELECT * FROM watchlist ORDER BY addedDate DESC")
    fun getAllWatchlistItems(): Flow<List<WatchlistItemWithMovie>>

    @Transaction
    @Query("SELECT * FROM watchlist WHERE priority = :priority ORDER BY addedDate DESC")
    fun getWatchlistByPriority(priority: Priority): Flow<List<WatchlistItemWithMovie>>

    @Query("SELECT COUNT(*) FROM watchlist")
    fun getWatchlistCount(): Flow<Int>
}
