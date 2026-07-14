import logging
import os
import sys
import json
from datetime import datetime

class BetproLogger:
    def __init__(self, log_file="logs/app.log", log_level=logging.DEBUG):
        # Create logs directory if it doesn't exist
        os.makedirs(os.path.dirname(log_file), exist_ok=True)

        self.logger = logging.getLogger("BetproAnalyst")
        self.logger.setLevel(log_level)

        # Prevent adding duplicate handlers if instantiated multiple times
        if not self.logger.handlers:
            # File handler
            file_handler = logging.FileHandler(log_file, encoding='utf-8')
            file_handler.setLevel(log_level)

            # Console handler
            console_handler = logging.StreamHandler(sys.stdout)
            console_handler.setLevel(log_level)

            # Formatter
            formatter = logging.Formatter(
                '%(asctime)s | %(levelname)s | [%(module)s] %(message)s',
                datefmt='%Y-%m-%d %H:%M:%S'
            )
            file_handler.setFormatter(formatter)
            console_handler.setFormatter(formatter)

            self.logger.addHandler(file_handler)
            self.logger.addHandler(console_handler)

    def get_logger(self):
        return self.logger

# Global logger instance
def setup_logger():
    try:
        with open("config.json", "r", encoding="utf-8") as f:
            config = json.load(f)
            log_level_str = config.get("app", {}).get("log_level", "DEBUG")
            log_file = config.get("app", {}).get("log_file", "logs/app.log")

            level = getattr(logging, log_level_str.upper(), logging.DEBUG)
            return BetproLogger(log_file=log_file, log_level=level).get_logger()
    except Exception as e:
        # Fallback if config is missing
        return BetproLogger().get_logger()

logger = setup_logger()
