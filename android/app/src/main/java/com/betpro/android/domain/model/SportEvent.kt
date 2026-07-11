package com.betpro.android.domain.model

data class SportEvent(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val startTime: Long,
    val odds1: Double,
    val oddsX: Double,
    val odds2: Double,
    val oddsBttsYes: Double? = null,
    val oddsOver25: Double? = null
)
