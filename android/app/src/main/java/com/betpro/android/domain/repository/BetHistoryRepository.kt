package com.betpro.android.domain.repository

import com.betpro.android.data.local.room.BetHistoryEntity
import kotlinx.coroutines.flow.Flow

interface BetHistoryRepository {
    fun getAllBetsFlow(): Flow<List<BetHistoryEntity>>
    suspend fun insertBet(bet: BetHistoryEntity)
}
