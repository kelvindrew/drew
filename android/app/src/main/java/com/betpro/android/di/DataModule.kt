package com.betpro.android.di

import com.betpro.android.data.api.ApiFootballService
import com.betpro.android.data.api.TheOddsApiService
import com.betpro.android.data.repository.SportEventRepositoryImpl
import com.betpro.android.domain.repository.SportEventRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideApiFootballService(): ApiFootballService {
        return Retrofit.Builder()
            .baseUrl("https://v3.football.api-sports.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiFootballService::class.java)
    }

    @Provides
    @Singleton
    fun provideTheOddsApiService(): TheOddsApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.the-odds-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TheOddsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSportEventRepository(
        footballService: ApiFootballService,
        oddsService: TheOddsApiService,
        settingsRepository: com.betpro.android.domain.repository.SettingsRepository
    ): SportEventRepository {
        return SportEventRepositoryImpl(footballService, oddsService, settingsRepository)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): com.betpro.android.domain.repository.SettingsRepository {
        return com.betpro.android.data.local.SettingsRepositoryImpl(context)
    }
}
