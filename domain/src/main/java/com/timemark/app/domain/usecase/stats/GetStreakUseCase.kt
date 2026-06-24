package com.timemark.app.domain.usecase.stats

import com.timemark.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取连续打卡天数用例
 */
class GetStreakUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    /** 获取指定打卡项目的连续打卡天数 */
    operator fun invoke(trackerId: Long): Flow<Int> =
        statsRepository.getStreak(trackerId)
}
