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

/**
 * Уровень угрозы. Хранится локально в очереди точек (см. [com.example.operator.data.local.entity.PendingPointEntity]).
 * В текущем UI-флоу шага выбора уровня угрозы нет, поэтому используется значение по умолчанию
 * [OBSERVATION]; backend пока не принимает это поле, и оно не уходит в сетевой запрос.
 */
enum class ThreatLevel(val apiValue: String, val label: String) {
    OBSERVATION("OBSERVATION", "НАБЛЮДЕНИЕ"),
    ATTENTION("ATTENTION", "ВНИМАНИЕ"),
    THREAT("THREAT", "УГРОЗА")
}
