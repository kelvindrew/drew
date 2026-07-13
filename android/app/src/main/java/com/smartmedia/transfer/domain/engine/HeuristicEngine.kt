package com.smartmedia.transfer.domain.engine

import com.smartmedia.transfer.domain.model.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeuristicEngine @Inject constructor() {
    suspend fun analyzeFiles(files: List<MediaFile>): AnalysisResult = withContext(Dispatchers.Default) {
        val statsByCategory = files.groupBy { it.category }
            .mapValues { it.value.sumOf { file -> file.size } }

        val sixMonthsAgo = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)

        val recommendedForTransfer = files.filter {
            it.size > 100 * 1024 * 1024 || it.dateModified < sixMonthsAgo
        }.sortedByDescending { it.size }

        return@withContext AnalysisResult(
            totalScanned = files.size,
            totalSize = files.sumOf { it.size },
            statsByCategory = statsByCategory,
            recommendedFiles = recommendedForTransfer.take(50),
            recoverableSpace = recommendedForTransfer.sumOf { it.size }
        )
    }

    suspend fun computeSHA256(path: String): String = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) return@withContext ""

        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hashBytes = digest.digest()
        hashBytes.joinToString("") { "%02x".format(it) }
    }
}

data class AnalysisResult(
    val totalScanned: Int,
    val totalSize: Long,
    val statsByCategory: Map<com.smartmedia.transfer.domain.model.FileCategory, Long>,
    val recommendedFiles: List<MediaFile>,
    val recoverableSpace: Long
)
