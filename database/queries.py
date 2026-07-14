from database.db_manager import db
from models.schema import Event, Team
from utils.logger import logger

class MatchQueries:
    @staticmethod
    def insert_match(match_id, league_id, home_team_id, away_team_id, match_date, status, home_score=None, away_score=None):
        session = db.get_session()
        try:
            match = session.query(Event).filter_by(id=match_id).first()
            if not match:
                match = Event(id=match_id)
            match.league_id = league_id
            match.home_team_id = home_team_id
            match.away_team_id = away_team_id
            match.match_date = match_date
            match.status = status
            match.home_score = home_score
            match.away_score = away_score

            session.add(match)
            session.commit()
            return match.id
        except Exception as e:
            session.rollback()
            logger.error(f"Error inserting match: {e}", exc_info=True)
            return None
        finally:
            session.close()

    @staticmethod
    def get_upcoming_matches(limit=50):
        session = db.get_session()
        try:
            matches = session.query(Event).filter(
                Event.status.notin_(['FT', 'FINISHED'])
            ).order_by(Event.match_date.asc()).limit(limit).all()

            # Simple conversion to dict for backward compatibility with UI
            return [{
                'id': m.id, 'league_id': m.league_id, 'home_team_id': m.home_team_id,
                'away_team_id': m.away_team_id, 'match_date': m.match_date,
                'status': m.status, 'home_score': m.home_score, 'away_score': m.away_score
            } for m in matches]
        except Exception as e:
            logger.error(f"Error fetching upcoming matches: {e}", exc_info=True)
            return []
        finally:
            session.close()

class TeamQueries:
    @staticmethod
    def insert_team(team_id, name, code, country, founded):
        session = db.get_session()
        try:
            team = session.query(Team).filter_by(id=team_id).first()
            if not team:
                team = Team(id=team_id)
            team.name = name
            team.code = code
            team.country = country
            team.founded = founded

            session.add(team)
            session.commit()
            return team.id
        except Exception as e:
            session.rollback()
            logger.error(f"Error inserting team: {e}", exc_info=True)
            return None
        finally:
            session.close()
