package com.holidaycount.app.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaycount.app.data.local.entity.CustomEventEntity
import com.holidaycount.app.data.model.DateType
import com.holidaycount.app.data.model.EventType
import com.holidaycount.app.data.model.RepeatRule
import com.holidaycount.app.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddEventState {
    object Idle : AddEventState()
    object Saving : AddEventState()
    data class Success(val id: Long) : AddEventState()
    data class Error(val message: String) : AddEventState()
}

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AddEventState>(AddEventState.Idle)
    val state = _state.asStateFlow()

    /** 保存自定义事件 */
    fun saveEvent(
        name: String,
        emoji: String,
        targetDateMs: Long,
        eventType: EventType,
        repeatRule: RepeatRule,
        backgroundColor: Int,
        notifyDays: List<Int>,
        notifyHour: Int,
        notifyMinute: Int,
        editId: Long = 0
    ) {
        if (name.isBlank()) {
            _state.value = AddEventState.Error("请输入事件名称")
            return
        }

        viewModelScope.launch {
            _state.value = AddEventState.Saving
            try {
                val entity = CustomEventEntity(
                    id = if (editId > 0) editId else 0,
                    name = name.trim(),
                    emoji = emoji,
                    targetDateMs = targetDateMs,
                    eventType = eventType,
                    repeatRule = repeatRule,
                    backgroundColor = backgroundColor,
                    notifyDaysStr = notifyDays.joinToString(","),
                    notifyHour = notifyHour,
                    notifyMinute = notifyMinute,
                    updatedAt = System.currentTimeMillis()
                )
                val id = repository.insertEvent(entity)
                _state.value = AddEventState.Success(id)
            } catch (e: Exception) {
                _state.value = AddEventState.Error(e.message ?: "保存失败")
            }
        }
    }

    suspend fun getEvent(id: Long): CustomEventEntity? = repository.getEventById(id)

    fun resetState() { _state.value = AddEventState.Idle }
}
