package com.betpro.android.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateNextStakeUseCaseTest {

    private lateinit var calculateNextStakeUseCase: CalculateNextStakeUseCase

    @Before
    fun setUp() {
        calculateNextStakeUseCase = CalculateNextStakeUseCase()
    }

    @Test
    fun `when last bet won, should return base stake`() {
        // Arrange
        val isLastBetWon = true
        val currentStake = 40.0
        val baseStake = 10.0

        // Act
        val nextStake = calculateNextStakeUseCase(isLastBetWon, currentStake, baseStake)

        // Assert
        assertEquals(10.0, nextStake, 0.0)
    }

    @Test
    fun `when last bet lost, should return double current stake`() {
        // Arrange
        val isLastBetWon = false
        val currentStake = 20.0
        val baseStake = 10.0

        // Act
        val nextStake = calculateNextStakeUseCase(isLastBetWon, currentStake, baseStake)

        // Assert
        assertEquals(40.0, nextStake, 0.0)
    }

    @Test
    fun `multiple losses should exponentially increase stake`() {
        // Simulate a losing streak starting at base stake 10
        var currentStake = 10.0

        currentStake = calculateNextStakeUseCase(false, currentStake, 10.0) // 1st loss
        assertEquals(20.0, currentStake, 0.0)

        currentStake = calculateNextStakeUseCase(false, currentStake, 10.0) // 2nd loss
        assertEquals(40.0, currentStake, 0.0)

        currentStake = calculateNextStakeUseCase(false, currentStake, 10.0) // 3rd loss
        assertEquals(80.0, currentStake, 0.0)
    }
}
