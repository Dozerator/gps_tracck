-- Миграция для уже существующей БД: направление становится числовым углом
-- (0-359°, компас на клиенте) вместо 4 фиксированных сторон света.
-- Запуск: psql -U postgres -d operator_db -f migrations/003_replace_direction_with_degrees.sql

ALTER TABLE location_points
    ADD COLUMN IF NOT EXISTS direction_degrees INTEGER,
    ADD COLUMN IF NOT EXISTS direction_label VARCHAR(50);

-- Бэкфилл существующих строк по старому строковому полю "direction".
UPDATE location_points SET
    direction_degrees = CASE direction
        WHEN 'NORTH' THEN 0
        WHEN 'EAST' THEN 90
        WHEN 'SOUTH' THEN 180
        WHEN 'WEST' THEN 270
        ELSE 0
    END,
    direction_label = CASE direction
        WHEN 'NORTH' THEN 'СЕВЕР (0°)'
        WHEN 'EAST' THEN 'ВОСТОК (90°)'
        WHEN 'SOUTH' THEN 'ЮГ (180°)'
        WHEN 'WEST' THEN 'ЗАПАД (270°)'
        ELSE 'СЕВЕР (0°)'
    END
WHERE direction_degrees IS NULL;

ALTER TABLE location_points
    ALTER COLUMN direction_degrees SET NOT NULL,
    ALTER COLUMN direction_degrees SET DEFAULT 0,
    ALTER COLUMN direction_label SET NOT NULL,
    ALTER COLUMN direction_label SET DEFAULT 'СЕВЕР (0°)';

ALTER TABLE location_points DROP COLUMN IF EXISTS direction;
