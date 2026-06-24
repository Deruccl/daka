package com.timemark.app.domain.usecase.stats

import com.timemark.app.domain.model.DailyStats
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * GetDailyStatsUseCase 单元测试
 *
 * 验证每日统计查询逻辑。
 */
class GetDailyStatsUseCaseTest {

    private lateinit var fakeRepository: FakeStatsRepository
    private lateinit var useCase: GetDailyStatsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeStatsRepository()
        useCase = GetDailyStatsUseCase(fakeRepository)
    }

    @Test
    fun invoke_存在统计_返回对应数据() = runTest {
        val stats = DailyStats(
            id = 1L,
            date = "2024-06-15",
            trackerId = 10L,
            totalValue = 5.0,
            count = 3,
            completed = true
        )
        fakeRepository.setDailyStats(10L, "2024-06-15", stats)

        val result = useCase(trackerId = 10L, date = "2024-06-15").first()
        assertNotNull(result)
        assertEquals(5.0, result!!.totalValue, 0.001)
        assertEquals(3, result.count)
        assertTrue(result.completed)
    }

    @Test
    fun invoke_不存在统计_返回null() = runTest {
        val result = useCase(trackerId = 999L, date = "2024-06-15").first()
        assertNull(result)
    }

    @Test
    fun invoke_不同日期_返回各自统计() = runTest {
        val stats1 = DailyStats(date = "2024-06-15", trackerId = 1L, totalValue = 3.0, count = 2)
        val stats2 = DailyStats(date = "2024-06-16", trackerId = 1L, totalValue = 5.0, count = 4)
        fakeRepository.setDailyStats(1L, "2024-06-15", stats1)
        fakeRepository.setDailyStats(1L, "2024-06-16", stats2)

        val result1 = useCase(1L, "2024-06-15").first()
        val result2 = useCase(1L, "2024-06-16").first()
        assertEquals(3.0, result1!!.totalValue, 0.001)
        assertEquals(5.0, result2!!.totalValue, 0.001)
    }

    @Test
    fun invoke_不同Tracker_返回各自统计() = runTest {
        val stats1 = DailyStats(date = "2024-06-15", trackerId = 1L, totalValue = 2.0, count = 1)
        val stats2 = DailyStats(date = "2024-06-15", trackerId = 2L, totalValue = 8.0, count = 4)
        fakeRepository.setDailyStats(1L, "2024-06-15", stats1)
        fakeRepository.setDailyStats(2L, "2024-06-15", stats2)

        val result1 = useCase(1L, "2024-06-15").first()
        val result2 = useCase(2L, "2024-06-15").first()
        assertEquals(2.0, result1!!.totalValue, 0.001)
        assertEquals(8.0, result2!!.totalValue, 0.001)
    }

    @Test
    fun allTrackers_存在统计_返回列表() = runTest {
        val stats1 = DailyStats(date = "2024-06-15", trackerId = 1L, totalValue = 2.0)
        val stats2 = DailyStats(date = "2024-06-15", trackerId = 2L, totalValue = 4.0)
        fakeRepository.setDailyStats(1L, "2024-06-15", stats1)
        fakeRepository.setDailyStats(2L, "2024-06-15", stats2)

        val result = useCase.allTrackers("2024-06-15").first()
        assertEquals(2, result.size)
    }

    @Test
    fun allTrackers_无统计_返回空列表() = runTest {
        val result = useCase.allTrackers("2024-06-15").first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun allTrackers_仅返回指定日期统计() = runTest {
        val stats1 = DailyStats(date = "2024-06-15", trackerId = 1L)
        val stats2 = DailyStats(date = "2024-06-16", trackerId = 2L)
        fakeRepository.setDailyStats(1L, "2024-06-15", stats1)
        fakeRepository.setDailyStats(2L, "2024-06-16", stats2)

        val result = useCase.allTrackers("2024-06-15").first()
        assertEquals(1, result.size)
        assertEquals(1L, result[0].trackerId)
    }
}
