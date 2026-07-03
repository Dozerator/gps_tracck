package com.example.operator.utils

/** Метка стороны света (8-румбовая роза ветров) с точным значением в градусах, например "СЕВЕРО-ВОСТОК (47°)". */
fun directionLabel(degrees: Int): String {
    val normalized = ((degrees % 360) + 360) % 360
    val base = when (normalized) {
        in 338..359, in 0..22 -> "СЕВЕР"
        in 23..67 -> "СЕВЕРО-ВОСТОК"
        in 68..112 -> "ВОСТОК"
        in 113..157 -> "ЮГО-ВОСТОК"
        in 158..202 -> "ЮГ"
        in 203..247 -> "ЮГО-ЗАПАД"
        in 248..292 -> "ЗАПАД"
        else -> "СЕВЕРО-ЗАПАД" // 293..337
    }
    return "$base (${normalized}°)"
}
