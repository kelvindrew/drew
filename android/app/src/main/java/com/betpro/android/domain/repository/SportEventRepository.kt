package com.betpro.android.domain.repository

import com.betpro.android.domain.model.SportEvent

interface SportEventRepository {
    suspend fun getUpcomingEvents(): List<SportEvent>
}
