package com.smartmediatransfer.ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransferState {
    PENDING, IN_PROGRESS, PAUSED, COMPLETED, ERROR, VERIFIED
}

enum class TransferMode {
    COPY, MOVE
}

@Entity(tableName = "transfers")
data class TransferRecordEntity(
    @PrimaryKey val transferId: String,
    val fileId: String,
    val fileHash: String,
    val fileName: String,
    val originalPath: String,
    val destinationPath: String,
    val sizeBytes: Long,
    val bytesTransferred: Long,
    val deviceId: String,
    val fileCreationDate: Long,
    val addedToQueueDate: Long,
    val transferCompletionDate: Long?,
    val category: String,
    val priority: Int,
    val state: TransferState,
    val mode: TransferMode
)

@Entity(tableName = "trusted_devices")
data class TrustedDeviceEntity(
    @PrimaryKey val deviceId: String,
    val deviceName: String,
    val publicKey: String,
    val firstSeen: Long,
    val lastConnected: Long,
    val isAuthorized: Boolean
)
