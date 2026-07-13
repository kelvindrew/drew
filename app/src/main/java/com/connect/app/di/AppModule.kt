package com.connect.app.di

import android.content.Context
import com.connect.ai.SmartCoachAI
import com.connect.bluetooth.BleScannerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSmartCoachAI(): SmartCoachAI {
        return SmartCoachAI("fake_gemini_api_key_123")
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): com.connect.settings.SettingsDataStore {
        return com.connect.settings.SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideBleScannerManager(@ApplicationContext context: Context): BleScannerManager {
        return BleScannerManager(context)
    }
}
