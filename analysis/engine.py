import pandas as pd
import numpy as np
from utils.logger import logger

class StatsEngine:
    def __init__(self):
        pass

    def calculate_form(self, recent_results):
        """
        recent_results: list of dicts [{'result': 'W'}, {'result': 'L'}, {'result': 'D'}]
        Returns a form score out of 100 based on the last 5 matches.
        """
        if not recent_results:
            return 50.0  # Unknown, default to average

        points = 0
        max_points = len(recent_results) * 3

        for res in recent_results:
            if res.get('result') == 'W':
                points += 3
            elif res.get('result') == 'D':
                points += 1

        return (points / max_points) * 100

    def analyze_team_metrics(self, df_matches, team_id):
        """
        Expects a pandas DataFrame of historical matches.
        Returns calculated metrics like xG average, goals scored/conceded, streaks.
        """
        try:
            if df_matches.empty:
                return {}

            # Filter matches for the specific team
            team_matches = df_matches[(df_matches['home_team_id'] == team_id) | (df_matches['away_team_id'] == team_id)]

            if team_matches.empty:
                return {}

            metrics = {}
            metrics['total_matches'] = len(team_matches)

            # Goals calculation (simplified)
            home_games = team_matches[team_matches['home_team_id'] == team_id]
            away_games = team_matches[team_matches['away_team_id'] == team_id]

            goals_scored = home_games['home_score'].sum() + away_games['away_score'].sum()
            goals_conceded = home_games['away_score'].sum() + away_games['home_score'].sum()

            metrics['avg_goals_scored'] = round(goals_scored / metrics['total_matches'], 2)
            metrics['avg_goals_conceded'] = round(goals_conceded / metrics['total_matches'], 2)

            # Add xG simulation / reading logic here if columns exist
            if 'home_xg' in df_matches.columns and 'away_xg' in df_matches.columns:
                xg_for = home_games['home_xg'].sum() + away_games['away_xg'].sum()
                metrics['avg_xg_for'] = round(xg_for / metrics['total_matches'], 2)

            return metrics

        except Exception as e:
            logger.error(f"Error analyzing team metrics: {e}", exc_info=True)
            return {}

    def generate_power_rating(self, metrics):
        """
        Generates a custom power rating (0-100) based on various stats.
        """
        try:
            score = 50.0

            # Reward high scoring and low conceding
            if 'avg_goals_scored' in metrics:
                score += (metrics['avg_goals_scored'] * 10)
            if 'avg_goals_conceded' in metrics:
                score -= (metrics['avg_goals_conceded'] * 8)

            # Normalize to 0-100 scale
            score = max(0, min(100, score))
            return round(score, 1)
        except Exception as e:
            logger.error(f"Error calculating power rating: {e}")
            return 50.0
