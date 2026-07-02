package com.example.operator.model

/** Тип отмечаемого объекта, значение apiValue уходит в JSON как "object_type". */
enum class ObjectType(val apiValue: String, val label: String) {
    UAV("UAV", "БЕСПИЛОТНИК"),
    QUAD("QUAD", "КВАДРИК")
}

/** Направление движения объекта, значение apiValue уходит в JSON как "direction". */
enum class Direction(val apiValue: String, val label: String, val arrow: String) {
    NORTH("NORTH", "СЕВЕР", "↑"),
    SOUTH("SOUTH", "ЮГ", "↓"),
    EAST("EAST", "ВОСТОК", "→"),
    WEST("WEST", "ЗАПАД", "←")
}

/** Уровень угрозы, значение apiValue уходит в JSON как "threat_level". Цвета — NATO threat color coding. */
enum class ThreatLevel(
    val apiValue: String,
    val label: String,
    val subtitle: String,
    val icon: String,
    val colorHex: String
) {
    OBSERVATION("OBSERVATION", "НАБЛЮДЕНИЕ", "Летит мимо, не опасен", "🟢", "#1B5E20"),
    ATTENTION("ATTENTION", "ВНИМАНИЕ", "Приближается к позиции", "🟡", "#E65100"),
    THREAT("THREAT", "УГРОЗА", "Прямой курс на позицию", "🔴", "#B71C1C")
}
