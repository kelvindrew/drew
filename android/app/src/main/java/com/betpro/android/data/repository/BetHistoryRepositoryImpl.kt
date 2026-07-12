package com.betpro.android.data.repository

import com.betpro.android.data.local.room.BetHistoryDao
import com.betpro.android.data.local.room.BetHistoryEntity
import com.betpro.android.domain.repository.BetHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BetHistoryRepositoryImpl @Inject constructor(
    private val dao: BetHistoryDao
) : BetHistoryRepository {

    override fun getAllBetsFlow(): Flow<List<BetHistoryEntity>> = dao.getAllBets()

    override suspend fun insertBet(bet: BetHistoryEntity) {
        dao.insertBet(bet)
    }
}
