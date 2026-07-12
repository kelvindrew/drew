package com.smartmediatransfer.ai.data.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MdnsDiscoveryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val SERVICE_TYPE = "_smartmedia._tcp."

    private val _discoveredDevices = MutableStateFlow<List<NsdServiceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<NsdServiceInfo>> = _discoveredDevices

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d("MdnsDiscovery", "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d("MdnsDiscovery", "Service discovery success $service")
            if (service.serviceType == SERVICE_TYPE) {
                nsdManager.resolveService(service, resolveListener)
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.e("MdnsDiscovery", "service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i("MdnsDiscovery", "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("MdnsDiscovery", "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("MdnsDiscovery", "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e("MdnsDiscovery", "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e("MdnsDiscovery", "Resolve Succeeded. $serviceInfo")
            val currentList = _discoveredDevices.value.toMutableList()
            if (currentList.none { it.serviceName == serviceInfo.serviceName }) {
                currentList.add(serviceInfo)
                _discoveredDevices.value = currentList
            }
        }
    }

    fun startDiscovery() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stopDiscovery() {
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
            Log.e("MdnsDiscovery", "Error stopping discovery", e)
        }
    }
}
