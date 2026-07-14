from PySide6.QtCore import QObject, Signal, Slot, QTimer
import json
from analysis.engine import StatsEngine
from database.queries import MatchQueries, TeamQueries
from api.api_football import APIFootballClient
from api.the_odds import TheOddsClient
from ai.assistant import LocalAIAssistant
from utils.logger import logger

class AppController(QObject):
    data_updated = Signal(dict)
    report_generated = Signal(str)
    error_occurred = Signal(str)

    def __init__(self):
        super().__init__()
        self.stats_engine = StatsEngine()
        self.ai = LocalAIAssistant()
        self.football_api = APIFootballClient()
        self.odds_api = TheOddsClient()

        # Scheduler
        try:
            with open("config.json", "r") as f:
                self.config = json.load(f)
            freq = self.config.get("api", {}).get("update_frequency_seconds", 3600)
        except:
            freq = 3600

        self.timer = QTimer(self)
        self.timer.timeout.connect(self.refresh_data)
        self.timer.start(freq * 1000) # milliseconds
        logger.info(f"Background scheduler started (interval: {freq}s)")

    def refresh_data(self):
        logger.info("ETL Pipeline: Starting data refresh from APIs...")
        import asyncio
        asyncio.run(self._async_refresh())

    async def _async_refresh(self):
        try:
            import datetime
            today = datetime.datetime.now().strftime("%Y-%m-%d")
            fixtures_res = await self.football_api.get_fixtures(today)

            fixtures = fixtures_res.get('response', []) if fixtures_res else []
            logger.info(f"ETL Pipeline: Fetched {len(fixtures)} fixtures")

            odds_res = await self.odds_api.get_odds()

            self.data_updated.emit({"status": "success", "message": f"{len(fixtures)} matchs mis à jour."})
            logger.info("ETL Pipeline: Data refresh completed successfully.")

        except Exception as e:
            logger.error(f"ETL Pipeline failed: {e}", exc_info=True)
            self.error_occurred.emit(f"Erreur de rafraîchissement: {str(e)}")

    @Slot(int)
    def generate_ai_report(self, match_id):
        logger.info(f"Generating report for match {match_id}")
        self.report_generated.emit("Rapport IA: Paris SG semble avoir l'avantage.")
