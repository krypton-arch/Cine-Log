package com.exmple.cinelog.data.repository

import com.exmple.cinelog.data.local.dao.MovieDao
import com.exmple.cinelog.data.local.dao.WatchlistDao
import com.exmple.cinelog.data.local.dao.WatchlistItemWithMovie
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.data.local.entity.Priority
import com.exmple.cinelog.data.local.entity.WatchlistEntry
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepository @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val movieDao: MovieDao
) {
    fun getAllWatchlistItems(): Flow<List<WatchlistItemWithMovie>> {
        return watchlistDao.getAllWatchlistItems()
    }

    fun getWatchlistByPriority(priority: Priority): Flow<List<WatchlistItemWithMovie>> {
        return watchlistDao.getWatchlistByPriority(priority)
    }

    suspend fun addToWatchlist(movie: MovieEntity, priority: Priority, notes: String? = null) {
        // Cache movie first
        movieDao.insertMovie(movie)
        val entry = WatchlistEntry(
            movieId = movie.movieId,
            priority = priority,
            addedDate = System.currentTimeMillis(),
            notes = notes
        )
        watchlistDao.insertWatchlistEntry(entry)
    }

    suspend fun updateWatchlistEntry(entry: WatchlistEntry) {
        watchlistDao.updateWatchlistEntry(entry)
    }

    suspend fun removeFromWatchlist(entry: WatchlistEntry) {
        watchlistDao.deleteWatchlistEntry(entry)
    }

    fun getWatchlistCount(): Flow<Int> {
        return watchlistDao.getWatchlistCount()
    }
}
