package com.connect.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connect.ai.SmartCoachAI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val aiCoach: SmartCoachAI
) : ViewModel() {

    private val _aiRecommendation = MutableStateFlow("Analyse de vos données en cours...")
    val aiRecommendation: StateFlow<String> = _aiRecommendation.asStateFlow()

    init {
        fetchRecommendation()
    }

    private fun fetchRecommendation() {
        viewModelScope.launch {
            val fakeHealthData = "BPM: 72, Pas: 8500, Sommeil: 7h30, Batterie: 85%"
            try {
                aiCoach.generateRecommendations(fakeHealthData).collectLatest { recommendation ->
                    _aiRecommendation.value = recommendation
                }
            } catch (e: Exception) {
                _aiRecommendation.value = "Impossible de générer la recommandation (Clé API de test)."
            }
        }
    }
}
