package com.betpro.android.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BetHistoryEntity::class], version = 1, exportSchema = false)
abstract class BetProDatabase : RoomDatabase() {
    abstract fun betHistoryDao(): BetHistoryDao
}
