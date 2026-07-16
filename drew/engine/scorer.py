import random

class ConfidenceScorer:
    def __init__(self, api_client):
        self.api_client = api_client

    def calculate_score_and_ev(self, event, odd):
        stats = self.api_client.fetch_advanced_stats(event['home_team'], event['away_team'])

        base_score = 50

        # Elo bonus
        elo_diff = abs(stats['home_elo'] - stats['away_elo'])
        elo_bonus = min(20, elo_diff / 50)

        # Form
        form_bonus = (stats['home_form'] + stats['away_form']) * 10

        # Risks
        risk_flags = []
        risk_penalty = 0

        if stats['injuries_impact'] > 0.15:
            risk_flags.append("Blessures importantes")
            risk_penalty += 15

        if stats['abnormal_odds_drop']:
            risk_flags.append("Baisse anormale des cotes")
            risk_penalty += 5

        price_penalty = min(30, (odd['price'] - 1.0) * 10)

        final_score = base_score + elo_bonus + form_bonus - risk_penalty - price_penalty
        final_score = max(1, min(99, final_score))

        if "U19" in event['home_team'] or "U17" in event['home_team']:
            final_score = 0
            risk_flags.append("Compétition mineure")

        # --- Expected Value Calculation ---
        # Bookmaker implied probability: 1 / odds
        implied_prob = (1 / odd['price']) * 100

        # We estimate "True Probability" based on our confidence score as a proxy
        # In real life, True Prob is derived from historical distribution.
        # Here we simulate true prob slightly above or below implied prob.
        true_prob = implied_prob + random.uniform(-10.0, 15.0)
        true_prob = max(1.0, min(99.0, true_prob))

        # True Odds
        implied_odds_val = 100 / true_prob

        # Expected Value = (True Prob * Win Amount) - (Loss Prob * Loss Amount)
        # Simplified EV% = (True_Prob_Decimal * Decimal_Odds) - 1
        ev = ((true_prob / 100) * odd['price']) - 1
        ev_percent = round(ev * 100, 2)

        ev_data = {
            'true_probability': round(true_prob, 1),
            'implied_odds': round(implied_odds_val, 2),
            'expected_value': ev_percent
        }

        return round(final_score, 1), risk_flags, ev_data
