package com.example.operator.ui

import android.location.Location
import androidx.lifecycle.ViewModel
import com.example.operator.model.ObjectType
import com.example.operator.model.ThreatLevel

/** Хранит состояние текущей отметки точки между шагами диалогов. */
class MarkPointViewModel : ViewModel() {
    var location: Location? = null
    var objectType: ObjectType? = null
    var directionDegrees: Int? = null
    var threatLevel: ThreatLevel? = null

    fun reset() {
        location = null
        objectType = null
        directionDegrees = null
        threatLevel = null
    }
}
