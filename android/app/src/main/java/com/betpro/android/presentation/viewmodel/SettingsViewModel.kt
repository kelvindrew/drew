package com.betpro.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betpro.android.domain.model.AppSettings
import com.betpro.android.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.appSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateSimulationMode(isSimulation: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSimulationMode(isSimulation)
        }
    }

    fun updateAIEngine(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAIEngine(isEnabled)
        }
    }

    fun updateBaseStake(stakeString: String) {
        viewModelScope.launch {
            val stake = stakeString.toDoubleOrNull()
            if (stake != null && stake > 0) {
                settingsRepository.updateBaseStake(stake)
            }
        }
    }

    fun updateStopLoss(stopLossString: String) {
        viewModelScope.launch {
            val stopLoss = stopLossString.toDoubleOrNull()
            if (stopLoss != null && stopLoss >= 0) {
                settingsRepository.updateStopLoss(stopLoss)
            }
        }
    }
}
