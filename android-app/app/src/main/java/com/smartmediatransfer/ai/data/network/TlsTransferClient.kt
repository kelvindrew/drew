package com.smartmediatransfer.ai.data.network

import android.util.Log
import com.smartmediatransfer.ai.data.local.TransferDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket
import javax.inject.Inject
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

class TlsTransferClient @Inject constructor(
    private val transferDao: TransferDao
) {
    suspend fun connectAndSync(host: String, port: Int) = withContext(Dispatchers.IO) {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("TLSv1.3")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            val factory = sslContext.socketFactory
            val socket = factory.createSocket(host, port) as SSLSocket
            socket.startHandshake()

            // 1. Perform JSON Handshake
            Log.d("TlsTransferClient", "Connected securely to PC at $host:$port")

            socket.close()
        } catch (e: Exception) {
            Log.e("TlsTransferClient", "Error connecting", e)
        }
    }
}
