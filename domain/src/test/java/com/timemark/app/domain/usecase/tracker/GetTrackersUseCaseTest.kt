package com.timemark.app.domain.usecase.tracker

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
 * GetTrackersUseCase 单元测试
 *
 * 验证获取打卡项目列表的多种查询方式。
 */
class GetTrackersUseCaseTest {

    private lateinit var fakeRepository: FakeTrackerRepository
    private lateinit var useCase: GetTrackersUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeTrackerRepository()
        useCase = GetTrackersUseCase(fakeRepository)
    }

    @Test
    fun invoke_空仓库_返回空列表() = runTest {
        val result = useCase().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_有数据_返回全部Tracker() = runTest {
        val trackers = listOf(
            createTracker(1L, "打卡1", sortOrder = 2),
            createTracker(2L, "打卡2", sortOrder = 1)
        )
        fakeRepository.setTrackers(trackers)
        val result = useCase().first()
        assertEquals(2, result.size)
    }

    @Test
    fun invoke_按sortOrder排序() = runTest {
        val trackers = listOf(
            createTracker(1L, "排序2", sortOrder = 2),
            createTracker(2L, "排序1", sortOrder = 1),
            createTracker(3L, "排序3", sortOrder = 3)
        )
        fakeRepository.setTrackers(trackers)
        val result = useCase().first()
        assertEquals("排序1", result[0].name)
        assertEquals("排序2", result[1].name)
        assertEquals("排序3", result[2].name)
    }

    @Test
    fun visible_仅返回可见Tracker() = runTest {
        val trackers = listOf(
            createTracker(1L, "可见1", isVisible = true),
            createTracker(2L, "不可见", isVisible = false),
            createTracker(3L, "可见2", isVisible = true)
        )
        fakeRepository.setTrackers(trackers)
        val result = useCase.visible().first()
        assertEquals(2, result.size)
        assertEquals("可见1", result[0].name)
        assertEquals("可见2", result[1].name)
    }

    @Test
    fun visible_全部不可见_返回空列表() = runTest {
        val trackers = listOf(
            createTracker(1L, "不可见1", isVisible = false),
            createTracker(2L, "不可见2", isVisible = false)
        )
        fakeRepository.setTrackers(trackers)
        val result = useCase.visible().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun byId_存在ID_返回对应Tracker() = runTest {
        val trackers = listOf(
            createTracker(1L, "打卡1"),
            createTracker(2L, "打卡2")
        )
        fakeRepository.setTrackers(trackers)
        val result = useCase.byId(2L).first()
        assertNotNull(result)
        assertEquals("打卡2", result!!.name)
    }

    @Test
    fun byId_不存在ID_返回null() = runTest {
        val trackers = listOf(createTracker(1L, "打卡1"))
        fakeRepository.setTrackers(trackers)
        val result = useCase.byId(999L).first()
        assertNull(result)
    }

    @Test
    fun byId_空仓库_返回null() = runTest {
        val result = useCase.byId(1L).first()
        assertNull(result)
    }

    /** 辅助方法：创建测试用 Tracker */
    private fun createTracker(
        id: Long,
        name: String,
        sortOrder: Int = 0,
        isVisible: Boolean = true
    ): Tracker = Tracker(
        id = id,
        name = name,
        icon = "📝",
        color = "#2196F3",
        type = TrackerType.COUNT,
        unit = "次",
        targetValue = 1.0,
        isVisible = isVisible,
        sortOrder = sortOrder
    )
}
