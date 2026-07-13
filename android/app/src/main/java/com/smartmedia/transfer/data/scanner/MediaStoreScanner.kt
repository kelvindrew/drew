package com.smartmedia.transfer.data.scanner

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.smartmedia.transfer.domain.model.FileCategory
import com.smartmedia.transfer.domain.model.MediaFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun scanFiles(): List<MediaFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val uri: Uri = MediaStore.Files.getContentUri("external")

        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn)
                if (path == null || !File(path).exists()) continue

                val name = cursor.getString(nameColumn) ?: "Unknown"
                val size = cursor.getLong(sizeColumn)
                if (size <= 0) continue

                val dateModified = cursor.getLong(dateModifiedColumn)
                val mimeType = cursor.getString(mimeTypeColumn) ?: ""

                val category = determineCategory(mimeType, name)

                files.add(
                    MediaFile(
                        id = cursor.getLong(idColumn),
                        name = name,
                        path = path,
                        size = size,
                        dateModified = dateModified * 1000L,
                        category = category
                    )
                )
            }
        }

        return@withContext files
    }

    private fun determineCategory(mimeType: String, name: String): FileCategory {
        val lowerName = name.lowercase()
        return when {
            mimeType.startsWith("video/") -> FileCategory.VIDEO
            mimeType.startsWith("image/") -> FileCategory.PHOTO
            mimeType.startsWith("audio/") -> FileCategory.AUDIO
            mimeType == "application/vnd.android.package-archive" || lowerName.endsWith(".apk") -> FileCategory.APK
            mimeType == "application/zip" || lowerName.endsWith(".zip") || lowerName.endsWith(".rar") -> FileCategory.ARCHIVE
            mimeType.startsWith("application/pdf") || mimeType.startsWith("text/") -> FileCategory.DOCUMENT
            else -> FileCategory.OTHER
        }
    }
}
