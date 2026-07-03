from datetime import datetime, timezone

from sqlalchemy import Column, DateTime, Float, ForeignKey, Integer, String
from sqlalchemy.orm import relationship

from database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    login = Column(String(64), unique=True, nullable=False, index=True)
    password_hash = Column(String(255), nullable=False)
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))

    points = relationship("LocationPoint", back_populates="user")


class LocationPoint(Base):
    __tablename__ = "location_points"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    lat = Column(Float, nullable=False)
    lon = Column(Float, nullable=False)
    accuracy = Column(Float, nullable=True)
    timestamp = Column(DateTime(timezone=True), nullable=False)
    sent_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    object_type = Column(String(10), nullable=False)
    direction_degrees = Column(Integer, nullable=False, default=0)
    direction_label = Column(String(50), nullable=False, default="СЕВЕР (0°)")
    threat_level = Column(String(15), nullable=False, default="OBSERVATION")
    track_id = Column(String(100), nullable=True, index=True)

    user = relationship("User", back_populates="points")
