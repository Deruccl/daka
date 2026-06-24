package com.timemark.app.data.repository

import com.timemark.app.data.db.entity.RecordEntity
import com.timemark.app.domain.model.Record
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * RecordRepositoryImpl 单元测试
 *
 * 使用 FakeRecordDao 验证 Repository 实现的 CRUD 操作、
 * Entity/Domain 转换以及统计重算触发。
 */
class RecordRepositoryImplTest {

    private lateinit var fakeDao: FakeRecordDao
    private lateinit var fakeStatsRepository: FakeStatsRepositoryForRecord
    private lateinit var repository: RecordRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeRecordDao()
        fakeStatsRepository = FakeStatsRepositoryForRecord()
        repository = RecordRepositoryImpl(fakeDao, fakeStatsRepository)
    }

    @Test
    fun getRecordsByTrackerAndDate_空数据库_返回空列表() = runTest {
        val result = repository.getRecordsByTrackerAndDate(1L, "2024-06-15").first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getRecordsByTrackerAndDate_有数据_返回Domain列表() = runTest {
        fakeDao.setEntities(listOf(
            createEntity(1L, 10L, "2024-06-15"),
            createEntity(2L, 10L, "2024-06-15"),
            createEntity(3L, 10L, "2024-06-16")
        ))
        val result = repository.getRecordsByTrackerAndDate(10L, "2024-06-15").first()
        assertEquals(2, result.size)
        assertTrue(result[0] is Record)
    }

    @Test
    fun getRecordsByTrackerAndDate_仅返回匹配trackerId的记录() = runTest {
        fakeDao.setEntities(listOf(
            createEntity(1L, 10L, "2024-06-15"),
            createEntity(2L, 20L, "2024-06-15")
        ))
        val result = repository.getRecordsByTrackerAndDate(10L, "2024-06-15").first()
        assertEquals(1, result.size)
        assertEquals(10L, result[0].trackerId)
    }

    @Test
    fun getRecordsByTrackerAndDateRange_返回范围内记录() = runTest {
        fakeDao.setEntities(listOf(
            createEntity(1L, 10L, "2024-06-14"),
            createEntity(2L, 10L, "2024-06-15"),
            createEntity(3L, 10L, "2024-06-16"),
            createEntity(4L, 10L, "2024-06-17")
        ))
        val result = repository.getRecordsByTrackerAndDateRange(10L, "2024-06-15", "2024-06-16").first()
        assertEquals(2, result.size)
    }

    @Test
    fun getAllRecordsByDate_返回所有tracker的当日记录() = runTest {
        fakeDao.setEntities(listOf(
            createEntity(1L, 10L, "2024-06-15"),
            createEntity(2L, 20L, "2024-06-15"),
            createEntity(3L, 30L, "2024-06-16")
        ))
        val result = repository.getAllRecordsByDate("2024-06-15").first()
        assertEquals(2, result.size)
    }

    @Test
    fun getAllRecordsByDateRange_返回范围内所有记录() = runTest {
        fakeDao.setEntities(listOf(
            createEntity(1L, 10L, "2024-06-14"),
            createEntity(2L, 20L, "2024-06-15"),
            createEntity(3L, 30L, "2024-06-17")
        ))
        val result = repository.getAllRecordsByDateRange("2024-06-14", "2024-06-15").first()
        assertEquals(2, result.size)
    }

    @Test
    fun insertRecord_新记录_返回非零ID() = runTest {
        val record = createDomain(trackerId = 1L)
        val id = repository.insertRecord(record)
        assertTrue(id > 0)
    }

    @Test
    fun insertRecord_保存后可查询() = runTest {
        val record = createDomain(trackerId = 5L, date = "2024-06-15")
        val id = repository.insertRecord(record)
        val saved = repository.getRecordById(id)
        assertNotNull(saved)
        assertEquals(5L, saved!!.trackerId)
        assertEquals("2024-06-15", saved.date)
    }

    @Test
    fun insertRecord_触发统计重算() = runTest {
        val record = createDomain(trackerId = 3L, date = "2024-06-15")
        repository.insertRecord(record)
        assertEquals(1, fakeStatsRepository.recalculateCallCount)
        assertEquals(3L, fakeStatsRepository.lastRecalculatedTrackerId)
        assertEquals("2024-06-15", fakeStatsRepository.lastRecalculatedDate)
    }

    @Test
    fun insertRecord_images和tags正确转换() = runTest {
        val record = createDomain(
            trackerId = 1L,
            images = listOf("img1.jpg", "img2.jpg"),
            tags = listOf("标签1", "标签2")
        )
        val id = repository.insertRecord(record)
        val saved = repository.getRecordById(id)
        assertEquals(2, saved!!.images.size)
        assertEquals("img1.jpg", saved.images[0])
        assertEquals(2, saved.tags.size)
        assertEquals("标签1", saved.tags[0])
    }

    @Test
    fun updateRecord_更新后字段变化() = runTest {
        val id = repository.insertRecord(createDomain(trackerId = 1L, value = 1.0))
        val original = repository.getRecordById(id)!!
        val updated = original.copy(value = 5.0, note = "已更新")
        repository.updateRecord(updated)
        val result = repository.getRecordById(id)
        assertEquals(5.0, result!!.value, 0.001)
        assertEquals("已更新", result.note)
    }

    @Test
    fun updateRecord_触发统计重算() = runTest {
        val id = repository.insertRecord(createDomain(trackerId = 2L, date = "2024-06-15"))
        val original = repository.getRecordById(id)!!
        fakeStatsRepository.recalculateCallCount = 0 // 重置计数
        repository.updateRecord(original.copy(value = 3.0))
        assertEquals(1, fakeStatsRepository.recalculateCallCount)
    }

    @Test
    fun deleteRecord_删除后不可查询() = runTest {
        val id = repository.insertRecord(createDomain(trackerId = 1L))
        repository.deleteRecord(id)
        val result = repository.getRecordById(id)
        assertNull(result)
    }

    @Test
    fun deleteRecord_触发统计重算() = runTest {
        val id = repository.insertRecord(createDomain(trackerId = 4L, date = "2024-06-15"))
        fakeStatsRepository.recalculateCallCount = 0
        repository.deleteRecord(id)
        assertEquals(1, fakeStatsRepository.recalculateCallCount)
        assertEquals(4L, fakeStatsRepository.lastRecalculatedTrackerId)
    }

    @Test
    fun deleteRecord_删除不影响其他记录() = runTest {
        val id1 = repository.insertRecord(createDomain(trackerId = 1L))
        val id2 = repository.insertRecord(createDomain(trackerId = 1L))
        repository.deleteRecord(id1)
        assertNull(repository.getRecordById(id1))
        assertNotNull(repository.getRecordById(id2))
    }

    @Test
    fun getRecordById_不存在ID_返回null() = runTest {
        val result = repository.getRecordById(999L)
        assertNull(result)
    }

    @Test
    fun insertRecord_多条记录_ID递增() = runTest {
        val id1 = repository.insertRecord(createDomain(trackerId = 1L))
        val id2 = repository.insertRecord(createDomain(trackerId = 1L))
        val id3 = repository.insertRecord(createDomain(trackerId = 1L))
        assertTrue(id2 > id1)
        assertTrue(id3 > id2)
    }

    /** 辅助方法：创建测试用 RecordEntity */
    private fun createEntity(
        id: Long,
        trackerId: Long,
        date: String
    ): RecordEntity = RecordEntity(
        id = id,
        trackerId = trackerId,
        value = 1.0,
        date = date,
        time = "12:00",
        timestamp = 0L,
        note = "",
        images = "[]",
        tags = "[]",
        mood = null,
        duration = 0L,
        createdAt = 0L,
        updatedAt = 0L
    )

    /** 辅助方法：创建测试用 Record */
    private fun createDomain(
        trackerId: Long,
        value: Double = 1.0,
        date: String = "2024-06-15",
        images: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ): Record = Record(
        trackerId = trackerId,
        value = value,
        date = date,
        time = "12:00",
        timestamp = System.currentTimeMillis(),
        images = images,
        tags = tags
    )
}
