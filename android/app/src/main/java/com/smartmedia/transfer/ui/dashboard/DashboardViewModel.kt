package com.smartmedia.transfer.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartmedia.transfer.data.scanner.MediaStoreScanner
import com.smartmedia.transfer.domain.engine.AnalysisResult
import com.smartmedia.transfer.domain.engine.HeuristicEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val scanner: MediaStoreScanner,
    private val engine: HeuristicEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Initial)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Scanning
            try {
                val files = scanner.scanFiles()
                val analysis = engine.analyzeFiles(files)
                _uiState.value = DashboardUiState.Success(analysis)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class DashboardUiState {
    object Initial : DashboardUiState()
    object Scanning : DashboardUiState()
    data class Success(val result: AnalysisResult) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
