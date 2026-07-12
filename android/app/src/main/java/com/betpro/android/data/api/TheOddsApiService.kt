package com.betpro.android.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface TheOddsApiService {
    @GET("v4/sports/soccer_ea_sports_fc/odds/")
    suspend fun getOdds(
        @Query("apiKey") apiKey: String,
        @Query("regions") regions: String = "eu",
        @Query("markets") markets: String = "h2h,totals,btts"
    ): List<TheOddsResponse>
}

data class TheOddsResponse(
    val id: String,
    val sport_key: String,
    val home_team: String,
    val away_team: String,
    val bookmakers: List<Bookmaker>
)

data class Bookmaker(
    val key: String,
    val markets: List<Market>
)

data class Market(
    val key: String,
    val outcomes: List<Outcome>
)

data class Outcome(
    val name: String,
    val price: Double
)
