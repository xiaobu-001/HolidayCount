package com.holidaycount.app.data.model

/**
 * 事件类型枚举
 */
enum class EventType {
    BUILT_IN_HOLIDAY,   // 内置节假日
    CUSTOM,             // 用户自定义
    BIRTHDAY,           // 生日
    ANNIVERSARY         // 纪念日
}

/**
 * 重复规则枚举
 */
enum class RepeatRule {
    NONE,       // 不重复
    YEARLY,     // 每年
    MONTHLY     // 每月
}

/**
 * 日期类型
 */
enum class DateType {
    SOLAR,  // 公历
    LUNAR   // 农历
}

/**
 * 统一事件数据模型（用于 UI 展示）
 */
data class EventItem(
    val id: Long = 0,
    val name: String,
    val emoji: String = "🎉",
    val targetDate: Long,           // 目标日期时间戳（毫秒）
    val daysLeft: Int,              // 剩余天数（负数表示已过去）
    val eventType: EventType,
    val repeatRule: RepeatRule,
    val backgroundColor: Int,       // 背景颜色
    val isEnabled: Boolean = true,
    val notifyDays: List<Int> = emptyList()  // 提前几天通知
) {
    val isUpcoming: Boolean get() = daysLeft >= 0
    val isToday: Boolean get() = daysLeft == 0
    val isExpired: Boolean get() = daysLeft < 0
}
