import httpx
from utils.logger import logger
import json

class BettingBotClient:
    def __init__(self):
        try:
            with open("config.json", "r") as f:
                self.config = json.load(f).get("bot", {})
        except:
            self.config = {}

        self.endpoint = self.config.get("webhook_url", "http://localhost:3000/api/place-bet")
        self.token = self.config.get("auth_token", "default_token")

    async def place_bet(self, match_id, selection, odds, amount):
        """
        Sends a webhook to the Node.js backend to automate the bet via Playwright.
        """
        logger.info(f"Triggering Node.js Bot for bet: Match {match_id} | {selection} @ {odds} | {amount}€")
        payload = {
            "match_id": match_id,
            "selection": selection,
            "odds": odds,
            "amount": amount
        }
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }

        try:
            async with httpx.AsyncClient(timeout=10) as client:
                res = await client.post(self.endpoint, json=payload, headers=headers)
                res.raise_for_status()
                return {"success": True, "details": res.json()}
        except Exception as e:
            logger.error(f"Failed to trigger Node.js Betting Bot: {e}")
            return {"success": False, "error": str(e)}
