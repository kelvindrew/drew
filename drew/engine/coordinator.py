from datetime import datetime
from drew.api.api_client import APIClient
from drew.database.db_manager import DatabaseManager
from drew.engine.scorer import ConfidenceScorer
from drew.engine.combination import CombinationEngine

class Coordinator:
    def __init__(self, db_path="drew_data.sqlite"):
        self.api = APIClient()
        self.db = DatabaseManager(db_path)
        self.scorer = ConfidenceScorer(self.api)
        self.combinator = CombinationEngine(self.db)

    def refresh_data(self):
        """
        The hourly task: fetches events, fetches odds, scores them, and saves to DB.
        """
        print(f"[{datetime.now().strftime('%H:%M:%S')}] Starting data refresh...")
        events = self.api.fetch_upcoming_events()
        for event in events:
            self.db.save_event(event)

        odds = self.api.fetch_odds_for_events(events)
        for odd in odds:
            # Find the full event details for scoring
            event_details = next((e for e in events if e['id'] == odd['event_id']), None)
            if event_details:
                score, flags = self.scorer.calculate_score(event_details, odd)
                odd['confidence_score'] = score
                odd['risk_flags'] = flags
                self.db.save_odd(odd)

        print(f"[{datetime.now().strftime('%H:%M:%S')}] Data refresh complete. Saved {len(events)} events and {len(odds)} odds.")

    def get_combinations(self, min_odds, max_odds):
        combos = self.combinator.find_best_combinations(min_odds, max_odds)
        for combo in combos:
            odd_ids = [item['odd_id'] for item in combo['items']]
            # Save generated combo to history
            self.db.save_combination(min_odds, max_odds, combo['total_odds'], combo['avg_confidence'], odd_ids)
        return combos
