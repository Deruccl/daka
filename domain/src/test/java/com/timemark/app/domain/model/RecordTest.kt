package com.timemark.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Record 模型单元测试
 *
 * 覆盖 Record 数据类的字段验证与默认值。
 */
class RecordTest {

    @Test
    fun 默认值_新建Record_使用正确默认值() {
        val record = Record(
            trackerId = 1L,
            date = "2024-06-15",
            time = "14:30",
            timestamp = System.currentTimeMillis()
        )
        assertEquals(1.0, record.value, 0.001)
        assertEquals("", record.note)
        assertTrue(record.images.isEmpty())
        assertTrue(record.tags.isEmpty())
        assertNull(record.mood)
        assertEquals(0L, record.duration)
    }

    @Test
    fun 完整构造_所有字段_正确赋值() {
        val timestamp = 1718447400000L
        val record = Record(
            id = 100L,
            trackerId = 5L,
            value = 3.5,
            date = "2024-06-15",
            time = "14:30",
            timestamp = timestamp,
            note = "今天喝了 3.5 杯水",
            images = listOf("/path/img1.jpg", "/path/img2.jpg"),
            tags = listOf("健康", "饮水"),
            mood = "开心",
            duration = 1800000L
        )
        assertEquals(100L, record.id)
        assertEquals(5L, record.trackerId)
        assertEquals(3.5, record.value, 0.001)
        assertEquals("2024-06-15", record.date)
        assertEquals("14:30", record.time)
        assertEquals(timestamp, record.timestamp)
        assertEquals("今天喝了 3.5 杯水", record.note)
        assertEquals(2, record.images.size)
        assertEquals("/path/img1.jpg", record.images[0])
        assertEquals(2, record.tags.size)
        assertEquals("健康", record.tags[0])
        assertEquals("开心", record.mood)
        assertEquals(1800000L, record.duration)
    }

    @Test
    fun value_零值_允许创建() {
        val record = createRecord(value = 0.0)
        assertEquals(0.0, record.value, 0.001)
    }

    @Test
    fun value_负值_允许创建() {
        // 模型层不限制负值，由业务层校验
        val record = createRecord(value = -1.0)
        assertEquals(-1.0, record.value, 0.001)
    }

    @Test
    fun value_大数值_正确存储() {
        val record = createRecord(value = 99999.99)
        assertEquals(99999.99, record.value, 0.001)
    }

    @Test
    fun images_空列表_默认为空() {
        val record = createRecord()
        assertTrue(record.images.isEmpty())
    }

    @Test
    fun images_多图片_顺序保持() {
        val record = createRecord(images = listOf("a.jpg", "b.jpg", "c.jpg", "d.jpg"))
        assertEquals(4, record.images.size)
        assertEquals("a.jpg", record.images[0])
        assertEquals("d.jpg", record.images[3])
    }

    @Test
    fun tags_多标签_顺序保持() {
        val record = createRecord(tags = listOf("运动", "健康", "每日"))
        assertEquals(3, record.tags.size)
        assertEquals("运动", record.tags[0])
    }

    @Test
    fun mood_空值_默认为null() {
        val record = createRecord()
        assertNull(record.mood)
    }

    @Test
    fun mood_有值_正确赋值() {
        val record = createRecord(mood = "😊")
        assertEquals("😊", record.mood)
    }

    @Test
    fun duration_默认值_为零() {
        val record = createRecord()
        assertEquals(0L, record.duration)
    }

    @Test
    fun duration_计时型_正确赋值() {
        val record = createRecord(duration = 3600000L) // 1 小时
        assertEquals(3600000L, record.duration)
    }

    @Test
    fun date_格式_为yyyy_MM_dd() {
        val record = createRecord(date = "2024-12-31")
        assertEquals("2024-12-31", record.date)
    }

    @Test
    fun time_格式_为HHmm() {
        val record = createRecord(time = "23:59")
        assertEquals("23:59", record.time)
    }

    /** 辅助方法：创建默认 Record，可覆盖指定字段 */
    private fun createRecord(
        value: Double = 1.0,
        date: String = "2024-06-15",
        time: String = "12:00",
        images: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        mood: String? = null,
        duration: Long = 0L
    ): Record = Record(
        trackerId = 1L,
        value = value,
        date = date,
        time = time,
        timestamp = System.currentTimeMillis(),
        images = images,
        tags = tags,
        mood = mood,
        duration = duration
    )
}
