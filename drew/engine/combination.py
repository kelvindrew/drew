import itertools
from operator import itemgetter

class CombinationEngine:
    def __init__(self, db_manager):
        self.db = db_manager

    def find_best_combinations(self, target_min, target_max, max_events=3):
        active_odds = self.db.get_active_odds()

        # Filter out odds with terrible confidence or fatal risk flags
        valid_odds = [o for o in active_odds if o['confidence_score'] is not None and o['confidence_score'] > 50]

        valid_combinations = []

        # Try finding combinations of 2 events first (preferred)
        for combo_size in range(2, max_events + 1):
            for combo in itertools.combinations(valid_odds, combo_size):
                # Ensure all odds in combo are from different events
                event_ids = [odd['event_id'] for odd in combo]
                if len(set(event_ids)) != combo_size:
                    continue

                total_odd = 1.0
                for odd in combo:
                    total_odd *= odd['price']

                if target_min <= total_odd <= target_max:
                    avg_conf = sum([o['confidence_score'] for o in combo]) / combo_size
                    valid_combinations.append({
                        'items': combo,
                        'total_odds': round(total_odd, 2),
                        'avg_confidence': round(avg_conf, 1),
                        'size': combo_size
                    })

        # Sort by best confidence first, then by smaller size (prefer 2 over 3)
        valid_combinations.sort(key=lambda x: (x['avg_confidence'], -x['size']), reverse=True)

        # Return top 5
        return valid_combinations[:5]
