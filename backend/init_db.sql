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
    object_type VARCHAR(10) NOT NULL,     -- 'UAV' или 'QUAD'
    direction VARCHAR(10) NOT NULL,       -- 'NORTH', 'SOUTH', 'EAST' или 'WEST'
    threat_level VARCHAR(15) NOT NULL DEFAULT 'OBSERVATION'  -- 'OBSERVATION', 'ATTENTION' или 'THREAT'
);

CREATE INDEX IF NOT EXISTS idx_location_points_user_id ON location_points(user_id);
CREATE INDEX IF NOT EXISTS idx_location_points_timestamp ON location_points(timestamp);

-- Тестовый пользователь: login = admin, password = admin123
-- Хеш сгенерирован напрямую через bcrypt (см. backend/auth.py)
INSERT INTO users (login, password_hash)
VALUES ('admin', '$2b$12$83KdOb.mHvuYG0qKxETkk.lKy0b0V1MYJA3mnjAIyZft1gZ1cs/qC')
ON CONFLICT (login) DO NOTHING;
