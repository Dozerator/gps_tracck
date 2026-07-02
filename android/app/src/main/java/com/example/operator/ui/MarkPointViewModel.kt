package com.example.operator.ui

import android.location.Location
import androidx.lifecycle.ViewModel
import com.example.operator.model.Direction
import com.example.operator.model.ObjectType

/** Хранит состояние текущей отметки точки между шагами диалогов. */
class MarkPointViewModel : ViewModel() {
    var location: Location? = null
    var objectType: ObjectType? = null
    var direction: Direction? = null

    fun reset() {
        location = null
        objectType = null
        direction = null
    }
}
