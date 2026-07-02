package com.example.operator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.operator.data.repository.LocationRepository
import com.example.operator.data.repository.SendResult
import com.example.operator.model.Direction
import com.example.operator.model.ObjectType
import com.example.operator.model.ThreatLevel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: LocationRepository) : ViewModel() {

    val pendingCount: StateFlow<Int> = repository.pendingCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isOnline: StateFlow<Boolean> = repository.isOnline

    private val _sendResult = MutableSharedFlow<SendResult>()
    val sendResult: SharedFlow<SendResult> = _sendResult

    // При появлении сети — автоматически запускаем синхронизацию накопленной очереди.
    init {
        viewModelScope.launch {
            repository.isOnline.collect { online ->
                if (online) {
                    repository.triggerSync()
                }
            }
        }
    }

    fun sendPoint(
        lat: Double,
        lon: Double,
        accuracy: Float,
        timestampMillis: Long,
        objectType: ObjectType,
        direction: Direction,
        threatLevel: ThreatLevel
    ) {
        viewModelScope.launch {
            val result = repository.sendPoint(lat, lon, accuracy, timestampMillis, objectType, direction, threatLevel)
            _sendResult.emit(result)
        }
    }

    class Factory(private val repository: LocationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
