package com.timemark.app.domain.usecase.stats

import com.timemark.app.domain.model.DailyStats
import com.timemark.app.domain.model.RangeStats
import com.timemark.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * StatsRepository 的内存假实现，用于单元测试。
 */
class FakeStatsRepository : StatsRepository {

    private val _dailyStatsMap = MutableStateFlow<Map<Pair<Long, String>, DailyStats>>(emptyMap())
    private val _streakMap = MutableStateFlow<Map<Long, Int>>(emptyMap())

    /** 预设每日统计 */
    fun setDailyStats(trackerId: Long, date: String, stats: DailyStats) {
        _dailyStatsMap.value = _dailyStatsMap.value + (trackerId to date) to stats
    }

    /** 预设连续天数 */
    fun setStreak(trackerId: Long, streak: Int) {
        _streakMap.value = _streakMap.value + (trackerId to streak)
    }

    override fun getDailyStats(trackerId: Long, date: String): Flow<DailyStats?> =
        _dailyStatsMap.map { it[trackerId to date] }

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
    ).asStateFlow()

    override fun getAllTrackersDailyStats(date: String): Flow<List<DailyStats>> =
        _dailyStatsMap.map { map ->
            map.filterKeys { (_, d) -> d == date }.values.toList()
        }

    override fun getStreak(trackerId: Long): Flow<Int> =
        _streakMap.map { it[trackerId] ?: 0 }

    override suspend fun recalculateDailyStats(trackerId: Long, date: String) {
        // 模拟重算，不做实际操作
    }
}
