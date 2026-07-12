package com.betpro.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betpro.android.data.local.room.BetHistoryEntity
import com.betpro.android.domain.repository.BetHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    betHistoryRepository: BetHistoryRepository
) : ViewModel() {

    val betHistory: StateFlow<List<BetHistoryEntity>> = betHistoryRepository.getAllBetsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
