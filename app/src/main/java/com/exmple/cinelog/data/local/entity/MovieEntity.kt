package com.exmple.cinelog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val releaseYear: String?,
    val genres: String,
    val runtime: Int?,
    val director: String?,
    val overview: String?
)
