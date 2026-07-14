package com.smartmediatransfer.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(transfer: TransferRecordEntity)

    @Query("SELECT * FROM transfers WHERE deviceId = :deviceId AND state != 'VERIFIED' ORDER BY priority DESC, addedToQueueDate ASC")
    suspend fun getPendingTransfersForDevice(deviceId: String): List<TransferRecordEntity>

    @Query("SELECT * FROM transfers ORDER BY addedToQueueDate DESC")
    fun getAllTransfersFlow(): Flow<List<TransferRecordEntity>>

    @Query("SELECT * FROM transfers WHERE fileHash = :hash AND state = 'VERIFIED' LIMIT 1")
    suspend fun getVerifiedTransferByHash(hash: String): TransferRecordEntity?
}
