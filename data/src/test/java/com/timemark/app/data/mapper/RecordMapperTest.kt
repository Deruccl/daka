package com.timemark.app.data.mapper

import com.timemark.app.data.db.entity.RecordEntity
import com.timemark.app.domain.model.Record
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * RecordMapper 单元测试
 *
 * 验证 RecordEntity ↔ Record Domain 模型的双向转换。
 */
class RecordMapperTest {

    @Test
    fun toDomain_完整字段_正确转换() {
        val entity = RecordEntity(
            id = 1L,
            trackerId = 10L,
            value = 3.5,
            date = "2024-06-15",
            time = "14:30",
            timestamp = 1718447400000L,
            note = "测试记录",
            images = "[\"img1.jpg\",\"img2.jpg\"]",
            tags = "[\"健康\",\"饮水\"]",
            mood = "开心",
            duration = 1800000L,
            createdAt = 1718447400000L,
            updatedAt = 1718447400000L
        )

        val domain = entity.toDomain()
        assertEquals(1L, domain.id)
        assertEquals(10L, domain.trackerId)
        assertEquals(3.5, domain.value, 0.001)
        assertEquals("2024-06-15", domain.date)
        assertEquals("14:30", domain.time)
        assertEquals(1718447400000L, domain.timestamp)
        assertEquals("测试记录", domain.note)
        assertEquals(2, domain.images.size)
        assertEquals("img1.jpg", domain.images[0])
        assertEquals("img2.jpg", domain.images[1])
        assertEquals(2, domain.tags.size)
        assertEquals("健康", domain.tags[0])
        assertEquals("饮水", domain.tags[1])
        assertEquals("开心", domain.mood)
        assertEquals(1800000L, domain.duration)
        assertEquals(1718447400000L, domain.createdAt)
        assertEquals(1718447400000L, domain.updatedAt)
    }

    @Test
    fun toEntity_完整字段_正确转换() {
        val domain = Record(
            id = 100L,
            trackerId = 5L,
            value = 2.0,
            date = "2024-06-16",
            time = "09:00",
            timestamp = 1718500000000L,
            note = "直接传入",
            images = listOf("a.jpg", "b.jpg", "c.jpg"),
            tags = listOf("运动"),
            mood = null,
            duration = 0L,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val entity = domain.toEntity()
        assertEquals(100L, entity.id)
        assertEquals(5L, entity.trackerId)
        assertEquals(2.0, entity.value, 0.001)
        assertEquals("2024-06-16", entity.date)
        assertEquals("09:00", entity.time)
        assertEquals(1718500000000L, entity.timestamp)
        assertEquals("直接传入", entity.note)
        assertTrue(entity.images.contains("a.jpg"))
        assertTrue(entity.images.contains("b.jpg"))
        assertTrue(entity.images.contains("c.jpg"))
        assertTrue(entity.tags.contains("运动"))
        assertEquals(null, entity.mood)
        assertEquals(0L, entity.duration)
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.updatedAt)
    }

    @Test
    fun toDomain_空images_返回空列表() {
        val entity = createEntity(images = "")
        val domain = entity.toDomain()
        assertTrue(domain.images.isEmpty())
    }

    @Test
    fun toDomain_nullimages_返回空列表() {
        val entity = createEntity(images = null)
        val domain = entity.toDomain()
        assertTrue(domain.images.isEmpty())
    }

    @Test
    fun toDomain_空tags_返回空列表() {
        val entity = createEntity(tags = "")
        val domain = entity.toDomain()
        assertTrue(domain.tags.isEmpty())
    }

    @Test
    fun toDomain_无效JSONimages_返回空列表() {
        val entity = createEntity(images = "invalid_json")
        val domain = entity.toDomain()
        assertTrue(domain.images.isEmpty())
    }

    @Test
    fun toDomain_无效JSONtags_返回空列表() {
        val entity = createEntity(tags = "invalid_json")
        val domain = entity.toDomain()
        assertTrue(domain.tags.isEmpty())
    }

    @Test
    fun toEntity_空images列表_编码为空数组() {
        val domain = createDomain(images = emptyList())
        val entity = domain.toEntity()
        assertEquals("[]", entity.images)
    }

    @Test
    fun toEntity_空tags列表_编码为空数组() {
        val domain = createDomain(tags = emptyList())
        val entity = domain.toEntity()
        assertEquals("[]", entity.tags)
    }

    @Test
    fun toEntity_单元素列表_正确编码() {
        val domain = createDomain(images = listOf("only.jpg"))
        val entity = domain.toEntity()
        assertEquals("[\"only.jpg\"]", entity.images)
    }

    @Test
    fun toDomain_mood为null_正确处理() {
        val entity = createEntity(mood = null)
        val domain = entity.toDomain()
        assertNull(domain.mood)
    }

    @Test
    fun toDomain_mood为空字符串_保留空字符串() {
        val entity = createEntity(mood = "")
        val domain = entity.toDomain()
        assertEquals("", domain.mood)
    }

    @Test
    fun 双向转换_toDomain后toEntity_字段一致() {
        val original = Record(
            id = 200L,
            trackerId = 7L,
            value = 5.0,
            date = "2024-12-31",
            time = "23:59",
            timestamp = 1735631940000L,
            note = "年终记录",
            images = listOf("year1.jpg", "year2.jpg"),
            tags = listOf("年终", "总结"),
            mood = "感慨",
            duration = 7200000L,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val entity = original.toEntity()
        val restored = entity.toDomain()
        assertEquals(original.id, restored.id)
        assertEquals(original.trackerId, restored.trackerId)
        assertEquals(original.value, restored.value, 0.001)
        assertEquals(original.date, restored.date)
        assertEquals(original.time, restored.time)
        assertEquals(original.timestamp, restored.timestamp)
        assertEquals(original.note, restored.note)
        assertEquals(original.images, restored.images)
        assertEquals(original.tags, restored.tags)
        assertEquals(original.mood, restored.mood)
        assertEquals(original.duration, restored.duration)
        assertEquals(original.createdAt, restored.createdAt)
        assertEquals(original.updatedAt, restored.updatedAt)
    }

    @Test
    fun toDomain_零值字段_正确处理() {
        val entity = RecordEntity(
            id = 0L,
            trackerId = 0L,
            value = 0.0,
            date = "2024-01-01",
            time = "00:00",
            timestamp = 0L,
            note = "",
            images = "[]",
            tags = "[]",
            mood = null,
            duration = 0L,
            createdAt = 0L,
            updatedAt = 0L
        )
        val domain = entity.toDomain()
        assertEquals(0L, domain.id)
        assertEquals(0.0, domain.value, 0.001)
        assertTrue(domain.images.isEmpty())
        assertTrue(domain.tags.isEmpty())
        assertNull(domain.mood)
    }

    /** 辅助方法：创建测试用 RecordEntity */
    private fun createEntity(
        images: String? = "[]",
        tags: String? = "[]",
        mood: String? = null
    ): RecordEntity = RecordEntity(
        id = 1L,
        trackerId = 1L,
        value = 1.0,
        date = "2024-06-15",
        time = "12:00",
        timestamp = 0L,
        note = "",
        images = images ?: "[]",
        tags = tags ?: "[]",
        mood = mood,
        duration = 0L,
        createdAt = 0L,
        updatedAt = 0L
    )

    /** 辅助方法：创建测试用 Record */
    private fun createDomain(
        images: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ): Record = Record(
        id = 1L,
        trackerId = 1L,
        value = 1.0,
        date = "2024-06-15",
        time = "12:00",
        timestamp = 0L,
        images = images,
        tags = tags
    )
}
