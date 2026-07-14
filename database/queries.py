import json
from database.db_manager import db
from utils.logger import logger

class MatchQueries:
    @staticmethod
    def insert_match(match_id, league_id, home_team_id, away_team_id, match_date, status, home_score=None, away_score=None):
        try:
            with db.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute('''
                    INSERT OR REPLACE INTO events (id, league_id, home_team_id, away_team_id, match_date, status, home_score, away_score)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ''', (match_id, league_id, home_team_id, away_team_id, match_date, status, home_score, away_score))
                conn.commit()
                return cursor.lastrowid
        except Exception as e:
            logger.error(f"Error inserting match: {e}", exc_info=True)
            return None

    @staticmethod
    def get_upcoming_matches(limit=50):
        try:
            with db.get_connection() as conn:
                cursor = conn.cursor()
                # Dummy query for upcoming matches
                cursor.execute('''
                    SELECT * FROM events
                    WHERE status NOT IN ('FT', 'FINISHED')
                    ORDER BY match_date ASC
                    LIMIT ?
                ''', (limit,))
                return [dict(row) for row in cursor.fetchall()]
        except Exception as e:
            logger.error(f"Error fetching upcoming matches: {e}", exc_info=True)
            return []

class TeamQueries:
    @staticmethod
    def insert_team(team_id, name, code, country, founded):
        try:
            with db.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute('''
                    INSERT OR REPLACE INTO teams (id, name, code, country, founded)
                    VALUES (?, ?, ?, ?, ?)
                ''', (team_id, name, code, country, founded))
                conn.commit()
                return cursor.lastrowid
        except Exception as e:
            logger.error(f"Error inserting team: {e}", exc_info=True)
            return None
