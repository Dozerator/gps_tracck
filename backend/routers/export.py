import csv
import io
import zipfile
from datetime import datetime, timedelta, timezone
from typing import Optional

import simplekml
from fastapi import APIRouter, Depends
from fastapi.responses import Response
from sqlalchemy import text
from sqlalchemy.orm import Session

from database import get_db
from models import LocationPoint

router = APIRouter(prefix="/api/export", tags=["export"])

THREAT_COLORS = {"THREAT": "#F44336", "ATTENTION": "#FF9800", "OBSERVATION": "#4CAF50"}
OBJECT_ICONS = {"UAV": "✈", "QUAD": "⬡"}
THREAT_PRIORITY = {"OBSERVATION": 1, "ATTENTION": 2, "THREAT": 3}


def _export_filename(extension: str) -> str:
    return f"operator_export_{datetime.now().strftime('%Y%m%d_%H%M')}.{extension}"


def _fetch_points(
    db: Session,
    hours: int,
    user_id: Optional[int] = None,
    threat_level: Optional[str] = None,
):
    since = datetime.now(timezone.utc) - timedelta(hours=hours)
    query = db.query(LocationPoint).filter(LocationPoint.sent_at > since)
    if user_id:
        query = query.filter(LocationPoint.user_id == user_id)
    if threat_level:
        query = query.filter(LocationPoint.threat_level == threat_level)
    return query.order_by(LocationPoint.timestamp.asc()).all()


def _fetch_tracks(db: Session, hours: int) -> dict:
    # Треки-линии рисуются по ПОЛНОМУ набору точек трека, без учёта user_id/threat_level
    # фильтров экспорта точек — иначе линия трека обрывалась бы там, где точка не
    # прошла фильтр, хотя физически объект продолжал двигаться.
    points = _fetch_points(db, hours)
    tracks: dict = {}
    for p in points:
        if p.track_id:
            tracks.setdefault(p.track_id, []).append(p)
    return tracks


# ─── GeoJSON ─────────────────────────────────────────────────────────

@router.get("/geojson")
def export_geojson(
    hours: int = 12,
    user_id: Optional[int] = None,
    threat_level: Optional[str] = None,
    db: Session = Depends(get_db),
):
    points = _fetch_points(db, hours, user_id, threat_level)
    tracks = _fetch_tracks(db, hours)
    features = []

    for p in points:
        features.append(
            {
                "type": "Feature",
                "geometry": {"type": "Point", "coordinates": [p.lon, p.lat]},
                "properties": {
                    "id": p.id,
                    "user_id": p.user_id,
                    "object_type": p.object_type,
                    "direction_degrees": p.direction_degrees,
                    "direction_label": p.direction_label,
                    "threat_level": p.threat_level,
                    "track_id": p.track_id,
                    "accuracy": p.accuracy,
                    "timestamp": p.timestamp.isoformat(),
                    "marker_color": THREAT_COLORS.get(p.threat_level, "#4CAF50"),
                },
            }
        )

    for track_id, track_points in tracks.items():
        if len(track_points) < 2:
            continue
        features.append(
            {
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": [[p.lon, p.lat] for p in track_points],
                },
                "properties": {
                    "track_id": track_id,
                    "object_type": track_points[0].object_type,
                    "user_id": track_points[0].user_id,
                    "point_count": len(track_points),
                    "start_time": track_points[0].timestamp.isoformat(),
                    "end_time": track_points[-1].timestamp.isoformat(),
                    "stroke": "#FF9800",
                    "stroke-width": 2,
                },
            }
        )

    collection = {"type": "FeatureCollection", "features": features}
    import json

    body = json.dumps(collection, ensure_ascii=False, indent=2)

    return Response(
        content=body,
        media_type="application/geo+json",
        headers={"Content-Disposition": f"attachment; filename={_export_filename('geojson')}"},
    )


# ─── KMZ ─────────────────────────────────────────────────────────────

def _build_kml_styles(kml: simplekml.Kml) -> dict:
    styles = {}
    configs = {
        "THREAT": {"color": simplekml.Color.red, "scale": 1.4},
        "ATTENTION": {"color": simplekml.Color.orange, "scale": 1.2},
        "OBSERVATION": {"color": simplekml.Color.green, "scale": 1.0},
    }
    for level, cfg in configs.items():
        style = simplekml.Style()
        style.iconstyle.color = cfg["color"]
        style.iconstyle.scale = cfg["scale"]
        style.iconstyle.icon.href = "http://maps.google.com/mapfiles/kml/paddle/wht-blank.png"
        style.labelstyle.color = cfg["color"]
        styles[level] = style
    return styles


@router.get("/kmz")
def export_kmz(
    hours: int = 12,
    user_id: Optional[int] = None,
    threat_level: Optional[str] = None,
    db: Session = Depends(get_db),
):
    points = _fetch_points(db, hours, user_id, threat_level)
    tracks = _fetch_tracks(db, hours)

    kml = simplekml.Kml(name="Operator Export")
    styles = _build_kml_styles(kml)

    folder_points = kml.newfolder(name="Точки")
    folder_tracks = kml.newfolder(name="Треки")
    folder_threat = kml.newfolder(name="⚠ Угрозы")

    for p in points:
        time_str = p.timestamp.strftime("%H:%M:%S %d.%m.%Y")
        name = f"{OBJECT_ICONS.get(p.object_type, '?')} {p.object_type} | {p.direction_label}"
        desc = (
            f"Пользователь: {p.user_id}\n"
            f"Угроза: {p.threat_level}\n"
            f"Направление: {p.direction_label}\n"
            f"Точность: {p.accuracy}м\n"
            f"Время: {time_str}\n"
            f"Трек: {p.track_id or 'нет'}"
        )
        folder = folder_threat if p.threat_level == "THREAT" else folder_points
        pnt = folder.newpoint(name=name, description=desc, coords=[(p.lon, p.lat)])
        pnt.style = styles.get(p.threat_level, styles["OBSERVATION"])
        pnt.timestamp.when = p.timestamp.isoformat()

    for track_id, track_points in tracks.items():
        if len(track_points) < 2:
            continue
        coords = [(p.lon, p.lat) for p in track_points]
        max_threat = max(track_points, key=lambda x: THREAT_PRIORITY.get(x.threat_level, 0)).threat_level

        line = folder_tracks.newlinestring(name=f"Трек {track_id[-8:]}", coords=coords)
        line.style.linestyle.color = {
            "THREAT": simplekml.Color.red,
            "ATTENTION": simplekml.Color.orange,
            "OBSERVATION": simplekml.Color.green,
        }.get(max_threat, simplekml.Color.green)
        line.style.linestyle.width = 3

    kmz_buf = io.BytesIO()
    with zipfile.ZipFile(kmz_buf, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("doc.kml", kml.kml())

    return Response(
        content=kmz_buf.getvalue(),
        media_type="application/vnd.google-earth.kmz",
        headers={"Content-Disposition": f"attachment; filename={_export_filename('kmz')}"},
    )


# ─── CSV ─────────────────────────────────────────────────────────────

@router.get("/csv")
def export_csv(
    hours: int = 12,
    user_id: Optional[int] = None,
    threat_level: Optional[str] = None,
    db: Session = Depends(get_db),
):
    points = _fetch_points(db, hours, user_id, threat_level)
    buf = io.StringIO()
    buf.write("﻿")  # UTF-8 BOM — чтобы Excel не путал кодировку

    writer = csv.writer(buf)
    writer.writerow(
        [
            "id", "timestamp", "user_id", "object_type",
            "lat", "lon", "accuracy",
            "direction_degrees", "direction_label",
            "threat_level", "track_id",
        ]
    )
    for p in points:
        writer.writerow(
            [
                p.id,
                p.timestamp.isoformat(),
                p.user_id,
                p.object_type,
                p.lat,
                p.lon,
                p.accuracy,
                p.direction_degrees,
                p.direction_label,
                p.threat_level,
                p.track_id or "",
            ]
        )

    return Response(
        content=buf.getvalue(),
        media_type="text/csv; charset=utf-8",
        headers={"Content-Disposition": f"attachment; filename={_export_filename('csv')}"},
    )


# ─── Статистика для панели экспорта ──────────────────────────────────

@router.get("/stats")
def export_stats(hours: int = 12, db: Session = Depends(get_db)):
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
                COUNT(DISTINCT user_id) AS active_users,
                COUNT(CASE WHEN object_type = 'UAV' THEN 1 END) AS uav_count,
                COUNT(CASE WHEN object_type = 'QUAD' THEN 1 END) AS quad_count
            FROM location_points
            WHERE sent_at > :since
            """
        ),
        {"since": since},
    ).fetchone()
    return dict(row._mapping)
