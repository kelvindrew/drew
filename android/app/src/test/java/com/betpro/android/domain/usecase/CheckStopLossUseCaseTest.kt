package com.betpro.android.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckStopLossUseCaseTest {

    private lateinit var checkStopLossUseCase: CheckStopLossUseCase

    @Before
    fun setUp() {
        checkStopLossUseCase = CheckStopLossUseCase()
    }

    @Test
    fun `when balance is above threshold, should return false`() {
        val currentBalance = 150.0
        val threshold = 100.0

        val shouldStop = checkStopLossUseCase(currentBalance, threshold)

        assertFalse(shouldStop)
    }

    @Test
    fun `when balance is exactly at threshold, should return true`() {
        val currentBalance = 100.0
        val threshold = 100.0

        val shouldStop = checkStopLossUseCase(currentBalance, threshold)

        assertTrue(shouldStop)
    }

    @Test
    fun `when balance drops below threshold, should return true`() {
        val currentBalance = 90.0
        val threshold = 100.0

        val shouldStop = checkStopLossUseCase(currentBalance, threshold)

        assertTrue(shouldStop)
    }
}
