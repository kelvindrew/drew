package com.betpro.android.domain.model

data class AppSettings(
    val isSimulationMode: Boolean = true,
    val isAIEngineEnabled: Boolean = true,
    val baseStake: Double = 10.0,
    val stopLossThreshold: Double = 100.0,
    val virtualBalance: Double = 1250.0
)
