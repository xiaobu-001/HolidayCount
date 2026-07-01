package com.holidaycount.app.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日期计算工具类
 */
object DateCalculation {

    private val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE)
    private val sdfEn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val weekdayFormat = SimpleDateFormat("EEEE", Locale.CHINESE)

    /**
     * 计算两个日期之间的天数差（目标 - 今天）
     * 正数 = 未来，负数 = 已过，0 = 今天
     */
    fun daysUntil(targetDateMs: Long): Int {
        val today = todayStartMs()
        val targetDay = dayStartMs(targetDateMs)
        val diff = targetDay - today
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    /**
     * 获取今天 00:00:00 的时间戳
     */
    fun todayStartMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * 获取指定时间戳当天 00:00:00 的时间戳
     */
    fun dayStartMs(timeMs: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMs
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * 格式化日期（中文）
     */
    fun formatDate(timeMs: Long): String = sdf.format(Date(timeMs))

    /**
     * 格式化日期（英文，用于存储）
     */
    fun formatDateEn(timeMs: Long): String = sdfEn.format(Date(timeMs))

    /**
     * 获取星期几
     */
    fun getWeekday(timeMs: Long): String = weekdayFormat.format(Date(timeMs))

    /**
     * 获取今年的目标月日对应时间戳
     * 如果今年已过，则返回明年的日期
     */
    fun getNextOccurrence(month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)

        cal.set(currentYear, month - 1, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (cal.timeInMillis < todayStartMs()) {
            cal.set(currentYear + 1, month - 1, day, 0, 0, 0)
        }
        return cal.timeInMillis
    }

    /**
     * 获取农历节日下一次对应的公历日期
     * @param lunarMonth 农历月
     * @param lunarDay 农历日
     */
    fun getNextLunarOccurrence(lunarMonth: Int, lunarDay: Int): Long {
        val today = todayStartMs()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // 尝试今年
        for (yearOffset in 0..1) {
            val lunarYear = currentYear + yearOffset - 1 // 农历年比公历年可能差1
            for (ly in lunarYear..lunarYear + 1) {
                try {
                    val solar = LunarCalendar.lunarToSolar(ly, lunarMonth, lunarDay)
                    if (solar.timeInMillis >= today) {
                        return solar.timeInMillis
                    }
                } catch (e: Exception) {
                    // 忽略无效日期
                }
            }
        }
        // 退化：返回明年今日
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, 1)
        return cal.timeInMillis
    }

    /**
     * 根据重复规则计算下一次日期
     */
    fun getNextOccurrenceByRule(
        originalMs: Long,
        repeatRule: com.holidaycount.app.data.model.RepeatRule
    ): Long {
        val today = todayStartMs()
        if (originalMs >= today) return originalMs

        val cal = Calendar.getInstance()
        cal.timeInMillis = originalMs

        return when (repeatRule) {
            com.holidaycount.app.data.model.RepeatRule.NONE -> originalMs
            com.holidaycount.app.data.model.RepeatRule.YEARLY -> {
                val month = cal.get(Calendar.MONTH)
                val day = cal.get(Calendar.DAY_OF_MONTH)
                getNextOccurrence(month + 1, day)
            }
            com.holidaycount.app.data.model.RepeatRule.MONTHLY -> {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val now = Calendar.getInstance()
                now.set(Calendar.DAY_OF_MONTH, day)
                now.set(Calendar.HOUR_OF_DAY, 0)
                now.set(Calendar.MINUTE, 0)
                now.set(Calendar.SECOND, 0)
                now.set(Calendar.MILLISECOND, 0)
                if (now.timeInMillis < today) {
                    now.add(Calendar.MONTH, 1)
                }
                now.timeInMillis
            }
        }
    }

    /**
     * 格式化剩余天数为显示文本
     */
    fun formatDaysLeft(days: Int): String {
        return when {
            days < 0 -> "已过去 ${-days} 天"
            days == 0 -> "就是今天！"
            days == 1 -> "还有明天"
            else -> "还有 $days 天"
        }
    }

    /**
     * 获取明天 00:00:00 的时间戳
     */
    fun tomorrowStartMs(): Long {
        return todayStartMs() + 86400000L
    }
}
