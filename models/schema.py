from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Text
from sqlalchemy.orm import declarative_base, relationship
import datetime

Base = declarative_base()

class League(Base):
    __tablename__ = 'leagues'

    id = Column(Integer, primary_key=True)
    name = Column(String, nullable=False)
    country = Column(String)
    type = Column(String)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow)

    events = relationship("Event", back_populates="league")

class Team(Base):
    __tablename__ = 'teams'

    id = Column(Integer, primary_key=True)
    name = Column(String, nullable=False)
    code = Column(String)
    country = Column(String)
    founded = Column(Integer)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow)

    players = relationship("Player", back_populates="team")
    # Events relationships are handled in Event class

class Player(Base):
    __tablename__ = 'players'

    id = Column(Integer, primary_key=True)
    team_id = Column(Integer, ForeignKey('teams.id'))
    name = Column(String, nullable=False)
    position = Column(String)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow)

    team = relationship("Team", back_populates="players")

class Event(Base):
    __tablename__ = 'events'

    id = Column(Integer, primary_key=True)
    league_id = Column(Integer, ForeignKey('leagues.id'))
    home_team_id = Column(Integer, ForeignKey('teams.id'))
    away_team_id = Column(Integer, ForeignKey('teams.id'))
    match_date = Column(DateTime, nullable=False)
    status = Column(String)
    home_score = Column(Integer)
    away_score = Column(Integer)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow)

    league = relationship("League", back_populates="events")
    home_team = relationship("Team", foreign_keys=[home_team_id])
    away_team = relationship("Team", foreign_keys=[away_team_id])

    stats = relationship("MatchStat", back_populates="event", uselist=False)
    ai_report = relationship("AIReport", back_populates="event", uselist=False)

class MatchStat(Base):
    __tablename__ = 'match_stats'

    match_id = Column(Integer, ForeignKey('events.id'), primary_key=True)
    stats_json = Column(Text) # JSON string
    updated_at = Column(DateTime, default=datetime.datetime.utcnow)

    event = relationship("Event", back_populates="stats")

class AIReport(Base):
    __tablename__ = 'ai_reports'

    id = Column(Integer, primary_key=True, autoincrement=True)
    match_id = Column(Integer, ForeignKey('events.id'))
    report_content = Column(Text)
    confidence_score = Column(Float)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

    event = relationship("Event", back_populates="ai_report")
