package com.exmple.cinelog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "logs",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["movieId"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("movieId")]
)
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val logId: Int = 0,
    val movieId: Int,
    val watchDate: Long,
    val rating: Float,
    val review: String?,
    val moodTag: String?,
    val isRewatch: Boolean
)
