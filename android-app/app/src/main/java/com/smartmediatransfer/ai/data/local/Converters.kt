package com.smartmediatransfer.ai.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromTransferState(state: TransferState): String {
        return state.name
    }

    @TypeConverter
    fun toTransferState(name: String): TransferState {
        return try {
            TransferState.valueOf(name)
        } catch (e: IllegalArgumentException) {
            TransferState.ERROR
        }
    }

    @TypeConverter
    fun fromTransferMode(mode: TransferMode): String {
        return mode.name
    }

    @TypeConverter
    fun toTransferMode(name: String): TransferMode {
        return try {
            TransferMode.valueOf(name)
        } catch (e: IllegalArgumentException) {
            TransferMode.COPY
        }
    }
}
