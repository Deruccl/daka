package com.timemark.app.data.repository

import com.timemark.app.data.db.dao.RecordDao
import com.timemark.app.data.db.dao.StatsDao
import com.timemark.app.data.db.dao.TrackerDao
import com.timemark.app.data.db.entity.DailyStatsEntity
import com.timemark.app.data.mapper.toDomain
import com.timemark.app.domain.model.DailyStats
import com.timemark.app.domain.model.DailyValue
import com.timemark.app.domain.model.RangeStats
import com.timemark.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * StatsRepository 接口实现。
 * 负责每日统计的查询、范围统计计算、连续打卡天数计算及统计重算。
 *
 * @param statsDao 统计数据访问对象
 * @param recordDao 记录数据访问对象（用于重算时聚合）
 * @param trackerDao 打卡项目数据访问对象（用于获取目标值）
 */
class StatsRepositoryImpl @Inject constructor(
    private val statsDao: StatsDao,
    private val recordDao: RecordDao,
    private val trackerDao: TrackerDao
) : StatsRepository {

    /** 获取指定 Tracker 在指定日期的每日统计 */
    override fun getDailyStats(trackerId: Long, date: String): Flow<DailyStats?> =
        statsDao.getDailyStats(trackerId, date).map { it?.toDomain() }

    /** 获取所有 Tracker 在指定日期的每日统计列表 */
    override fun getAllTrackersDailyStats(date: String): Flow<List<DailyStats>> =
        statsDao.getAllTrackersDailyStats(date).map { entities -> entities.map { it.toDomain() } }

    /** 获取指定 Tracker 在日期范围内的范围统计 */
    override fun getRangeStats(
        trackerId: Long,
        startDate: String,
        endDate: String
    ): Flow<RangeStats> = combine(
        statsDao.getRangeStats(trackerId, startDate, endDate),
        trackerDao.getTrackerById(trackerId)
    ) { statsList, tracker ->
        // 构建每日数值列表
        val dailyValues = statsList.map {
            DailyValue(it.date, it.totalValue, it.count, it.completed)
        }
        val totalValue = statsList.sumOf { it.totalValue }
        val totalCount = statsList.sumOf { it.count }
        val completedDays = statsList.count { it.completed }
        val totalDays = statsList.size
        val target = tracker?.targetValue ?: 0.0

        RangeStats(
            trackerId = trackerId,
            startDate = startDate,
            endDate = endDate,
            totalValue = totalValue,
            totalCount = totalCount,
            avgValue = if (totalDays > 0) totalValue / totalDays else 0.0,
            maxValue = dailyValues.maxOfOrNull { it.value } ?: 0.0,
            minValue = dailyValues.filter { it.value > 0 }.minOfOrNull { it.value } ?: 0.0,
            completedDays = completedDays,
            totalDays = totalDays,
            completionRate = if (totalDays > 0) completedDays.toFloat() / totalDays else 0f,
            streak = calculateStreak(dailyValues),
            dailyValues = dailyValues
        )
    }

    /** 获取指定 Tracker 从今天往前数的连续完成天数 */
    override fun getStreak(trackerId: Long): Flow<Int> = flow {
        val today = LocalDate.now()
        var streak = 0
        var date = today
        // 从今天往前遍历，遇到未完成的天数即停止
        while (true) {
            val dateStr = date.format(DateTimeFormatter.ISO_DATE)
            val stats = statsDao.getDailyStats(trackerId, dateStr).first()
            if (stats?.completed == true) {
                streak++
                date = date.minusDays(1)
            } else {
                break
            }
        }
        emit(streak)
    }

    /** 重新计算指定 Tracker 在指定日期的每日统计 */
    override suspend fun recalculateDailyStats(trackerId: Long, date: String) {
        val total = recordDao.getDailyTotal(trackerId, date) ?: 0.0
        val count = recordDao.getDailyCount(trackerId, date)
        val tracker = trackerDao.getTrackerByIdOnce(trackerId)
        // 完成判定：有目标值且当日总值达到目标
        val completed = tracker?.let { it.targetValue > 0 && total >= it.targetValue } ?: false
        val stats = DailyStatsEntity(
            trackerId = trackerId,
            date = date,
            totalValue = total,
            count = count,
            completed = completed,
            extra = "{}"
        )
        statsDao.upsert(stats)
    }

    /** 从最后一天往前数连续完成的天数 */
    private fun calculateStreak(dailyValues: List<DailyValue>): Int {
        var streak = 0
        for (v in dailyValues.reversed()) {
            if (v.completed) streak++ else break
        }
        return streak
    }
}
