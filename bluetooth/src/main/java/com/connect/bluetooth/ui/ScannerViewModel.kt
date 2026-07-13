package com.connect.bluetooth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connect.bluetooth.BleScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scannerManager: BleScannerManager
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _deviceFound = MutableStateFlow(false)
    val deviceFound: StateFlow<Boolean> = _deviceFound.asStateFlow()

    fun startScan() {
        _isScanning.value = true
        _deviceFound.value = false

        // Appelle le vrai scanner
        scannerManager.startScan()

        // Simulation de délai pour la démo UI
        viewModelScope.launch {
            delay(3000)
            _deviceFound.value = true
            _isScanning.value = false
            scannerManager.stopScan()
        }
    }
}
