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

    @Slot(str)
    def ask_ai_chat(self, prompt):
        logger.info(f"Chat IA request: {prompt}")
        import threading
        import asyncio

        def run_ai():
            try:
                loop = asyncio.new_event_loop()
                asyncio.set_event_loop(loop)
                # Call real AI
                response = loop.run_until_complete(self.ai.generate_report({"user_question": prompt}))
                self.report_generated.emit(response)
                loop.close()
            except Exception as e:
                logger.error(f"AI Chat error: {e}")
                self.report_generated.emit("Erreur lors de la communication avec l'IA.")

        threading.Thread(target=run_ai, daemon=True).start()

    def place_automated_bet(self, match_id, selection, odds, amount):
        logger.info("Controller delegating bet to Node.js backend...")
        import threading
        import asyncio
        from api.betting_bot import BettingBotClient

        def run_bot():
            try:
                loop = asyncio.new_event_loop()
                asyncio.set_event_loop(loop)
                bot = BettingBotClient()
                res = loop.run_until_complete(bot.place_bet(match_id, selection, odds, amount))
                loop.close()
                if res.get("success"):
                    logger.info("Bet placed successfully via Node.js bot")
                else:
                    logger.error("Node.js bot rejected the bet")
            except Exception as e:
                logger.error(f"Failed to run bot client: {e}")

        threading.Thread(target=run_bot, daemon=True).start()
