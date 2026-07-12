package com.betpro.android.domain.usecase

import javax.inject.Inject

class CheckStopLossUseCase @Inject constructor() {

    /**
     * Vérifie si le stop-loss a été atteint.
     * @param currentBalance Le solde actuel du portefeuille.
     * @param stopLossThreshold Le seuil en dessous duquel on arrête de parier.
     * @return True si le solde est inférieur ou égal au seuil (donc on doit arrêter).
     */
    operator fun invoke(currentBalance: Double, stopLossThreshold: Double): Boolean {
        return currentBalance <= stopLossThreshold
    }
}
