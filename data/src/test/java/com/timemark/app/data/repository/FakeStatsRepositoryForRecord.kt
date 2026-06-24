package com.timemark.app.data.repository

import com.timemark.app.domain.model.DailyStats
import com.timemark.app.domain.model.RangeStats
import com.timemark.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * StatsRepository 的内存假实现，用于 RecordRepositoryImpl 测试。
 * 仅实现 recalculateDailyStats，其余方法返回空数据。
 */
class FakeStatsRepositoryForRecord : StatsRepository {

    var recalculateCallCount = 0
        private set

    var lastRecalculatedTrackerId: Long? = null
        private set

    var lastRecalculatedDate: String? = null
        private set

    override fun getDailyStats(trackerId: Long, date: String): Flow<DailyStats?> =
        MutableStateFlow(null)

    override fun getRangeStats(
        trackerId: Long,
        startDate: String,
        endDate: String
    ): Flow<RangeStats> = MutableStateFlow(
        RangeStats(
            trackerId = trackerId,
            startDate = startDate,
            endDate = endDate,
            totalValue = 0.0,
            totalCount = 0,
            avgValue = 0.0,
            maxValue = 0.0,
            minValue = 0.0,
            completedDays = 0,
            totalDays = 0,
            completionRate = 0f,
            streak = 0,
            dailyValues = emptyList()
        )
    )

    override fun getAllTrackersDailyStats(date: String): Flow<List<DailyStats>> =
        MutableStateFlow(emptyList())

    override fun getStreak(trackerId: Long): Flow<Int> = MutableStateFlow(0)

    override suspend fun recalculateDailyStats(trackerId: Long, date: String) {
        recalculateCallCount++
        lastRecalculatedTrackerId = trackerId
        lastRecalculatedDate = date
    }
}
