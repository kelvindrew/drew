import random

class ConfidenceScorer:
    def __init__(self, api_client):
        self.api_client = api_client

    def calculate_score(self, event, odd):
        """
        Calculates a confidence score out of 100 based on simulated advanced stats.
        """
        stats = self.api_client.fetch_advanced_stats(event['home_team'], event['away_team'])

        base_score = 50

        # Calculate Elo difference advantage (simplified)
        elo_diff = abs(stats['home_elo'] - stats['away_elo'])
        elo_bonus = min(20, elo_diff / 50)

        # Form bonus
        form_bonus = (stats['home_form'] + stats['away_form']) * 10

        # Risk detection
        risk_flags = []
        risk_penalty = 0

        if stats['injuries_impact'] > 0.15:
            risk_flags.append("Blessures importantes")
            risk_penalty += 15

        if stats['abnormal_odds_drop']:
            risk_flags.append("Baisse anormale des cotes")
            # Sometimes odds drop means good value, sometimes it's suspicious.
            risk_penalty += 5

        # Price penalty: Higher odds are inherently riskier
        price_penalty = min(30, (odd['price'] - 1.0) * 10)

        final_score = base_score + elo_bonus + form_bonus - risk_penalty - price_penalty

        # Ensure score is between 1 and 99
        final_score = max(1, min(99, final_score))

        # If it's a youth/reserve team, we penalize heavily (as requested)
        if "U19" in event['home_team'] or "U17" in event['home_team']:
            final_score = 0
            risk_flags.append("Compétition mineure interdite")

        return round(final_score, 1), risk_flags
