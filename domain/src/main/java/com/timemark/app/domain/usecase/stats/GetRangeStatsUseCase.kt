package com.timemark.app.domain.usecase.stats

import com.timemark.app.domain.model.RangeStats
import com.timemark.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取日期范围统计用例
 */
class GetRangeStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    /** 获取指定打卡项目在日期范围内的统计 */
    operator fun invoke(trackerId: Long, startDate: String, endDate: String): Flow<RangeStats> =
        statsRepository.getRangeStats(trackerId, startDate, endDate)
}
