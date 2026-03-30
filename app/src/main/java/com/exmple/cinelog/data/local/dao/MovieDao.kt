package com.exmple.cinelog.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.exmple.cinelog.data.local.entity.MovieEntity

@Dao
interface MovieDao {
    @Upsert
    suspend fun upsertMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE movieId = :id")
    suspend fun getMovieById(id: Int): MovieEntity?
}
