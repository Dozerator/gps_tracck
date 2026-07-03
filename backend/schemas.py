from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field

ObjectType = Literal["UAV", "QUAD"]
ThreatLevel = Literal["OBSERVATION", "ATTENTION", "THREAT"]


class LoginRequest(BaseModel):
    login: str
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    expires_in: int


class LocationPointCreate(BaseModel):
    lat: float = Field(..., ge=-90, le=90)
    lon: float = Field(..., ge=-180, le=180)
    accuracy: float | None = None
    timestamp: datetime
    object_type: ObjectType
    direction_degrees: int = Field(..., ge=0, le=359)
    direction_label: str = Field(..., max_length=50)
    threat_level: ThreatLevel = "OBSERVATION"
    # Клиент сам решает, продолжается ли трек (тот же пользователь+тип объекта
    # в пределах 10-минутного окна) — см. Android utils/TrackManager.kt.
    track_id: str = Field(..., max_length=100)


class LocationPointResponse(BaseModel):
    id: int
    user_id: int
    lat: float
    lon: float
    accuracy: float | None
    timestamp: datetime
    sent_at: datetime
    object_type: ObjectType
    direction_degrees: int
    direction_label: str
    threat_level: ThreatLevel
    track_id: str | None

    class Config:
        from_attributes = True
