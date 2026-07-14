import httpx
import asyncio
import json
from utils.logger import logger

class ApiClient:
    def __init__(self):
        try:
            with open("config.json", "r", encoding="utf-8") as f:
                self.config = json.load(f)["api"]
        except Exception as e:
            logger.error("Failed to load API config", exc_info=True)
            self.config = {"timeout": 30, "max_retries": 3}

        self.client = httpx.AsyncClient(timeout=self.config.get("timeout", 30))

    async def get(self, url, headers=None, params=None):
        retries = self.config.get("max_retries", 3)
        for attempt in range(retries):
            try:
                response = await self.client.get(url, headers=headers, params=params)
                response.raise_for_status()
                return response.json()
            except httpx.HTTPStatusError as e:
                logger.error(f"HTTP error on {url}: {e.response.status_code}")
                # Don't retry on 4xx errors usually, unless it's 429 Too Many Requests
                if e.response.status_code != 429 and e.response.status_code < 500:
                    break
            except httpx.RequestError as e:
                logger.warning(f"Request error on {url} (Attempt {attempt+1}/{retries}): {e}")

            if attempt < retries - 1:
                await asyncio.sleep(self.config.get("network_delay", 1) * (attempt + 1))

        logger.error(f"Failed to fetch {url} after {retries} attempts.")
        return None

    async def close(self):
        await self.client.aclose()
