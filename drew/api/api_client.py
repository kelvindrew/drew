import json
import os
import requests
from .mock_data import generate_mock_events, generate_mock_odds_for_event

class APIClient:
    def __init__(self, config_path="drew/config.json"):
        with open(config_path, 'r') as f:
            self.config = json.load(f)

        self.odds_api_key = self.config.get('api_keys', {}).get('odds_api', '')
        self.football_api_key = self.config.get('api_keys', {}).get('football_api', '')

        self.use_mock = not self.odds_api_key or not self.football_api_key

        if self.use_mock:
            print("[API] Missing real API keys. Using Mock Data generators.")

    def fetch_upcoming_events(self):
        if self.use_mock:
            return generate_mock_events(40)

        # REAL The-Odds API integration
        url = "https://api.the-odds-api.com/v4/sports/soccer/events"
        params = {
            'apiKey': self.odds_api_key,
            'regions': 'eu',
        }
        try:
            response = requests.get(url, params=params)
            response.raise_for_status()
            data = response.json()
            events = []
            for item in data:
                events.append({
                    "id": item['id'],
                    "sport_key": item['sport_key'],
                    "sport_title": item['sport_title'],
                    "commence_time": item['commence_time'],
                    "home_team": item['home_team'],
                    "away_team": item['away_team'],
                    "competition": item.get('sport_title', 'Unknown'),
                    "status": "upcoming"
                })
            return events
        except Exception as e:
            print(f"[API ERROR] Events fetch failed: {e}")
            return []

    def fetch_odds_for_events(self, events):
        if self.use_mock:
            all_odds = []
            for event in events:
                all_odds.extend(generate_mock_odds_for_event(event['id']))
            return all_odds

        # REAL The-Odds API integration
        # In a real scenario, to save credits, we fetch odds per sport, not per event.
        # This is simplified for architecture demonstration.
        return []

    def fetch_advanced_stats(self, home_team, away_team):
        if self.use_mock:
            import random
            return {
                "home_elo": random.randint(1400, 2000),
                "away_elo": random.randint(1400, 2000),
                "home_form": random.uniform(0.1, 1.0),
                "away_form": random.uniform(0.1, 1.0),
                "injuries_impact": random.uniform(0, 0.3),
                "abnormal_odds_drop": random.choice([True, False, False, False])
            }

        # Real API-Football integration would happen here
        return {}
