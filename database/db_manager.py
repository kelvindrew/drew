import sqlite3
import os
import json
from contextlib import contextmanager
from utils.logger import logger

class DatabaseManager:
    def __init__(self, db_path="betpro_analyst.db"):
        self.db_path = db_path
        self._initialize_db()

    @contextmanager
    def get_connection(self):
        conn = None
        try:
            conn = sqlite3.connect(self.db_path)
            conn.row_factory = sqlite3.Row
            yield conn
        except sqlite3.Error as e:
            logger.error(f"Database connection error: {e}", exc_info=True)
            raise
        finally:
            if conn:
                conn.close()

    def _initialize_db(self):
        """Creates tables if they don't exist."""
        logger.info(f"Initializing database at {self.db_path}")
        try:
            with self.get_connection() as conn:
                cursor = conn.cursor()

                # Leagues
                cursor.execute('''
                    CREATE TABLE IF NOT EXISTS leagues (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        country TEXT,
                        type TEXT,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                ''')

                # Teams
                cursor.execute('''
                    CREATE TABLE IF NOT EXISTS teams (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        code TEXT,
                        country TEXT,
                        founded INTEGER,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                ''')

                # Players
                cursor.execute('''
                    CREATE TABLE IF NOT EXISTS players (
                        id INTEGER PRIMARY KEY,
                        team_id INTEGER,
                        name TEXT NOT NULL,
                        position TEXT,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (team_id) REFERENCES teams (id)
                    )
                ''')

                # Events / Matches
                cursor.execute('''
                    CREATE TABLE IF NOT EXISTS events (
                        id INTEGER PRIMARY KEY,
                        league_id INTEGER,
                        home_team_id INTEGER,
                        away_team_id INTEGER,
                        match_date TIMESTAMP NOT NULL,
                        status TEXT,
                        home_score INTEGER,
                        away_score INTEGER,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (league_id) REFERENCES leagues (id),
                        FOREIGN KEY (home_team_id) REFERENCES teams (id),
                        FOREIGN KEY (away_team_id) REFERENCES teams (id)
                    )
                ''')

                # Statistics
                cursor.execute('''
                    CREATE TABLE IF NOT EXISTS match_stats (
                        match_id INTEGER PRIMARY KEY,
                        stats_json TEXT, -- Store JSON string of stats (possession, shots, etc.)
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (match_id) REFERENCES events (id)
                    )
                ''')

                # AI Reports
                cursor.execute('''
                    CREATE TABLE IF NOT EXISTS ai_reports (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        match_id INTEGER,
                        report_content TEXT,
                        confidence_score REAL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (match_id) REFERENCES events (id)
                    )
                ''')

                conn.commit()
                logger.info("Database tables initialized successfully.")
        except Exception as e:
            logger.error(f"Failed to initialize database: {e}", exc_info=True)

# Factory pattern to get db instance based on config
def get_db_manager():
    try:
        with open("config.json", "r", encoding="utf-8") as f:
            config = json.load(f)
            db_path = config.get("database", {}).get("connection_string", "betpro_analyst.db")
            return DatabaseManager(db_path)
    except Exception as e:
        logger.error("Could not load db path from config, using default.", exc_info=True)
        return DatabaseManager()

# Default instance
db = get_db_manager()
