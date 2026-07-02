-- Миграция для уже существующей БД: добавляет тип объекта и направление движения.
-- Запуск: psql -U postgres -d operator_db -f migrations/001_add_object_type_direction.sql

ALTER TABLE location_points
    ADD COLUMN IF NOT EXISTS object_type VARCHAR(10),   -- 'UAV' или 'QUAD'
    ADD COLUMN IF NOT EXISTS direction VARCHAR(10);      -- 'NORTH','SOUTH','EAST','WEST'

-- Если в таблице уже есть исторические строки без этих полей — проставляем
-- значение по умолчанию, чтобы затем можно было включить NOT NULL.
UPDATE location_points SET object_type = 'UAV' WHERE object_type IS NULL;
UPDATE location_points SET direction = 'NORTH' WHERE direction IS NULL;

ALTER TABLE location_points
    ALTER COLUMN object_type SET NOT NULL,
    ALTER COLUMN direction SET NOT NULL;
