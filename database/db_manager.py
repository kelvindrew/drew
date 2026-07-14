import json
import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, scoped_session
from models.schema import Base
from utils.logger import logger

class DatabaseManager:
    def __init__(self, db_path="betpro_analyst.db"):
        self.db_path = db_path
        self.engine = create_engine(f"sqlite:///{self.db_path}", echo=False)
        self.SessionFactory = scoped_session(sessionmaker(bind=self.engine))
        self._initialize_db()

    def get_session(self):
        return self.SessionFactory()

    def _initialize_db(self):
        logger.info(f"Initializing SQLAlchemy DB at {self.db_path}")
        try:
            Base.metadata.create_all(self.engine)
            logger.info("Database schemas created/verified successfully via SQLAlchemy.")
        except Exception as e:
            logger.error(f"Failed to initialize SQLAlchemy DB: {e}", exc_info=True)

def get_db_manager():
    try:
        with open("config.json", "r", encoding="utf-8") as f:
            config = json.load(f)
            db_path = config.get("database", {}).get("connection_string", "betpro_analyst.db")
            return DatabaseManager(db_path)
    except Exception as e:
        logger.error("Could not load db path from config, using default.", exc_info=True)
        return DatabaseManager()

db = get_db_manager()
