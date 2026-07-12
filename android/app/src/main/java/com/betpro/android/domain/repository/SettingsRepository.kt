package com.betpro.android.domain.repository

import com.betpro.android.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val appSettingsFlow: Flow<AppSettings>
    suspend fun updateSimulationMode(isSimulation: Boolean)
    suspend fun updateAIEngine(isEnabled: Boolean)
    suspend fun updateBaseStake(stake: Double)
    suspend fun updateStopLoss(stopLoss: Double)
    suspend fun updateVirtualBalance(newBalance: Double)
}
