import random
import uuid
from datetime import datetime, timedelta

TEAMS = ["PSG", "Marseille", "Lyon", "Monaco", "Lille", "Lens", "Rennes", "Nice", "Real Madrid", "Barcelona", "Atletico Madrid", "Sevilla", "Arsenal", "Man City", "Liverpool", "Chelsea"]
COMPETITIONS = ["Ligue 1", "La Liga", "Premier League", "Champions League"]
MARKETS = ["Victoire domicile", "Victoire extérieur", "Match nul", "Double chance", "Plus de 2.5 buts", "Moins de 2.5 buts", "Les deux équipes marquent"]

def generate_mock_events(num_events=20):
    events = []
    for _ in range(num_events):
        team1, team2 = random.sample(TEAMS, 2)
        event = {
            "id": str(uuid.uuid4()),
            "sport_key": "soccer",
            "sport_title": "Soccer",
            "commence_time": (datetime.now() + timedelta(hours=random.randint(1, 72))).isoformat(),
            "home_team": team1,
            "away_team": team2,
            "competition": random.choice(COMPETITIONS),
            "status": "upcoming"
        }
        events.append(event)
    return events

def generate_mock_odds_for_event(event_id):
    odds = []
    num_markets = random.randint(3, len(MARKETS))
    selected_markets = random.sample(MARKETS, num_markets)

    for market in selected_markets:
        # Generate slightly realistic odds
        if market == "Double chance":
            price = round(random.uniform(1.10, 1.90), 2)
        elif "2.5" in market:
            price = round(random.uniform(1.50, 2.50), 2)
        else:
            price = round(random.uniform(1.20, 5.00), 2)

        odd = {
            "event_id": event_id,
            "market_key": market,
            "selection_name": market,
            "price": price,
            # We don't generate confidence here, the engine will do it
        }
        odds.append(odd)
    return odds
