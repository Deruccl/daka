package com.timemark.app.data.repository

import com.timemark.app.data.db.entity.TrackerEntity
import com.timemark.app.domain.model.ReminderFrequency
import com.timemark.app.domain.model.TimePeriod
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.model.TrackerType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TrackerRepositoryImpl 单元测试
 *
 * 使用 FakeTrackerDao 验证 Repository 实现的 CRUD 操作与 Entity/Domain 转换。
 */
class TrackerRepositoryImplTest {

    private lateinit var fakeDao: FakeTrackerDao
    private lateinit var repository: TrackerRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeTrackerDao()
        repository = TrackerRepositoryImpl(fakeDao)
    }

    @Test
    fun getAllTrackers_空数据库_返回空列表() = runTest {
        val result = repository.getAllTrackers().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getAllTrackers_有数据_返回Domain模型列表() = runTest {
        fakeDao.setEntities(listOf(createEntity(1L, "打卡1"), createEntity(2L, "打卡2")))
        val result = repository.getAllTrackers().first()
        assertEquals(2, result.size)
        assertTrue(result[0] is Tracker)
    }

    @Test
    fun getAllTrackers_按sortOrder排序() = runTest {
        fakeDao.setEntities(listOf(
            createEntity(1L, "排序3", sortOrder = 3),
            createEntity(2L, "排序1", sortOrder = 1),
            createEntity(3L, "排序2", sortOrder = 2)
        ))
        val result = repository.getAllTrackers().first()
        assertEquals("排序1", result[0].name)
        assertEquals("排序2", result[1].name)
        assertEquals("排序3", result[2].name)
    }

    @Test
    fun getVisibleTrackers_仅返回可见项() = runTest {
        fakeDao.setEntities(listOf(
            createEntity(1L, "可见", isVisible = true),
            createEntity(2L, "不可见", isVisible = false)
        ))
        val result = repository.getVisibleTrackers().first()
        assertEquals(1, result.size)
        assertEquals("可见", result[0].name)
    }

    @Test
    fun getVisibleTrackers_全部不可见_返回空列表() = runTest {
        fakeDao.setEntities(listOf(
            createEntity(1L, "不可见1", isVisible = false),
            createEntity(2L, "不可见2", isVisible = false)
        ))
        val result = repository.getVisibleTrackers().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getTrackerById_存在ID_返回对应Tracker() = runTest {
        fakeDao.setEntities(listOf(createEntity(10L, "目标打卡")))
        val result = repository.getTrackerById(10L).first()
        assertNotNull(result)
        assertEquals("目标打卡", result!!.name)
    }

    @Test
    fun getTrackerById_不存在ID_返回null() = runTest {
        fakeDao.setEntities(listOf(createEntity(1L, "打卡")))
        val result = repository.getTrackerById(999L).first()
        assertNull(result)
    }

    @Test
    fun insertTracker_新Tracker_返回非零ID() = runTest {
        val tracker = createDomain(name = "新打卡")
        val id = repository.insertTracker(tracker)
        assertTrue(id > 0)
    }

    @Test
    fun insertTracker_保存后可查询() = runTest {
        val tracker = createDomain(name = "保存测试")
        val id = repository.insertTracker(tracker)
        val saved = repository.getTrackerById(id).first()
        assertNotNull(saved)
        assertEquals("保存测试", saved!!.name)
    }

    @Test
    fun insertTracker_枚举字段正确转换() = runTest {
        val tracker = Tracker(
            name = "运动",
            icon = "🏃",
            color = "#F44336",
            type = TrackerType.DURATION,
            unit = "分钟",
            targetValue = 30.0,
            timePeriod = TimePeriod.MORNING,
            reminderFrequency = ReminderFrequency.INTERVAL,
            reminderDays = listOf(1, 3, 5)
        )
        val id = repository.insertTracker(tracker)
        val saved = repository.getTrackerById(id).first()
        assertNotNull(saved)
        assertEquals(TrackerType.DURATION, saved!!.type)
        assertEquals(TimePeriod.MORNING, saved.timePeriod)
        assertEquals(ReminderFrequency.INTERVAL, saved.reminderFrequency)
        assertEquals(listOf(1, 3, 5), saved.reminderDays)
    }

    @Test
    fun updateTracker_更新后字段变化() = runTest {
        val id = repository.insertTracker(createDomain(name = "原名"))
        val original = repository.getTrackerById(id).first()!!
        val updated = original.copy(name = "新名", targetValue = 10.0)
        repository.updateTracker(updated)
        val result = repository.getTrackerById(id).first()
        assertEquals("新名", result!!.name)
        assertEquals(10.0, result.targetValue, 0.001)
    }

    @Test
    fun deleteTracker_删除后不可查询() = runTest {
        val id = repository.insertTracker(createDomain(name = "待删除"))
        repository.deleteTracker(id)
        val result = repository.getTrackerById(id).first()
        assertNull(result)
    }

    @Test
    fun deleteTracker_删除不影响其他() = runTest {
        val id1 = repository.insertTracker(createDomain(name = "打卡1"))
        val id2 = repository.insertTracker(createDomain(name = "打卡2"))
        repository.deleteTracker(id1)
        val result1 = repository.getTrackerById(id1).first()
        val result2 = repository.getTrackerById(id2).first()
        assertNull(result1)
        assertNotNull(result2)
    }

    @Test
    fun updateSortOrder_批量更新_顺序变化() = runTest {
        fakeDao.setEntities(listOf(
            createEntity(1L, "打卡1", sortOrder = 0),
            createEntity(2L, "打卡2", sortOrder = 1),
            createEntity(3L, "打卡3", sortOrder = 2)
        ))
        repository.updateSortOrder(listOf(
            1L to 2,
            2L to 0,
            3L to 1
        ))
        val result = repository.getAllTrackers().first()
        assertEquals("打卡2", result[0].name)
        assertEquals("打卡3", result[1].name)
        assertEquals("打卡1", result[2].name)
    }

    @Test
    fun insertTracker_多个Tracker_ID递增() = runTest {
        val id1 = repository.insertTracker(createDomain(name = "1"))
        val id2 = repository.insertTracker(createDomain(name = "2"))
        val id3 = repository.insertTracker(createDomain(name = "3"))
        assertTrue(id2 > id1)
        assertTrue(id3 > id2)
    }

    /** 辅助方法：创建测试用 TrackerEntity */
    private fun createEntity(
        id: Long,
        name: String,
        sortOrder: Int = 0,
        isVisible: Boolean = true
    ): TrackerEntity = TrackerEntity(
        id = id,
        name = name,
        icon = "📝",
        color = "#2196F3",
        type = "COUNT",
        unit = "次",
        targetValue = 1.0,
        description = "",
        timePeriod = "ALL_DAY",
        customStartTime = null,
        customEndTime = null,
        isVisible = isVisible,
        sortOrder = sortOrder,
        aiEnabled = false,
        reminderEnabled = false,
        reminderTime = null,
        reminderFrequency = "DAILY",
        reminderIntervalHours = 2,
        reminderDays = "[]",
        createdAt = 0L,
        updatedAt = 0L
    )

    /** 辅助方法：创建测试用 Tracker */
    private fun createDomain(name: String): Tracker = Tracker(
        name = name,
        icon = "📝",
        color = "#2196F3",
        type = TrackerType.COUNT,
        unit = "次",
        targetValue = 1.0
    )
}
