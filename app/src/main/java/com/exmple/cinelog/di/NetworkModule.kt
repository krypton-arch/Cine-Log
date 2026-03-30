package com.exmple.cinelog.di

import com.exmple.cinelog.data.remote.MovieApiService
import com.exmple.cinelog.data.remote.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMovieApiService(): MovieApiService {
        return RetrofitClient.apiService
    }
}
