package com.exmple.cinelog.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.exmple.cinelog.data.local.dao.*
import com.exmple.cinelog.data.repository.*
import com.exmple.cinelog.domain.GamificationManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLogRepository(
        logDao: LogDao,
        movieDao: MovieDao
    ): LogRepository {
        return LogRepository(logDao, movieDao)
    }

    @Provides
    @Singleton
    fun provideGamificationRepository(
        gamificationDao: GamificationDao,
        userProfileDao: UserProfileDao
    ): GamificationRepository {
        return GamificationRepository(gamificationDao, userProfileDao)
    }

    @Provides
    @Singleton
    fun provideWatchlistRepository(
        watchlistDao: WatchlistDao,
        movieDao: MovieDao
    ): WatchlistRepository {
        return WatchlistRepository(watchlistDao, movieDao)
    }

    @Provides
    @Singleton
    fun provideGamificationManager(
        gamificationRepository: GamificationRepository,
        logRepository: LogRepository
    ): GamificationManager {
        return GamificationManager(gamificationRepository, logRepository)
    }
}
