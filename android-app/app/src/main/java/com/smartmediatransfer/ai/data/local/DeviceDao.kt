package com.smartmediatransfer.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(device: TrustedDeviceEntity)

    @Query("SELECT * FROM trusted_devices WHERE isAuthorized = 1")
    suspend fun getTrustedDevices(): List<TrustedDeviceEntity>
}
