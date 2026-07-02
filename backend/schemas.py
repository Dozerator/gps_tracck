from datetime import datetime

from pydantic import BaseModel, Field


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


class LocationPointResponse(BaseModel):
    id: int
    user_id: int
    lat: float
    lon: float
    accuracy: float | None
    timestamp: datetime
    sent_at: datetime

    class Config:
        from_attributes = True
