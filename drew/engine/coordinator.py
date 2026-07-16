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
        print(f"[{datetime.now().strftime('%H:%M:%S')}] Starting data refresh...")
        events = self.api.fetch_upcoming_events()
        for event in events:
            self.db.save_event(event)

        odds = self.api.fetch_odds_for_events(events)
        for odd in odds:
            event_details = next((e for e in events if e['id'] == odd['event_id']), None)
            if event_details:
                score, flags, ev_data = self.scorer.calculate_score_and_ev(event_details, odd)
                odd['confidence_score'] = score
                odd['risk_flags'] = flags
                # Store EV data within risk_flags or a new field in real production,
                # here we attach it dynamically for the current session.
                odd['true_probability'] = ev_data['true_probability']
                odd['implied_odds'] = ev_data['implied_odds']
                odd['expected_value'] = ev_data['expected_value']
                self.db.save_odd(odd)

        print(f"[{datetime.now().strftime('%H:%M:%S')}] Data refresh complete.")

    def get_combinations(self, min_odds, max_odds):
        combos = self.combinator.find_best_combinations(min_odds, max_odds)
        for combo in combos:
            odd_ids = [item['odd_id'] for item in combo['items']]
            self.db.save_combination(min_odds, max_odds, combo['total_odds'], combo['avg_confidence'], odd_ids)
        return combos

    def get_value_bets(self):
        """ Returns odds that have the highest Expected Value (EV+) """
        active_odds = self.db.get_active_odds()
        # Since EV isn't strictly saved in DB schema yet, we recalculate it quickly for display
        value_bets = []
        for o in active_odds:
            # We reconstruct the event details for scoring
            event = {
                'home_team': o['home_team'],
                'away_team': o['away_team']
            }
            _, _, ev_data = self.scorer.calculate_score_and_ev(event, o)
            if ev_data['expected_value'] > 2.0: # Only return EV > 2%
                o.update(ev_data)
                value_bets.append(o)

        # Sort by best EV
        value_bets.sort(key=lambda x: x['expected_value'], reverse=True)
        return value_bets[:15]
