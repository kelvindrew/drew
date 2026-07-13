package com.smartmedia.transfer.data.network

import com.smartmedia.transfer.domain.model.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileInputStream
import java.net.Socket
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureTransferManager @Inject constructor() {
    private val AES_KEY = "12345678901234561234567890123456".toByteArray()
    private val AES_IV = "1234567890123456".toByteArray()

    fun transferFile(
        file: MediaFile,
        host: String,
        port: Int
    ): Flow<TransferProgress> = flow {
        var socket: Socket? = null
        try {
            socket = Socket(host, port)
            val outputStream = socket.getOutputStream()

            val nameBytes = file.name.toByteArray(Charsets.UTF_8)
            outputStream.write(nameBytes.size)
            outputStream.write(nameBytes)

            val secretKey = SecretKeySpec(AES_KEY, "AES")
            val ivParameterSpec = IvParameterSpec(AES_IV)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

            val cipherOutputStream = CipherOutputStream(outputStream, cipher)
            val fileInputStream = FileInputStream(File(file.path))

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0L

            emit(TransferProgress(file.id, 0f, TransferState.IN_PROGRESS))

            fileInputStream.use { fis ->
                cipherOutputStream.use { cos ->
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        cos.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        emit(TransferProgress(file.id, totalRead.toFloat() / file.size.toFloat(), TransferState.IN_PROGRESS))
                    }
                }
            }
            emit(TransferProgress(file.id, 1f, TransferState.COMPLETED))
        } catch (e: Exception) {
            emit(TransferProgress(file.id, 0f, TransferState.FAILED, e.message))
        } finally {
            socket?.close()
        }
    }.flowOn(Dispatchers.IO)
}

data class TransferProgress(val fileId: Long, val progress: Float, val state: TransferState, val error: String? = null)
enum class TransferState { IN_PROGRESS, COMPLETED, FAILED }
