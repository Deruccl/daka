package com.timemark.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tracker 模型单元测试
 *
 * 覆盖 Tracker 数据类的计算属性与默认值。
 */
class TrackerTest {

    @Test
    fun hasTarget_目标值大于0_返回true() {
        val tracker = createTracker(targetValue = 8.0)
        assertTrue(tracker.hasTarget)
    }

    @Test
    fun hasTarget_目标值为0_返回false() {
        val tracker = createTracker(targetValue = 0.0)
        assertFalse(tracker.hasTarget)
    }

    @Test
    fun hasTarget_目标值为负数_返回false() {
        val tracker = createTracker(targetValue = -1.0)
        assertFalse(tracker.hasTarget)
    }

    @Test
    fun hasTarget_目标值为小数_返回true() {
        val tracker = createTracker(targetValue = 0.5)
        assertTrue(tracker.hasTarget)
    }

    @Test
    fun 默认值_新建Tracker_使用正确默认值() {
        val tracker = Tracker(
            name = "喝水",
            icon = "💧",
            color = "#2196F3",
            type = TrackerType.COUNT
        )
        assertEquals("", tracker.unit)
        assertEquals(0.0, tracker.targetValue, 0.001)
        assertEquals("", tracker.description)
        assertEquals(TimePeriod.ALL_DAY, tracker.timePeriod)
        assertEquals(null, tracker.customStartTime)
        assertEquals(null, tracker.customEndTime)
        assertTrue(tracker.isVisible)
        assertEquals(0, tracker.sortOrder)
        assertFalse(tracker.aiEnabled)
        assertFalse(tracker.reminderEnabled)
        assertEquals(null, tracker.reminderTime)
        assertEquals(ReminderFrequency.DAILY, tracker.reminderFrequency)
        assertEquals(2, tracker.reminderIntervalHours)
        assertTrue(tracker.reminderDays.isEmpty())
    }

    @Test
    fun 自定义值_完整构造_字段正确赋值() {
        val tracker = Tracker(
            id = 100L,
            name = "运动",
            icon = "🏃",
            color = "#F44336",
            type = TrackerType.DURATION,
            unit = "分钟",
            targetValue = 30.0,
            description = "每日运动 30 分钟",
            timePeriod = TimePeriod.MORNING,
            customStartTime = "06:00",
            customEndTime = "12:00",
            isVisible = false,
            sortOrder = 5,
            aiEnabled = true,
            reminderEnabled = true,
            reminderTime = "07:00",
            reminderFrequency = ReminderFrequency.INTERVAL,
            reminderIntervalHours = 3,
            reminderDays = listOf(1, 3, 5)
        )
        assertEquals(100L, tracker.id)
        assertEquals("运动", tracker.name)
        assertEquals("🏃", tracker.icon)
        assertEquals("#F44336", tracker.color)
        assertEquals(TrackerType.DURATION, tracker.type)
        assertEquals("分钟", tracker.unit)
        assertEquals(30.0, tracker.targetValue, 0.001)
        assertEquals("每日运动 30 分钟", tracker.description)
        assertEquals(TimePeriod.MORNING, tracker.timePeriod)
        assertEquals("06:00", tracker.customStartTime)
        assertEquals("12:00", tracker.customEndTime)
        assertFalse(tracker.isVisible)
        assertEquals(5, tracker.sortOrder)
        assertTrue(tracker.aiEnabled)
        assertTrue(tracker.reminderEnabled)
        assertEquals("07:00", tracker.reminderTime)
        assertEquals(ReminderFrequency.INTERVAL, tracker.reminderFrequency)
        assertEquals(3, tracker.reminderIntervalHours)
        assertEquals(listOf(1, 3, 5), tracker.reminderDays)
    }

    @Test
    fun 各类型Tracker_均可正确创建() {
        // 测试所有打卡类型都能正确创建
        TrackerType.values().forEach { type ->
            val tracker = createTracker(type = type)
            assertEquals(type, tracker.type)
        }
    }

    @Test
    fun 各时间段Tracker_均可正确创建() {
        TimePeriod.values().forEach { period ->
            val tracker = createTracker(timePeriod = period)
            assertEquals(period, tracker.timePeriod)
        }
    }

    @Test
    fun 各提醒频率Tracker_均可正确创建() {
        ReminderFrequency.values().forEach { freq ->
            val tracker = createTracker(reminderFrequency = freq)
            assertEquals(freq, tracker.reminderFrequency)
        }
    }

    /** 辅助方法：创建默认 Tracker，可覆盖指定字段 */
    private fun createTracker(
        targetValue: Double = 0.0,
        type: TrackerType = TrackerType.COUNT,
        timePeriod: TimePeriod = TimePeriod.ALL_DAY,
        reminderFrequency: ReminderFrequency = ReminderFrequency.DAILY
    ): Tracker = Tracker(
        name = "测试打卡",
        icon = "📝",
        color = "#2196F3",
        type = type,
        targetValue = targetValue,
        timePeriod = timePeriod,
        reminderFrequency = reminderFrequency
    )
}
