package com.exmple.cinelog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Priority { MUST_WATCH, CASUAL, SOMEDAY }

@Entity(
    tableName = "watchlist",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["movieId"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("movieId", unique = true)]
)
data class WatchlistEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val movieId: Int,
    val priority: Priority,
    val addedDate: Long,
    val notes: String?
)
