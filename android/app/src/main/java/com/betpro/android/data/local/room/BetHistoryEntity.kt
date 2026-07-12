package com.betpro.android.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bet_history")
data class BetHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matchName: String,
    val status: String, // "Gagné", "Perdu", "Preuve Disponible", etc.
    val amountOrOdds: String,
    val isWin: Boolean,
    val timestamp: Long,
    val screenshotBase64: String? = null
)
