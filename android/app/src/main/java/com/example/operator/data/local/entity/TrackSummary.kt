package com.example.operator.data.local.entity

/** Агрегированная сводка по одному треку — результат GROUP BY в PendingPointDao.getAllTracks(). */
data class TrackSummary(
    val trackId: String,
    val objectType: String,
    val threatLevel: String,
    val pointCount: Int,
    val startTime: Long,
    val endTime: Long
)
