from datetime import datetime, timedelta, timezone

from fastapi import APIRouter, Depends
from sqlalchemy import text
from sqlalchemy.orm import Session

from database import get_db

router = APIRouter(prefix="/api/tracks", tags=["tracks"])

# Панель оператора (как и /admin, /ws/operator) не требует JWT — доступ
# ограничивается на уровне сети (см. README, раздел про TLS/закрытую сеть),
# а не токеном телефона оператора-репортёра.


def _rows_to_dicts(rows) -> list[dict]:
    return [dict(row._mapping) for row in rows]


@router.get("/")
def get_tracks(hours: int = 12, db: Session = Depends(get_db)):
    since = datetime.now(timezone.utc) - timedelta(hours=hours)
    rows = db.execute(
        text(
            """
            SELECT * FROM track_summaries
            WHERE start_time > :since
            ORDER BY start_time DESC
            """
        ),
        {"since": since},
    ).fetchall()
    return _rows_to_dicts(rows)


@router.get("/history/shift")
def get_shift_history(hours: int = 12, db: Session = Depends(get_db)):
    since = datetime.now(timezone.utc) - timedelta(hours=hours)
    rows = db.execute(
        text(
            """
            SELECT * FROM location_points
            WHERE sent_at > :since
            ORDER BY timestamp DESC
            """
        ),
        {"since": since},
    ).fetchall()
    return _rows_to_dicts(rows)


@router.get("/stats/shift")
def get_shift_stats(hours: int = 12, db: Session = Depends(get_db)):
    since = datetime.now(timezone.utc) - timedelta(hours=hours)
    row = db.execute(
        text(
            """
            SELECT
                COUNT(*) AS total,
                COUNT(CASE WHEN threat_level = 'THREAT' THEN 1 END) AS threats,
                COUNT(CASE WHEN threat_level = 'ATTENTION' THEN 1 END) AS attention,
                COUNT(CASE WHEN threat_level = 'OBSERVATION' THEN 1 END) AS observation,
                COUNT(DISTINCT track_id) AS total_tracks,
                COUNT(DISTINCT user_id) AS active_users
            FROM location_points
            WHERE sent_at > :since
            """
        ),
        {"since": since},
    ).fetchone()
    return dict(row._mapping)


# Регистрируем ПОСЛЕ /history/shift и /stats/shift: иначе строки "history"/"stats"
# были бы синтаксически валидным (хоть и бессмысленным) значением {track_id} для
# /{track_id}/points, если бы FastAPI сопоставлял пути в другом порядке.
@router.get("/{track_id}/points")
def get_track_points(track_id: str, db: Session = Depends(get_db)):
    rows = db.execute(
        text(
            """
            SELECT * FROM location_points
            WHERE track_id = :track_id
            ORDER BY timestamp ASC
            """
        ),
        {"track_id": track_id},
    ).fetchall()
    return _rows_to_dicts(rows)
