package com.holidaycount.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.holidaycount.app.data.model.DateType
import com.holidaycount.app.data.model.EventType
import com.holidaycount.app.data.model.RepeatRule

/**
 * 自定义事件数据库实体
 */
@Entity(tableName = "custom_events")
data class CustomEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val emoji: String = "🎉",

    /** 公历目标日期时间戳（毫秒）*/
    val targetDateMs: Long,

    /** 原始输入月份（农历时使用）*/
    val originalMonth: Int = 0,

    /** 原始输入日（农历时使用）*/
    val originalDay: Int = 0,

    val dateType: DateType = DateType.SOLAR,

    val eventType: EventType = EventType.CUSTOM,

    val repeatRule: RepeatRule = RepeatRule.NONE,

    /** 背景颜色 ARGB */
    val backgroundColor: Int = 0xFF6200EE.toInt(),

    val isEnabled: Boolean = true,

    /** 提前提醒天数，逗号分隔，如 "1,3,7" */
    val notifyDaysStr: String = "",

    /** 提醒小时（0-23）*/
    val notifyHour: Int = 9,

    /** 提醒分钟（0-59）*/
    val notifyMinute: Int = 0,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getNotifyDaysList(): List<Int> {
        return if (notifyDaysStr.isBlank()) emptyList()
        else notifyDaysStr.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
