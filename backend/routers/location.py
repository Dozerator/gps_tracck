from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import LocationPoint, User
from schemas import LocationPointCreate, LocationPointResponse
from websocket_manager import manager

router = APIRouter(prefix="/api/location", tags=["location"])


@router.post("/point", response_model=LocationPointResponse)
async def create_point(
    payload: LocationPointCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    point = LocationPoint(
        user_id=current_user.id,
        lat=payload.lat,
        lon=payload.lon,
        accuracy=payload.accuracy,
        timestamp=payload.timestamp,
        object_type=payload.object_type,
        direction=payload.direction,
    )
    db.add(point)
    db.commit()
    db.refresh(point)

    await manager.broadcast(
        {
            "type": "new_point",
            "user": current_user.login,
            "lat": point.lat,
            "lon": point.lon,
            "accuracy": point.accuracy,
            "object_type": point.object_type,
            "direction": point.direction,
            "timestamp": point.timestamp.isoformat(),
        }
    )

    return point
