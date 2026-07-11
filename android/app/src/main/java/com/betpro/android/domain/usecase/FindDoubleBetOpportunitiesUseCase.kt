package com.betpro.android.domain.usecase

import com.betpro.android.domain.model.SportEvent
import javax.inject.Inject

class FindDoubleBetOpportunitiesUseCase @Inject constructor() {

    /**
     * Trouve des paires de matchs dont le produit des cotes (pour une sélection spécifique)
     * est compris entre 3.0 et 3.5.
     * Pour simplifier, on cherche la combinaison de victoires à domicile (odds1) des deux matchs.
     */
    operator fun invoke(events: List<SportEvent>): List<Pair<SportEvent, SportEvent>> {
        val opportunities = mutableListOf<Pair<SportEvent, SportEvent>>()

        for (i in events.indices) {
            for (j in i + 1 until events.size) {
                val event1 = events[i]
                val event2 = events[j]

                // On pourrait vérifier d'autres marchés (oddsX, odds2, btts), mais on illustre avec odds1
                val combinedOdds = event1.odds1 * event2.odds1

                if (combinedOdds in 3.0..3.5) {
                    opportunities.add(Pair(event1, event2))
                }
            }
        }

        return opportunities
    }
}
