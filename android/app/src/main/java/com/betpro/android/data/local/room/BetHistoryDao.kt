package com.betpro.android.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BetHistoryDao {

    @Query("SELECT * FROM bet_history ORDER BY timestamp DESC")
    fun getAllBets(): Flow<List<BetHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(bet: BetHistoryEntity)

    @Query("DELETE FROM bet_history")
    suspend fun clearHistory()
}
