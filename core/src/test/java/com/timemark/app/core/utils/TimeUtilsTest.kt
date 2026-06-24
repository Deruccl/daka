package com.timemark.app.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * TimeUtils 单元测试
 *
 * 覆盖时间格式化、解析、问候语、日期计算等核心方法。
 */
class TimeUtilsTest {

    @Test
    fun formatDate_标准日期_返回中文格式() {
        // 给定一个明确的日期
        val date = LocalDate.of(2024, 3, 5)
        // 格式化后应包含年月日和星期
        val result = TimeUtils.formatDate(date)
        // 验证包含中文日期格式
        assertTrue("应包含 2024年", result.contains("2024年"))
        assertTrue("应包含 3月", result.contains("3月"))
        assertTrue("应包含 5日", result.contains("5日"))
        assertTrue("应包含星期", result.contains("星期"))
    }

    @Test
    fun formatDate_年初日期_正确格式化() {
        val date = LocalDate.of(2024, 1, 1)
        val result = TimeUtils.formatDate(date)
        assertTrue(result.contains("2024年1月1日"))
    }

    @Test
    fun formatDate_年末日期_正确格式化() {
        val date = LocalDate.of(2024, 12, 31)
        val result = TimeUtils.formatDate(date)
        assertTrue(result.contains("2024年12月31日"))
    }

    @Test
    fun formatTime_标准时间_返回HHmm格式() {
        val time = LocalTime.of(9, 30)
        assertEquals("09:30", TimeUtils.formatTime(time))
    }

    @Test
    fun formatTime_午夜时间_返回0000() {
        val time = LocalTime.of(0, 0)
        assertEquals("00:00", TimeUtils.formatTime(time))
    }

    @Test
    fun formatTime_最大时间_返回2359() {
        val time = LocalTime.of(23, 59)
        assertEquals("23:59", TimeUtils.formatTime(time))
    }

    @Test
    fun formatDateTime_标准日期时间_返回完整格式() {
        val dateTime = LocalDateTime.of(2024, 6, 15, 14, 30)
        assertEquals("2024-06-15 14:30", TimeUtils.formatDateTime(dateTime))
    }

    @Test
    fun parseDate_有效日期字符串_返回LocalDate() {
        val result = TimeUtils.parseDate("2024-06-15")
        assertNotNull(result)
        assertEquals(LocalDate.of(2024, 6, 15), result)
    }

    @Test
    fun parseDate_无效格式_返回null() {
        val result = TimeUtils.parseDate("2024/06/15")
        assertNull(result)
    }

    @Test
    fun parseDate_空字符串_返回null() {
        val result = TimeUtils.parseDate("")
        assertNull(result)
    }

    @Test
    fun parseDate_非法日期_返回null() {
        // 2 月 30 日不存在
        val result = TimeUtils.parseDate("2024-02-30")
        assertNull(result)
    }

    @Test
    fun greeting_当前时间_返回非空字符串() {
        val greeting = TimeUtils.greeting()
        // 问候语应为预定义的几个值之一
        val validGreetings = listOf("早上好", "上午好", "中午好", "下午好", "晚上好", "夜深了")
        assertTrue("问候语 '$greeting' 应在有效列表中", greeting in validGreetings)
    }

    @Test
    fun startOfDay_任意日期_返回午夜() {
        val date = LocalDate.of(2024, 6, 15)
        val start = TimeUtils.startOfDay(date)
        assertEquals(0, start.hour)
        assertEquals(0, start.minute)
        assertEquals(0, start.second)
    }

    @Test
    fun endOfDay_任意日期_返回235959() {
        val date = LocalDate.of(2024, 6, 15)
        val end = TimeUtils.endOfDay(date)
        assertEquals(23, end.hour)
        assertEquals(59, end.minute)
        assertEquals(59, end.second)
    }

    @Test
    fun startOfWeek_周三_返回本周周一() {
        // 2024-06-19 是周三
        val date = LocalDate.of(2024, 6, 19)
        val monday = TimeUtils.startOfWeek(date)
        assertEquals(LocalDate.of(2024, 6, 17), monday)
    }

    @Test
    fun startOfWeek_周一_返回当天() {
        // 2024-06-17 是周一
        val date = LocalDate.of(2024, 6, 17)
        val monday = TimeUtils.startOfWeek(date)
        assertEquals(date, monday)
    }

    @Test
    fun startOfWeek_周日_返回上周周一() {
        // 2024-06-23 是周日
        val date = LocalDate.of(2024, 6, 23)
        val monday = TimeUtils.startOfWeek(date)
        assertEquals(LocalDate.of(2024, 6, 17), monday)
    }

    @Test
    fun startOfMonth_月中日期_返回月初() {
        val date = LocalDate.of(2024, 6, 15)
        assertEquals(LocalDate.of(2024, 6, 1), TimeUtils.startOfMonth(date))
    }

    @Test
    fun startOfMonth_月初日期_返回当天() {
        val date = LocalDate.of(2024, 6, 1)
        assertEquals(date, TimeUtils.startOfMonth(date))
    }

    @Test
    fun startOfYear_年中日期_返回年初() {
        val date = LocalDate.of(2024, 6, 15)
        assertEquals(LocalDate.of(2024, 1, 1), TimeUtils.startOfYear(date))
    }

    @Test
    fun startOfYear_年初日期_返回当天() {
        val date = LocalDate.of(2024, 1, 1)
        assertEquals(date, TimeUtils.startOfYear(date))
    }

    @Test
    fun daysBetween_同一天_返回0() {
        val date = LocalDate.of(2024, 6, 15)
        assertEquals(0, TimeUtils.daysBetween(date, date))
    }

    @Test
    fun daysBetween_连续两天_返回1() {
        val start = LocalDate.of(2024, 6, 15)
        val end = LocalDate.of(2024, 6, 16)
        assertEquals(1, TimeUtils.daysBetween(start, end))
    }

    @Test
    fun daysBetween_跨月_返回正确天数() {
        val start = LocalDate.of(2024, 6, 28)
        val end = LocalDate.of(2024, 7, 5)
        assertEquals(7, TimeUtils.daysBetween(start, end))
    }

    @Test
    fun daysBetween_结束日期早于开始日期_返回负数() {
        val start = LocalDate.of(2024, 6, 16)
        val end = LocalDate.of(2024, 6, 15)
        assertEquals(-1, TimeUtils.daysBetween(start, end))
    }

    @Test
    fun daysBetween_跨年_返回正确天数() {
        val start = LocalDate.of(2024, 12, 30)
        val end = LocalDate.of(2025, 1, 5)
        assertEquals(6, TimeUtils.daysBetween(start, end))
    }

    @Test
    fun now_调用_返回当前时间() {
        val before = LocalDateTime.now().minusSeconds(1)
        val now = TimeUtils.now()
        val after = LocalDateTime.now().plusSeconds(1)
        assertTrue("now 应在调用前后之间", now.isAfter(before) && now.isBefore(after))
    }

    @Test
    fun today_调用_返回当前日期() {
        assertEquals(LocalDate.now(), TimeUtils.today())
    }
}
