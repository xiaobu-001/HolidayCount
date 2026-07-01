package com.holidaycount.app.data.repository

import com.holidaycount.app.data.model.EventItem
import com.holidaycount.app.data.model.EventType
import com.holidaycount.app.data.model.RepeatRule
import com.holidaycount.app.utils.DateCalculation
import android.graphics.Color

/**
 * 内置节假日数据源
 * 包含中国法定节假日 + 国际常用节日
 */
object BuiltInHolidays {

    data class HolidayDef(
        val name: String,
        val emoji: String,
        val month: Int,
        val day: Int,
        val isLunar: Boolean = false,
        val lunarMonth: Int = 0,
        val lunarDay: Int = 0,
        val color: Int = Color.parseColor("#6200EE"),
        val priority: Int = 10 // 值越小优先级越高
    )

    /** 内置节假日定义列表 */
    private val HOLIDAYS = listOf(
        // ============== 中国法定节假日（公历）==============
        HolidayDef("元旦", "🎆", month = 1, day = 1,
            color = Color.parseColor("#E91E63"), priority = 1),
        HolidayDef("劳动节", "👷", month = 5, day = 1,
            color = Color.parseColor("#FF5722"), priority = 1),
        HolidayDef("国庆节", "🇨🇳", month = 10, day = 1,
            color = Color.parseColor("#F44336"), priority = 1),

        // ============== 中国传统节日（农历）==============
        HolidayDef("春节", "🧧", month = 0, day = 0,
            isLunar = true, lunarMonth = 1, lunarDay = 1,
            color = Color.parseColor("#F44336"), priority = 1),
        HolidayDef("元宵节", "🏮", month = 0, day = 0,
            isLunar = true, lunarMonth = 1, lunarDay = 15,
            color = Color.parseColor("#FF9800"), priority = 3),
        HolidayDef("清明节", "🌿", month = 4, day = 4,
            color = Color.parseColor("#4CAF50"), priority = 2),  // 公历近似值
        HolidayDef("端午节", "🐉", month = 0, day = 0,
            isLunar = true, lunarMonth = 5, lunarDay = 5,
            color = Color.parseColor("#2196F3"), priority = 2),
        HolidayDef("七夕节", "💑", month = 0, day = 0,
            isLunar = true, lunarMonth = 7, lunarDay = 7,
            color = Color.parseColor("#E91E63"), priority = 4),
        HolidayDef("中元节", "🕯️", month = 0, day = 0,
            isLunar = true, lunarMonth = 7, lunarDay = 15,
            color = Color.parseColor("#607D8B"), priority = 5),
        HolidayDef("中秋节", "🌕", month = 0, day = 0,
            isLunar = true, lunarMonth = 8, lunarDay = 15,
            color = Color.parseColor("#FF9800"), priority = 1),
        HolidayDef("重阳节", "🍂", month = 0, day = 0,
            isLunar = true, lunarMonth = 9, lunarDay = 9,
            color = Color.parseColor("#795548"), priority = 4),
        HolidayDef("冬至", "❄️", month = 12, day = 22,
            color = Color.parseColor("#3F51B5"), priority = 3),
        HolidayDef("腊八节", "🥣", month = 0, day = 0,
            isLunar = true, lunarMonth = 12, lunarDay = 8,
            color = Color.parseColor("#8D6E63"), priority = 5),
        HolidayDef("除夕", "🎉", month = 0, day = 0,
            isLunar = true, lunarMonth = 12, lunarDay = 30,
            color = Color.parseColor("#FF1744"), priority = 1),

        // ============== 国际节日（公历）==============
        HolidayDef("情人节", "💝", month = 2, day = 14,
            color = Color.parseColor("#E91E63"), priority = 4),
        HolidayDef("妇女节", "💐", month = 3, day = 8,
            color = Color.parseColor("#FF80AB"), priority = 4),
        HolidayDef("愚人节", "🃏", month = 4, day = 1,
            color = Color.parseColor("#FFEB3B"), priority = 5),
        HolidayDef("儿童节", "🧸", month = 6, day = 1,
            color = Color.parseColor("#8BC34A"), priority = 4),
        HolidayDef("感恩节", "🦃", month = 11, day = 24,
            color = Color.parseColor("#FF8F00"), priority = 5),
        HolidayDef("圣诞节", "🎄", month = 12, day = 25,
            color = Color.parseColor("#4CAF50"), priority = 3),
        HolidayDef("平安夜", "🌟", month = 12, day = 24,
            color = Color.parseColor("#388E3C"), priority = 4),
        HolidayDef("万圣节", "🎃", month = 10, day = 31,
            color = Color.parseColor("#FF6D00"), priority = 5),
        HolidayDef("新年前夜", "🥂", month = 12, day = 31,
            color = Color.parseColor("#9C27B0"), priority = 3)
    )

    /**
     * 获取所有内置节假日对应的 EventItem（计算下次日期）
     */
    fun getAllHolidays(): List<EventItem> {
        val result = mutableListOf<EventItem>()
        var id = -1L // 内置节假日使用负数 ID

        for (holiday in HOLIDAYS) {
            try {
                val targetMs = if (holiday.isLunar) {
                    DateCalculation.getNextLunarOccurrence(holiday.lunarMonth, holiday.lunarDay)
                } else {
                    // 特殊处理感恩节（11月第4个周四）
                    if (holiday.name == "感恩节") {
                        getThanksgivingDate()
                    } else {
                        DateCalculation.getNextOccurrence(holiday.month, holiday.day)
                    }
                }

                val daysLeft = DateCalculation.daysUntil(targetMs)

                result.add(
                    EventItem(
                        id = id,
                        name = holiday.name,
                        emoji = holiday.emoji,
                        targetDate = targetMs,
                        daysLeft = daysLeft,
                        eventType = EventType.BUILT_IN_HOLIDAY,
                        repeatRule = RepeatRule.YEARLY,
                        backgroundColor = holiday.color
                    )
                )
                id--
            } catch (e: Exception) {
                // 忽略计算失败的节日
            }
        }

        return result.sortedBy { it.daysLeft.let { d -> if (d < 0) Int.MAX_VALUE + d else d } }
    }

    /**
     * 计算感恩节日期（11月第4个周四）
     */
    private fun getThanksgivingDate(): Long {
        val cal = java.util.Calendar.getInstance()
        val year = cal.get(java.util.Calendar.YEAR)

        fun calcForYear(y: Int): Long {
            val c = java.util.Calendar.getInstance()
            c.set(y, 10, 1, 0, 0, 0) // 11月1日
            c.set(java.util.Calendar.MILLISECOND, 0)
            var thursdays = 0
            while (thursdays < 4) {
                if (c.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.THURSDAY) {
                    thursdays++
                }
                if (thursdays < 4) c.add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            return c.timeInMillis
        }

        val thisYear = calcForYear(year)
        return if (thisYear >= DateCalculation.todayStartMs()) thisYear else calcForYear(year + 1)
    }
}
