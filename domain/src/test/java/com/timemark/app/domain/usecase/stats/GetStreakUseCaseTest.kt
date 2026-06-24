package com.timemark.app.domain.usecase.stats

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * GetStreakUseCase 单元测试
 *
 * 验证连续打卡天数查询逻辑。
 */
class GetStreakUseCaseTest {

    private lateinit var fakeRepository: FakeStatsRepository
    private lateinit var useCase: GetStreakUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeStatsRepository()
        useCase = GetStreakUseCase(fakeRepository)
    }

    @Test
    fun invoke_存在连续天数_返回正确值() = runTest {
        fakeRepository.setStreak(trackerId = 1L, streak = 7)
        val result = useCase(1L).first()
        assertEquals(7, result)
    }

    @Test
    fun invoke_不存在连续天数_返回0() = runTest {
        val result = useCase(999L).first()
        assertEquals(0, result)
    }

    @Test
    fun invoke_连续天数为0_返回0() = runTest {
        fakeRepository.setStreak(trackerId = 1L, streak = 0)
        val result = useCase(1L).first()
        assertEquals(0, result)
    }

    @Test
    fun invoke_大连续天数_正确返回() = runTest {
        fakeRepository.setStreak(trackerId = 1L, streak = 365)
        val result = useCase(1L).first()
        assertEquals(365, result)
    }

    @Test
    fun invoke_不同Tracker_返回各自连续天数() = runTest {
        fakeRepository.setStreak(trackerId = 1L, streak = 5)
        fakeRepository.setStreak(trackerId = 2L, streak = 30)
        fakeRepository.setStreak(trackerId = 3L, streak = 0)

        assertEquals(5, useCase(1L).first())
        assertEquals(30, useCase(2L).first())
        assertEquals(0, useCase(3L).first())
    }

    @Test
    fun invoke_空仓库_返回0() = runTest {
        val result = useCase(1L).first()
        assertEquals(0, result)
    }
}
