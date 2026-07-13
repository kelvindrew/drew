package com.smartmedia.transfer.domain.model

data class MediaFile(
    val id: Long,
    val name: String,
    val path: String,
    val size: Long,
    val dateModified: Long,
    val category: FileCategory,
    val hash: String? = null
)

enum class FileCategory {
    VIDEO, PHOTO, AUDIO, DOCUMENT, APK, ARCHIVE, OTHER
}
