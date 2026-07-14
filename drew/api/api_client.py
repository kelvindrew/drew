import json
import os
from .mock_data import generate_mock_events, generate_mock_odds_for_event

class APIClient:
    def __init__(self, config_path="drew/config.json"):
        with open(config_path, 'r') as f:
            self.config = json.load(f)

    def fetch_upcoming_events(self):
        """
        Simulate fetching events from API.
        In a real scenario, this would use self.config['api_keys']['odds_api']
        to call The-Odds API.
        """
        return generate_mock_events(30)

    def fetch_odds_for_events(self, events):
        """
        Simulate fetching odds for given events.
        """
        all_odds = []
        for event in events:
            all_odds.extend(generate_mock_odds_for_event(event['id']))
        return all_odds

    def fetch_advanced_stats(self, home_team, away_team):
        """
        Simulate fetching advanced stats (Elo, form, injuries) from API 4.
        """
        import random
        return {
            "home_elo": random.randint(1400, 2000),
            "away_elo": random.randint(1400, 2000),
            "home_form": random.uniform(0.1, 1.0),
            "away_form": random.uniform(0.1, 1.0),
            "injuries_impact": random.uniform(0, 0.3), # 0 = no injuries, 0.3 = severe
            "abnormal_odds_drop": random.choice([True, False, False, False])
        }
