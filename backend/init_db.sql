-- Инициализация базы данных для Operator Tracking
-- Запуск: psql -U postgres -f init_db.sql

CREATE DATABASE operator_db;

\c operator_db

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    login VARCHAR(64) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS location_points (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    accuracy DOUBLE PRECISION,
    timestamp TIMESTAMPTZ NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    object_type VARCHAR(10) NOT NULL,             -- 'UAV' или 'QUAD'
    direction_degrees INTEGER NOT NULL DEFAULT 0, -- 0..359, 0 = север, по часовой стрелке
    direction_label VARCHAR(50) NOT NULL DEFAULT 'СЕВЕР (0°)',
    threat_level VARCHAR(15) NOT NULL DEFAULT 'OBSERVATION',  -- 'OBSERVATION', 'ATTENTION' или 'THREAT'
    track_id VARCHAR(100)  -- группировка точек одного объекта в трек, см. TrackManager
);

CREATE INDEX IF NOT EXISTS idx_location_points_user_id ON location_points(user_id);
CREATE INDEX IF NOT EXISTS idx_location_points_timestamp ON location_points(timestamp);
CREATE INDEX IF NOT EXISTS idx_location_points_track_id ON location_points(track_id);
CREATE INDEX IF NOT EXISTS idx_location_points_user_timestamp ON location_points(user_id, timestamp);

-- Вью для треков: одна строка на трек с агрегированной статистикой.
-- max_threat берётся через ARRAY_AGG(... ORDER BY приоритет DESC)[1], а не
-- MAX(threat_level) — лексикографически 'OBSERVATION' > 'ATTENTION', хотя по
-- серьёзности всё наоборот.
CREATE OR REPLACE VIEW track_summaries AS
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

-- Тестовый пользователь: login = admin, password = admin123
-- Хеш сгенерирован напрямую через bcrypt (см. backend/auth.py)
INSERT INTO users (login, password_hash)
VALUES ('admin', '$2b$12$83KdOb.mHvuYG0qKxETkk.lKy0b0V1MYJA3mnjAIyZft1gZ1cs/qC')
ON CONFLICT (login) DO NOTHING;
