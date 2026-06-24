package com.timemark.app.domain.repository

import com.timemark.app.domain.model.DailyStats
import com.timemark.app.domain.model.RangeStats
import kotlinx.coroutines.flow.Flow

/** 统计仓库接口 */
interface StatsRepository {
    fun getDailyStats(trackerId: Long, date: String): Flow<DailyStats?>
    fun getRangeStats(trackerId: Long, startDate: String, endDate: String): Flow<RangeStats>
    fun getAllTrackersDailyStats(date: String): Flow<List<DailyStats>>
    fun getStreak(trackerId: Long): Flow<Int>
    suspend fun recalculateDailyStats(trackerId: Long, date: String)
}
