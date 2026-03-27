package com.exmple.cinelog.data.local

import androidx.room.TypeConverter
import com.exmple.cinelog.data.local.entity.Priority

class DatabaseConverters {
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(name: String): Priority {
        return try {
            Priority.valueOf(name)
        } catch (e: Exception) {
            Priority.CASUAL
        }
    }
}
