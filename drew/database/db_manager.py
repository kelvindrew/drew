import sqlite3
import os
import json
from datetime import datetime

class DatabaseManager:
    def __init__(self, db_path="drew_data.sqlite"):
        self.db_path = db_path
        self._init_db()

    def get_connection(self):
        return sqlite3.connect(self.db_path)

    def _init_db(self):
        conn = self.get_connection()
        cursor = conn.cursor()

        # Events table
        cursor.execute('''
        CREATE TABLE IF NOT EXISTS events (
            id TEXT PRIMARY KEY,
            sport_key TEXT,
            sport_title TEXT,
            commence_time DATETIME,
            home_team TEXT,
            away_team TEXT,
            competition TEXT,
            status TEXT,
            last_updated DATETIME
        )
        ''')

        # Odds/Selections table
        cursor.execute('''
        CREATE TABLE IF NOT EXISTS odds (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            event_id TEXT,
            market_key TEXT,
            selection_name TEXT,
            price REAL,
            confidence_score REAL,
            risk_flags TEXT,
            last_updated DATETIME,
            FOREIGN KEY(event_id) REFERENCES events(id)
        )
        ''')

        # Combinations History
        cursor.execute('''
        CREATE TABLE IF NOT EXISTS combinations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            target_min REAL,
            target_max REAL,
            total_odds REAL,
            avg_confidence REAL,
            generated_at DATETIME,
            status TEXT
        )
        ''')

        # Combination items mapping
        cursor.execute('''
        CREATE TABLE IF NOT EXISTS combination_items (
            combo_id INTEGER,
            odd_id INTEGER,
            FOREIGN KEY(combo_id) REFERENCES combinations(id),
            FOREIGN KEY(odd_id) REFERENCES odds(id)
        )
        ''')

        conn.commit()
        conn.close()

    def save_event(self, event_data):
        conn = self.get_connection()
        cursor = conn.cursor()
        cursor.execute('''
            INSERT OR REPLACE INTO events (id, sport_key, sport_title, commence_time, home_team, away_team, competition, status, last_updated)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            event_data['id'], event_data['sport_key'], event_data['sport_title'],
            event_data['commence_time'], event_data['home_team'], event_data['away_team'],
            event_data['competition'], event_data['status'], datetime.now().isoformat()
        ))
        conn.commit()
        conn.close()

    def save_odd(self, odd_data):
        conn = self.get_connection()
        cursor = conn.cursor()

        # Simple check if exists to update or insert
        cursor.execute('''
            SELECT id FROM odds WHERE event_id = ? AND market_key = ? AND selection_name = ?
        ''', (odd_data['event_id'], odd_data['market_key'], odd_data['selection_name']))
        row = cursor.fetchone()

        if row:
            cursor.execute('''
                UPDATE odds SET price = ?, confidence_score = ?, risk_flags = ?, last_updated = ?
                WHERE id = ?
            ''', (odd_data['price'], odd_data['confidence_score'], json.dumps(odd_data.get('risk_flags', [])), datetime.now().isoformat(), row[0]))
        else:
            cursor.execute('''
                INSERT INTO odds (event_id, market_key, selection_name, price, confidence_score, risk_flags, last_updated)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            ''', (odd_data['event_id'], odd_data['market_key'], odd_data['selection_name'],
                  odd_data['price'], odd_data['confidence_score'], json.dumps(odd_data.get('risk_flags', [])), datetime.now().isoformat()))
        conn.commit()
        conn.close()

    def get_active_odds(self):
        # Fetch odds for events that haven't started yet
        conn = self.get_connection()
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        cursor.execute('''
            SELECT o.id as odd_id, o.event_id, o.market_key, o.selection_name, o.price, o.confidence_score, o.risk_flags,
                   e.home_team, e.away_team, e.competition, e.commence_time
            FROM odds o
            JOIN events e ON o.event_id = e.id
            WHERE e.status = 'upcoming' AND e.commence_time > ?
        ''', (datetime.now().isoformat(),))

        rows = cursor.fetchall()
        conn.close()
        return [dict(row) for row in rows]

    def save_combination(self, target_min, target_max, total_odds, avg_confidence, odd_ids):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute('''
            INSERT INTO combinations (target_min, target_max, total_odds, avg_confidence, generated_at, status)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (target_min, target_max, total_odds, avg_confidence, datetime.now().isoformat(), 'pending'))

        combo_id = cursor.lastrowid

        for odd_id in odd_ids:
            cursor.execute('INSERT INTO combination_items (combo_id, odd_id) VALUES (?, ?)', (combo_id, odd_id))

        conn.commit()
        conn.close()
        return combo_id
