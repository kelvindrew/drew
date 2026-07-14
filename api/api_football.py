import urllib.parse
from api.client import ApiClient
from utils.logger import logger

class APIFootballClient(ApiClient):
    def __init__(self):
        super().__init__()
        self.base_url = "https://v3.football.api-sports.io"
        self.api_key = self.config.get("api_football_key", "")
        self.headers = {
            "x-apisports-key": self.api_key
        }

    async def get_fixtures(self, date_str):
        """
        Fetch fixtures for a specific date (YYYY-MM-DD)
        """
        url = f"{self.base_url}/fixtures"
        params = {"date": date_str}
        logger.info(f"Fetching API-Football fixtures for date: {date_str}")
        return await self.get(url, headers=self.headers, params=params)

    async def get_team_statistics(self, league_id, season, team_id):
        url = f"{self.base_url}/teams/statistics"
        params = {
            "league": league_id,
            "season": season,
            "team": team_id
        }
        logger.info(f"Fetching stats for team {team_id} in league {league_id}")
        return await self.get(url, headers=self.headers, params=params)

    async def get_injuries(self, fixture_id):
        url = f"{self.base_url}/injuries"
        params = {"fixture": fixture_id}
        return await self.get(url, headers=self.headers, params=params)
