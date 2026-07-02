package com.example.operator.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val ISO_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

/** Форматирует момент времени (мс с эпохи) в ISO-8601 UTC-строку для backend. */
fun isoUtc(millis: Long): String = synchronized(ISO_FORMAT) { ISO_FORMAT.format(Date(millis)) }
