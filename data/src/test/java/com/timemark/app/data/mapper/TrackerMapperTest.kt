package com.timemark.app.data.mapper

import com.timemark.app.data.db.entity.TrackerEntity
import com.timemark.app.domain.model.ReminderFrequency
import com.timemark.app.domain.model.TimePeriod
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.model.TrackerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TrackerMapper 单元测试
 *
 * 验证 TrackerEntity ↔ Tracker Domain 模型的双向转换。
 */
class TrackerMapperTest {

    @Test
    fun toDomain_完整字段_正确转换() {
        val entity = TrackerEntity(
            id = 1L,
            name = "喝水",
            icon = "💧",
            color = "#2196F3",
            type = "COUNT",
            unit = "杯",
            targetValue = 8.0,
            description = "每日饮水",
            timePeriod = "ALL_DAY",
            customStartTime = null,
            customEndTime = null,
            isVisible = true,
            sortOrder = 0,
            aiEnabled = false,
            reminderEnabled = false,
            reminderTime = null,
            reminderFrequency = "DAILY",
            reminderIntervalHours = 2,
            reminderDays = "[1,3,5]",
            createdAt = 1718447400000L,
            updatedAt = 1718447400000L
        )

        val domain = entity.toDomain()
        assertEquals(1L, domain.id)
        assertEquals("喝水", domain.name)
        assertEquals("💧", domain.icon)
        assertEquals("#2196F3", domain.color)
        assertEquals(TrackerType.COUNT, domain.type)
        assertEquals("杯", domain.unit)
        assertEquals(8.0, domain.targetValue, 0.001)
        assertEquals("每日饮水", domain.description)
        assertEquals(TimePeriod.ALL_DAY, domain.timePeriod)
        assertEquals(null, domain.customStartTime)
        assertEquals(null, domain.customEndTime)
        assertTrue(domain.isVisible)
        assertEquals(0, domain.sortOrder)
        assertTrue(!domain.aiEnabled)
        assertTrue(!domain.reminderEnabled)
        assertEquals(null, domain.reminderTime)
        assertEquals(ReminderFrequency.DAILY, domain.reminderFrequency)
        assertEquals(2, domain.reminderIntervalHours)
        assertEquals(listOf(1, 3, 5), domain.reminderDays)
        assertEquals(1718447400000L, domain.createdAt)
        assertEquals(1718447400000L, domain.updatedAt)
    }

    @Test
    fun toEntity_完整字段_正确转换() {
        val domain = Tracker(
            id = 10L,
            name = "运动",
            icon = "🏃",
            color = "#F44336",
            type = TrackerType.DURATION,
            unit = "分钟",
            targetValue = 30.0,
            description = "每日运动",
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
            reminderDays = listOf(2, 4, 6),
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val entity = domain.toEntity()
        assertEquals(10L, entity.id)
        assertEquals("运动", entity.name)
        assertEquals("🏃", entity.icon)
        assertEquals("#F44336", entity.color)
        assertEquals("DURATION", entity.type)
        assertEquals("分钟", entity.unit)
        assertEquals(30.0, entity.targetValue, 0.001)
        assertEquals("每日运动", entity.description)
        assertEquals("MORNING", entity.timePeriod)
        assertEquals("06:00", entity.customStartTime)
        assertEquals("12:00", entity.customEndTime)
        assertTrue(!entity.isVisible)
        assertEquals(5, entity.sortOrder)
        assertTrue(entity.aiEnabled)
        assertTrue(entity.reminderEnabled)
        assertEquals("07:00", entity.reminderTime)
        assertEquals("INTERVAL", entity.reminderFrequency)
        assertEquals(3, entity.reminderIntervalHours)
        assertEquals("[2,4,6]", entity.reminderDays)
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.updatedAt)
    }

    @Test
    fun toDomain_无效type_回退到COUNT() {
        val entity = createEntity(type = "INVALID_TYPE")
        val domain = entity.toDomain()
        assertEquals(TrackerType.COUNT, domain.type)
    }

    @Test
    fun toDomain_无效timePeriod_回退到ALL_DAY() {
        val entity = createEntity(timePeriod = "INVALID")
        val domain = entity.toDomain()
        assertEquals(TimePeriod.ALL_DAY, domain.timePeriod)
    }

    @Test
    fun toDomain_无效reminderFrequency_回退到DAILY() {
        val entity = createEntity(reminderFrequency = "INVALID")
        val domain = entity.toDomain()
        assertEquals(ReminderFrequency.DAILY, domain.reminderFrequency)
    }

    @Test
    fun toDomain_空reminderDays_返回空列表() {
        val entity = createEntity(reminderDays = "")
        val domain = entity.toDomain()
        assertTrue(domain.reminderDays.isEmpty())
    }

    @Test
    fun toDomain_空字符串reminderDays_返回空列表() {
        val entity = createEntity(reminderDays = "")
        val domain = entity.toDomain()
        assertTrue(domain.reminderDays.isEmpty())
    }

    @Test
    fun toDomain_多元素reminderDays_正确解析() {
        val entity = createEntity(reminderDays = "[1,2,3,4,5,6,7]")
        val domain = entity.toDomain()
        assertEquals(7, domain.reminderDays.size)
        assertEquals(1, domain.reminderDays[0])
        assertEquals(7, domain.reminderDays[6])
    }

    @Test
    fun toEntity_空reminderDays_编码为空数组() {
        val domain = createDomain(reminderDays = emptyList())
        val entity = domain.toEntity()
        assertEquals("[]", entity.reminderDays)
    }

    @Test
    fun toEntity_多元素reminderDays_正确编码() {
        val domain = createDomain(reminderDays = listOf(1, 3, 5, 7))
        val entity = domain.toEntity()
        assertEquals("[1,3,5,7]", entity.reminderDays)
    }

    @Test
    fun 双向转换_toDomain后toEntity_字段一致() {
        val original = Tracker(
            id = 50L,
            name = "冥想",
            icon = "🧘",
            color = "#00BCD4",
            type = TrackerType.DURATION,
            unit = "分钟",
            targetValue = 15.0,
            description = "冥想练习",
            timePeriod = TimePeriod.EVENING,
            customStartTime = "20:00",
            customEndTime = "21:00",
            isVisible = true,
            sortOrder = 3,
            aiEnabled = true,
            reminderEnabled = true,
            reminderTime = "20:00",
            reminderFrequency = ReminderFrequency.WEEKLY,
            reminderIntervalHours = 4,
            reminderDays = listOf(1, 2, 3, 4, 5),
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val entity = original.toEntity()
        val restored = entity.toDomain()
        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.icon, restored.icon)
        assertEquals(original.color, restored.color)
        assertEquals(original.type, restored.type)
        assertEquals(original.unit, restored.unit)
        assertEquals(original.targetValue, restored.targetValue, 0.001)
        assertEquals(original.description, restored.description)
        assertEquals(original.timePeriod, restored.timePeriod)
        assertEquals(original.customStartTime, restored.customStartTime)
        assertEquals(original.customEndTime, restored.customEndTime)
        assertEquals(original.isVisible, restored.isVisible)
        assertEquals(original.sortOrder, restored.sortOrder)
        assertEquals(original.aiEnabled, restored.aiEnabled)
        assertEquals(original.reminderEnabled, restored.reminderEnabled)
        assertEquals(original.reminderTime, restored.reminderTime)
        assertEquals(original.reminderFrequency, restored.reminderFrequency)
        assertEquals(original.reminderIntervalHours, restored.reminderIntervalHours)
        assertEquals(original.reminderDays, restored.reminderDays)
        assertEquals(original.createdAt, restored.createdAt)
        assertEquals(original.updatedAt, restored.updatedAt)
    }

    @Test
    fun toDomain_所有TrackerType_正确转换() {
        TrackerType.values().forEach { type ->
            val entity = createEntity(type = type.name)
            val domain = entity.toDomain()
            assertEquals(type, domain.type)
        }
    }

    @Test
    fun toDomain_所有TimePeriod_正确转换() {
        TimePeriod.values().forEach { period ->
            val entity = createEntity(timePeriod = period.name)
            val domain = entity.toDomain()
            assertEquals(period, domain.timePeriod)
        }
    }

    @Test
    fun toDomain_所有ReminderFrequency_正确转换() {
        ReminderFrequency.values().forEach { freq ->
            val entity = createEntity(reminderFrequency = freq.name)
            val domain = entity.toDomain()
            assertEquals(freq, domain.reminderFrequency)
        }
    }

    /** 辅助方法：创建测试用 TrackerEntity */
    private fun createEntity(
        type: String = "COUNT",
        timePeriod: String = "ALL_DAY",
        reminderFrequency: String = "DAILY",
        reminderDays: String = "[]"
    ): TrackerEntity = TrackerEntity(
        id = 1L,
        name = "测试",
        icon = "📝",
        color = "#000000",
        type = type,
        unit = "次",
        targetValue = 1.0,
        description = "",
        timePeriod = timePeriod,
        customStartTime = null,
        customEndTime = null,
        isVisible = true,
        sortOrder = 0,
        aiEnabled = false,
        reminderEnabled = false,
        reminderTime = null,
        reminderFrequency = reminderFrequency,
        reminderIntervalHours = 2,
        reminderDays = reminderDays,
        createdAt = 0L,
        updatedAt = 0L
    )

    /** 辅助方法：创建测试用 Tracker */
    private fun createDomain(
        reminderDays: List<Int> = emptyList()
    ): Tracker = Tracker(
        id = 1L,
        name = "测试",
        icon = "📝",
        color = "#000000",
        type = TrackerType.COUNT,
        unit = "次",
        targetValue = 1.0,
        reminderDays = reminderDays
    )
}
