from api.client import ApiClient
from utils.logger import logger

class TheOddsClient(ApiClient):
    def __init__(self):
        super().__init__()
        self.base_url = "https://api.the-odds-api.com/v4"
        self.api_key = self.config.get("the_odds_api_key", "")

    async def get_sports(self):
        url = f"{self.base_url}/sports"
        params = {"apiKey": self.api_key}
        logger.info("Fetching active sports from The-Odds API")
        return await self.get(url, params=params)

    async def get_odds(self, sport_key="upcoming", regions="eu", markets="h2h"):
        url = f"{self.base_url}/sports/{sport_key}/odds"
        params = {
            "apiKey": self.api_key,
            "regions": regions,
            "markets": markets,
            "oddsFormat": "decimal"
        }
        logger.info(f"Fetching odds for sport: {sport_key}")
        return await self.get(url, params=params)
