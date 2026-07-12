package com.betpro.app.di

import com.betpro.app.BuildConfig
import com.betpro.app.data.BackendApiService
import com.betpro.app.data.OddsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOddsApiService(): OddsApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.the-odds-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OddsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBackendApiService(): BackendApiService {
        val baseUrl = BuildConfig.BACKEND_URL.ifEmpty { "http://10.0.2.2:3000/" }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApiService::class.java)
    }
}
