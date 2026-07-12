package com.betpro.app.data

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface BackendApiService {
    @POST("api/bot/place-bet")
    suspend fun placeBet(
        @Header("Authorization") authHeader: String,
        @Body request: PlaceBetRequest
    ): PlaceBetResponse
}

data class PlaceBetRequest(
    val eventId: String,
    val amount: Double,
    val odds: Double,
    val bookmaker: String
)

data class PlaceBetResponse(
    val success: Boolean,
    val error: String?
)
