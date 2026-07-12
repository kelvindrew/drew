package com.betpro.android.data.repository

import com.betpro.android.data.api.ApiFootballService
import com.betpro.android.data.api.TheOddsApiService
import com.betpro.android.data.api.TheOddsResponse
import com.betpro.android.domain.model.SportEvent
import com.betpro.android.domain.repository.SportEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

import com.betpro.android.BuildConfig

import com.betpro.android.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class SportEventRepositoryImpl @Inject constructor(
    private val footballApi: ApiFootballService,
    private val oddsApi: TheOddsApiService,
    private val settingsRepository: SettingsRepository
) : SportEventRepository {

    private val footballApiKey = BuildConfig.API_FOOTBALL_KEY
    private val oddsApiKey = BuildConfig.THE_ODDS_KEY

    override suspend fun getUpcomingEvents(): List<SportEvent> = withContext(Dispatchers.IO) {
        try {
            val isSimulation = settingsRepository.appSettingsFlow.first().isSimulationMode
            if (isSimulation) {
                return@withContext getIntelligentMockEvents()
            }

            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val footballDeferred = async {
                // Catching exception to allow mock compilation
                try {
                    footballApi.getFixtures(footballApiKey, todayDate).response
                } catch (e: Exception) {
                    emptyList()
                }
            }
            val oddsDeferred = async {
                try {
                    oddsApi.getOdds(oddsApiKey)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val fixtures = footballDeferred.await()
            val oddsData = oddsDeferred.await()

            val mergedEvents = mutableListOf<SportEvent>()

            for (fixture in fixtures) {
                val bestMatch = findBestMatch(fixture.teams.home.name, fixture.teams.away.name, oddsData)
                if (bestMatch != null) {
                    val event = parseEvent(fixture.fixture.id.toString(), fixture.teams.home.name, fixture.teams.away.name, fixture.fixture.timestamp * 1000, bestMatch)
                    if (event != null) {
                        mergedEvents.add(event)
                    }
                }
            }

            // Return mock data if APIs fail to facilitate UI testing
            if (mergedEvents.isEmpty()) {
                 return@withContext getMockEvents()
            }

            mergedEvents
        } catch (e: Exception) {
            e.printStackTrace()
            getMockEvents()
        }
    }

    // Fuzzy Matching Logic (Simplified Levenshtein or token matching)
    private fun findBestMatch(homeTeam: String, awayTeam: String, oddsData: List<TheOddsResponse>): TheOddsResponse? {
        var bestMatch: TheOddsResponse? = null
        var maxScore = 0.0

        for (odds in oddsData) {
            val homeScore = similarity(homeTeam.lowercase(), odds.home_team.lowercase())
            val awayScore = similarity(awayTeam.lowercase(), odds.away_team.lowercase())

            val totalScore = homeScore + awayScore
            if (totalScore > maxScore && totalScore > 1.2) { // Arbitrary threshold
                maxScore = totalScore
                bestMatch = odds
            }
        }
        return bestMatch
    }

    // Very basic Jaccard-like similarity for team names
    private fun similarity(s1: String, s2: String): Double {
        val set1 = s1.split(" ").toSet()
        val set2 = s2.split(" ").toSet()
        val intersection = set1.intersect(set2).size.toDouble()
        val union = set1.union(set2).size.toDouble()
        return if (union == 0.0) 0.0 else intersection / union
    }

    private fun parseEvent(id: String, home: String, away: String, startTime: Long, oddsData: TheOddsResponse): SportEvent? {
        val bookmaker = oddsData.bookmakers.firstOrNull() ?: return null

        var odds1 = 0.0
        var oddsX = 0.0
        var odds2 = 0.0
        var oddsBttsYes: Double? = null
        var oddsOver25: Double? = null

        bookmaker.markets.forEach { market ->
            when (market.key) {
                "h2h" -> {
                    market.outcomes.forEach { outcome ->
                        when (outcome.name) {
                            oddsData.home_team -> odds1 = outcome.price
                            "Draw" -> oddsX = outcome.price
                            oddsData.away_team -> odds2 = outcome.price
                        }
                    }
                }
                "btts" -> {
                    market.outcomes.firstOrNull { it.name == "Yes" }?.let { oddsBttsYes = it.price }
                }
                "totals" -> {
                    market.outcomes.firstOrNull { it.name == "Over" }?.let { oddsOver25 = it.price } // Assuming default total is 2.5
                }
            }
        }

        if (odds1 == 0.0 || oddsX == 0.0 || odds2 == 0.0) return null

        return SportEvent(id, home, away, startTime, odds1, oddsX, odds2, oddsBttsYes, oddsOver25)
    }

    private fun getMockEvents(): List<SportEvent> {
        return listOf(
            SportEvent("1", "Real Madrid", "Manchester City", System.currentTimeMillis() + 3600000, 2.65, 3.40, 2.50, 1.65, 1.80),
            SportEvent("2", "Arsenal", "Chelsea", System.currentTimeMillis() + 86400000, 1.85, 3.60, 4.20, 1.90, 2.10)
        )
    }

    private fun getIntelligentMockEvents(): List<SportEvent> {
        // Generates somewhat dynamic realistic data for testing
        val teams = listOf("PSG", "Bayern", "Liverpool", "Juventus", "AC Milan", "Inter", "Ajax", "Porto")
        val shuffledTeams = teams.shuffled()

        return listOf(
            SportEvent(
                id = "mock_${System.currentTimeMillis()}_1",
                homeTeam = shuffledTeams[0],
                awayTeam = shuffledTeams[1],
                startTime = System.currentTimeMillis() + 3600000,
                odds1 = 1.5 + Math.random(),
                oddsX = 3.0 + Math.random(),
                odds2 = 4.0 + Math.random(),
                oddsBttsYes = 1.5 + Math.random(),
                oddsOver25 = 1.6 + Math.random()
            ),
            SportEvent(
                id = "mock_${System.currentTimeMillis()}_2",
                homeTeam = shuffledTeams[2],
                awayTeam = shuffledTeams[3],
                startTime = System.currentTimeMillis() + 7200000,
                odds1 = 2.5 + Math.random(),
                oddsX = 3.2 + Math.random(),
                odds2 = 2.7 + Math.random(),
                oddsBttsYes = 1.8 + Math.random(),
                oddsOver25 = 1.9 + Math.random()
            )
        )
    }
}
