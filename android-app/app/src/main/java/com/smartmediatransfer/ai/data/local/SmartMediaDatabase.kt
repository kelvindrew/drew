package com.smartmediatransfer.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TransferRecordEntity::class, TrustedDeviceEntity::class], version = 1, exportSchema = false)
abstract class SmartMediaDatabase : RoomDatabase() {
    abstract fun transferDao(): TransferDao
    abstract fun deviceDao(): DeviceDao
}
