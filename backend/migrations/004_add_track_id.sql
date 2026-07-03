-- Миграция для уже существующей БД: треки (несколько точек одного объекта,
-- сгруппированных по user_id+object_type+10-минутному окну — см. Android
-- utils/TrackManager.kt) и представление для их сводки.
-- Запуск: psql -U postgres -d operator_db -f migrations/004_add_track_id.sql

ALTER TABLE location_points
    ADD COLUMN IF NOT EXISTS track_id VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_location_points_track_id ON location_points(track_id);
CREATE INDEX IF NOT EXISTS idx_location_points_user_timestamp ON location_points(user_id, timestamp);

-- Вью для треков: одна строка на трек с агрегированной статистикой.
-- max_threat берётся НЕ через MAX(threat_level) — лексикографически
-- 'OBSERVATION' > 'ATTENTION' (по алфавиту), хотя по серьёзности всё наоборот.
-- ARRAY_AGG(... ORDER BY приоритет DESC)[1] выбирает метку самой серьёзной точки.
DROP VIEW IF EXISTS track_summaries;
CREATE VIEW track_summaries AS
SELECT
    track_id,
    user_id,
    object_type,
    (ARRAY_AGG(threat_level ORDER BY
        CASE threat_level
            WHEN 'THREAT' THEN 3
            WHEN 'ATTENTION' THEN 2
            WHEN 'OBSERVATION' THEN 1
            ELSE 0
        END DESC
    ))[1]              AS max_threat,
    COUNT(*)           AS point_count,
    MIN(timestamp)     AS start_time,
    MAX(timestamp)     AS end_time,
    MIN(lat)           AS min_lat,
    MAX(lat)           AS max_lat,
    MIN(lon)           AS min_lon,
    MAX(lon)           AS max_lon
FROM location_points
WHERE track_id IS NOT NULL
GROUP BY track_id, user_id, object_type;
