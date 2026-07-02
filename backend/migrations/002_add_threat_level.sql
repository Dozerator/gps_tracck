-- Миграция для уже существующей БД: добавляет уровень угрозы.
-- Запуск: psql -U postgres -d operator_db -f migrations/002_add_threat_level.sql

ALTER TABLE location_points
    ADD COLUMN IF NOT EXISTS threat_level VARCHAR(15) DEFAULT 'OBSERVATION';

UPDATE location_points SET threat_level = 'OBSERVATION' WHERE threat_level IS NULL;

ALTER TABLE location_points
    ALTER COLUMN threat_level SET NOT NULL;
