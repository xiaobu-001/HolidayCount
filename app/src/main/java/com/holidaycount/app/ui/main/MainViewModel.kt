package com.holidaycount.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaycount.app.data.model.EventItem
import com.holidaycount.app.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EventUiState {
    object Loading : EventUiState()
    data class Success(val events: List<EventItem>) : EventUiState()
    data class Error(val message: String) : EventUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Loading)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    /** 即将到来的事件（daysLeft >= 0，按天数升序）*/
    val upcomingEvents: StateFlow<List<EventItem>> = repository.getAllEventsFlow()
        .map { events -> events.filter { it.daysLeft >= 0 }.sortedBy { it.daysLeft } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 所有事件 */
    val allEvents: StateFlow<List<EventItem>> = repository.getAllEventsFlow()
        .map { it.sortedBy { e -> e.daysLeft.let { d -> if (d < 0) 100000 - d else d } } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 已过期事件（daysLeft < 0，按天数降序）*/
    val expiredEvents: StateFlow<List<EventItem>> = repository.getAllEventsFlow()
        .map { events -> events.filter { it.daysLeft < 0 }.sortedByDescending { it.daysLeft } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 下一个最近事件（用于首页展示）*/
    val nextEvent: StateFlow<EventItem?> = upcomingEvents
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteEventById(eventId)
        }
    }
}
