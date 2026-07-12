package com.betpro.android.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiFootballService {
    @GET("fixtures")
    suspend fun getFixtures(
        @Header("x-apisports-key") apiKey: String,
        @Query("date") date: String,
        @Query("timezone") timezone: String = "Africa/Kinshasa"
    ): ApiFootballResponse
}

data class ApiFootballResponse(
    val response: List<FixtureResponse>
)

data class FixtureResponse(
    val fixture: FixtureDetails,
    val teams: Teams
)

data class FixtureDetails(
    val id: Int,
    val date: String,
    val timestamp: Long
)

data class Teams(
    val home: Team,
    val away: Team
)

data class Team(
    val name: String
)
