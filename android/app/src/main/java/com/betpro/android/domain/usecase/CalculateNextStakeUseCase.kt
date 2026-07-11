package com.betpro.android.domain.usecase

import javax.inject.Inject

class CalculateNextStakeUseCase @Inject constructor() {

    /**
     * Calcule la prochaine mise selon la stratégie de Martingale.
     * @param isLastBetWon True si le dernier pari était gagnant, False sinon.
     * @param currentStake La mise du dernier pari.
     * @param baseStake La mise de base (initiale).
     * @return La nouvelle mise à placer.
     */
    operator fun invoke(isLastBetWon: Boolean, currentStake: Double, baseStake: Double): Double {
        return if (isLastBetWon) {
            baseStake
        } else {
            currentStake * 2.0
        }
    }
}
