package com.exmple.cinelog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.exmple.cinelog.data.local.dao.*
import com.exmple.cinelog.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MovieEntity::class,
        WatchlistEntry::class,
        LogEntry::class,
        UserProfile::class,
        Badge::class,
        Challenge::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun logDao(): LogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun gamificationDao(): GamificationDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cinelog_database"
                )
                .addCallback(SeedCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }
    }
}

suspend fun seedDatabase(db: AppDatabase) {
    // Seed initial user profile
    db.userProfileDao().insertProfile(
        UserProfile(
            id = 1,
            xp = 0,
            level = 1,
            currentStreak = 0,
            lastLogDate = null,
            totalFilmsWatched = 0,
            totalMinutesWatched = 0
        )
    )

    // Seed badges
    val badges = listOf(
        Badge(
            badgeId = "first_log",
            name = "First Frame",
            description = "Log your first movie",
            iconRes = "🎬",
            isUnlocked = false,
            unlockedDate = null
        ),
        Badge(
            badgeId = "centurion",
            name = "Centurion",
            description = "Log 100 movies",
            iconRes = "💯",
            isUnlocked = false,
            unlockedDate = null
        ),
        Badge(
            badgeId = "horror_fiend",
            name = "Horror Fiend",
            description = "Watch 10 horror films",
            iconRes = "👻",
            isUnlocked = false,
            unlockedDate = null
        ),
        Badge(
            badgeId = "old_soul",
            name = "Old Soul",
            description = "Watch 10 pre-1980 films",
            iconRes = "🎞️",
            isUnlocked = false,
            unlockedDate = null
        ),
        Badge(
            badgeId = "binge_king",
            name = "Binge King",
            description = "Watch 5 films in 7 days",
            iconRes = "👑",
            isUnlocked = false,
            unlockedDate = null
        ),
        Badge(
            badgeId = "marathon",
            name = "Marathon Runner",
            description = "Watch 1000 total minutes",
            iconRes = "🏃",
            isUnlocked = false,
            unlockedDate = null
        ),
        Badge(
            badgeId = "critic",
            name = "The Critic",
            description = "Write 10 reviews",
            iconRes = "✍️",
            isUnlocked = false,
            unlockedDate = null
        ),
        Badge(
            badgeId = "streak_7",
            name = "Week Warrior",
            description = "Maintain a 7-day logging streak",
            iconRes = "🔥",
            isUnlocked = false,
            unlockedDate = null
        )
    )
    db.gamificationDao().insertBadges(badges)

    // Seed challenges
    val challenges = listOf(
        Challenge(
            challengeId = "weekend_warrior",
            title = "Weekend Warrior",
            description = "Watch 3 movies this weekend",
            targetCount = 3,
            currentCount = 0,
            deadline = null,
            isCompleted = false
        ),
        Challenge(
            challengeId = "genre_explorer",
            title = "Genre Explorer",
            description = "Watch films from 5 different genres",
            targetCount = 5,
            currentCount = 0,
            deadline = null,
            isCompleted = false
        ),
        Challenge(
            challengeId = "review_streak",
            title = "Review Streak",
            description = "Write reviews for 5 consecutive logs",
            targetCount = 5,
            currentCount = 0,
            deadline = null,
            isCompleted = false
        )
    )
    db.gamificationDao().insertChallenges(challenges)
}
