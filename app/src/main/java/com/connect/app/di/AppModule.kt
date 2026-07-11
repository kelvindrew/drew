package com.connect.app.di

import com.connect.ai.SmartCoachAI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSmartCoachAI(): SmartCoachAI {
        // En production, cette clé viendrait de BuildConfig ou d'un DataStore
        return SmartCoachAI("fake_gemini_api_key_123")
    }
}
