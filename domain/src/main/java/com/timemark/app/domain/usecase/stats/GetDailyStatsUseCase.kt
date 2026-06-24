package com.timemark.app.domain.usecase.stats

import com.timemark.app.domain.model.DailyStats
import com.timemark.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取每日统计用例
 *
 * 支持查询单个打卡项目或全部项目的每日统计。
 */
class GetDailyStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    /** 获取指定打卡项目某一天的统计 */
    operator fun invoke(trackerId: Long, date: String): Flow<DailyStats?> =
        statsRepository.getDailyStats(trackerId, date)

    /** 获取全部打卡项目某一天的统计 */
    fun allTrackers(date: String): Flow<List<DailyStats>> =
        statsRepository.getAllTrackersDailyStats(date)
}
