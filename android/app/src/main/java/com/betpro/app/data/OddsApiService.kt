package com.betpro.app.data

import retrofit2.http.GET
import retrofit2.http.Query

interface OddsApiService {
    @GET("v4/sports/upcoming/odds/")
    suspend fun getUpcomingOdds(
        @Query("apiKey") apiKey: String,
        @Query("regions") regions: String = "eu,uk",
        @Query("markets") markets: String = "h2h",
        @Query("bookmakers") bookmakers: String = "betfair,pinnacle"
    ): List<EventResponse>
}

data class EventResponse(
    val id: String,
    val sport_key: String,
    val sport_title: String,
    val commence_time: String,
    val home_team: String,
    val away_team: String,
    val bookmakers: List<Bookmaker>
)

data class Bookmaker(
    val key: String,
    val title: String,
    val last_update: String,
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
