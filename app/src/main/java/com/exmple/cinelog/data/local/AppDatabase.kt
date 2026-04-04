package com.exmple.cinelog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.exmple.cinelog.data.local.dao.*
import com.exmple.cinelog.data.local.entity.*
import com.exmple.cinelog.domain.MonthlyChallengeEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId

@Database(
    entities = [
        MovieEntity::class,
        WatchlistEntry::class,
        LogEntry::class,
        UserProfile::class,
        Badge::class,
        Challenge::class,
        AiInsightEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun logDao(): LogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun gamificationDao(): GamificationDao
    abstract fun aiDao(): AiDao
    
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
                .addCallback(SeedCallback(context))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                seedDatabase(getDatabase(context))
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                // Check if seeded - if no profile with ID 1, seed it.
                val profile = database.userProfileDao().getUserProfile().firstOrNull()
                if (profile == null) {
                    seedDatabase(database)
                }
            }
        }
    }
}

suspend fun seedDatabase(db: AppDatabase) {
    // Seed initial user profile
    db.userProfileDao().upsertProfile(
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

    val currentMonthlyChallenge = MonthlyChallengeEngine.challengeForMonth(
        yearMonth = YearMonth.now(),
        zoneId = ZoneId.systemDefault()
    )
    db.gamificationDao().insertChallenges(listOf(currentMonthlyChallenge))
}
