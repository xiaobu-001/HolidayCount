package com.holidaycount.app

import com.holidaycount.app.utils.DateCalculation
import com.holidaycount.app.utils.LunarCalendar
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * 日期计算单元测试
 */
class DateCalculationTest {

    @Test
    fun testDaysUntilFuture() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 10)
        val result = DateCalculation.daysUntil(cal.timeInMillis)
        assertEquals(10, result)
    }

    @Test
    fun testDaysUntilPast() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -5)
        val result = DateCalculation.daysUntil(cal.timeInMillis)
        assertEquals(-5, result)
    }

    @Test
    fun testDaysUntilToday() {
        val today = DateCalculation.todayStartMs()
        val result = DateCalculation.daysUntil(today)
        assertEquals(0, result)
    }

    @Test
    fun testGetNextOccurrence_futureDate() {
        // 测试12月25日（圣诞节）
        val result = DateCalculation.getNextOccurrence(12, 25)
        assertTrue(result >= DateCalculation.todayStartMs())
    }

    @Test
    fun testFormatDate() {
        val cal = Calendar.getInstance()
        cal.set(2026, 0, 1) // 2026年1月1日
        val formatted = DateCalculation.formatDate(cal.timeInMillis)
        assertTrue(formatted.contains("2026"))
        assertTrue(formatted.contains("1"))
    }

    @Test
    fun testFormatDaysLeft_today() {
        assertEquals("就是今天！", DateCalculation.formatDaysLeft(0))
    }

    @Test
    fun testFormatDaysLeft_tomorrow() {
        assertEquals("还有明天", DateCalculation.formatDaysLeft(1))
    }

    @Test
    fun testFormatDaysLeft_negative() {
        val result = DateCalculation.formatDaysLeft(-3)
        assertTrue(result.contains("3"))
        assertTrue(result.contains("过去"))
    }
}

/**
 * 农历计算单元测试
 */
class LunarCalendarTest {

    @Test
    fun testSpringFestival2026() {
        // 2026年春节：2月17日
        val sf = LunarCalendar.lunarToSolar(2026, 1, 1)
        val cal = Calendar.getInstance()
        cal.timeInMillis = sf.timeInMillis
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(1, cal.get(Calendar.MONTH)) // 月份从0开始，1=2月
        assertEquals(17, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testSpringFestival2025() {
        // 2025年春节：1月29日
        val sf = LunarCalendar.lunarToSolar(2025, 1, 1)
        val cal = Calendar.getInstance()
        cal.timeInMillis = sf.timeInMillis
        assertEquals(2025, cal.get(Calendar.YEAR))
        assertEquals(0, cal.get(Calendar.MONTH)) // 0=1月
        assertEquals(29, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testLeapMonthDetection() {
        // 2023年有闰二月
        val leapMonth = LunarCalendar.getLeapMonth(2023)
        assertEquals(2, leapMonth)
    }

    @Test
    fun testLunarMonthDays() {
        // 农历月份大月30天，小月29天
        val days = LunarCalendar.getLunarMonthDays(2026, 1)
        assertTrue(days == 29 || days == 30)
    }

    @Test
    fun testSolarToLunar() {
        // 2026年2月17日 = 农历正月初一
        val lunar = LunarCalendar.solarToLunar(2026, 2, 17)
        assertEquals(2026, lunar.year)
        assertEquals(1, lunar.month)
        assertEquals(1, lunar.day)
    }

    @Test
    fun testLunarIsLeapYear() {
        assertTrue(LunarCalendar.isLeapYear(2024))
        assertFalse(LunarCalendar.isLeapYear(2023))
        assertTrue(LunarCalendar.isLeapYear(2000))
        assertFalse(LunarCalendar.isLeapYear(1900))
    }
}
