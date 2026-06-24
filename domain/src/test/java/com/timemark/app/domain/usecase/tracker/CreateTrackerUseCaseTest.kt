package com.timemark.app.domain.usecase.tracker

import com.timemark.app.domain.model.ReminderFrequency
import com.timemark.app.domain.model.TimePeriod
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.model.TrackerType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * CreateTrackerUseCase 单元测试
 *
 * 验证创建打卡项目的业务逻辑。
 */
class CreateTrackerUseCaseTest {

    private lateinit var fakeRepository: FakeTrackerRepository
    private lateinit var useCase: CreateTrackerUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeTrackerRepository()
        useCase = CreateTrackerUseCase(fakeRepository)
    }

    @Test
    fun invoke_新Tracker_返回非零ID() = runTest {
        val tracker = createTracker(name = "喝水")
        val id = useCase(tracker)
        assertTrue("新创建的 Tracker ID 应大于 0", id > 0)
    }

    @Test
    fun invoke_新Tracker_保存到仓库() = runTest {
        val tracker = createTracker(name = "运动健身")
        val id = useCase(tracker)
        val saved = fakeRepository.getTrackerById(id).first()
        assertNotNull(saved)
        assertEquals("运动健身", saved!!.name)
    }

    @Test
    fun invoke_多个Tracker_ID递增() = runTest {
        val id1 = useCase(createTracker(name = "打卡1"))
        val id2 = useCase(createTracker(name = "打卡2"))
        val id3 = useCase(createTracker(name = "打卡3"))
        assertTrue("ID 应递增", id2 > id1)
        assertTrue("ID 应递增", id3 > id2)
    }

    @Test
    fun invoke_完整字段_正确保存() = runTest {
        val tracker = Tracker(
            name = "阅读",
            icon = "📖",
            color = "#673AB7",
            type = TrackerType.IMAGE_TEXT,
            unit = "页",
            targetValue = 30.0,
            description = "每日阅读 30 页",
            timePeriod = TimePeriod.ALL_DAY,
            aiEnabled = true,
            reminderEnabled = true,
            reminderTime = "20:00",
            reminderFrequency = ReminderFrequency.DAILY
        )
        val id = useCase(tracker)
        val saved = fakeRepository.getTrackerById(id).first()
        assertNotNull(saved)
        assertEquals("阅读", saved!!.name)
        assertEquals("📖", saved.icon)
        assertEquals("#673AB7", saved.color)
        assertEquals(TrackerType.IMAGE_TEXT, saved.type)
        assertEquals("页", saved.unit)
        assertEquals(30.0, saved.targetValue, 0.001)
        assertEquals("每日阅读 30 页", saved.description)
        assertTrue(saved.aiEnabled)
        assertTrue(saved.reminderEnabled)
        assertEquals("20:00", saved.reminderTime)
    }

    @Test
    fun invoke_无目标Tracker_正确保存() = runTest {
        val tracker = createTracker(name = "体重记录", targetValue = 0.0)
        val id = useCase(tracker)
        val saved = fakeRepository.getTrackerById(id).first()
        assertNotNull(saved)
        assertEquals(0.0, saved!!.targetValue, 0.001)
        assertTrue(!saved.hasTarget)
    }

    @Test
    fun invoke_不可见Tracker_正确保存() = runTest {
        val tracker = createTracker(name = "隐藏打卡", isVisible = false)
        val id = useCase(tracker)
        val saved = fakeRepository.getTrackerById(id).first()
        assertNotNull(saved)
        assertTrue(!saved!!.isVisible)
    }

    /** 辅助方法：创建测试用 Tracker */
    private fun createTracker(
        name: String = "测试打卡",
        targetValue: Double = 1.0,
        isVisible: Boolean = true
    ): Tracker = Tracker(
        name = name,
        icon = "📝",
        color = "#2196F3",
        type = TrackerType.COUNT,
        unit = "次",
        targetValue = targetValue,
        isVisible = isVisible
    )
}
