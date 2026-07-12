package com.smartmediatransfer.ai.di

import android.content.Context
import androidx.room.Room
import com.smartmediatransfer.ai.data.local.DeviceDao
import com.smartmediatransfer.ai.data.local.SmartMediaDatabase
import com.smartmediatransfer.ai.data.local.TransferDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmartMediaDatabase {
        return Room.databaseBuilder(
            context,
            SmartMediaDatabase::class.java,
            "smartmedia_db"
        ).build()
    }

    @Provides
    fun provideTransferDao(database: SmartMediaDatabase): TransferDao {
        return database.transferDao()
    }

    @Provides
    fun provideDeviceDao(database: SmartMediaDatabase): DeviceDao {
        return database.deviceDao()
    }
}
