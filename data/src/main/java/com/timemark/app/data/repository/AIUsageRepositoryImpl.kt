package com.timemark.app.data.repository

import com.timemark.app.data.db.dao.AIUsageDao
import com.timemark.app.data.mapper.toDomain
import com.timemark.app.data.mapper.toEntity
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIUsage
import com.timemark.app.domain.repository.AIUsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * AIUsageRepository 接口实现。
 * 通过 AIUsageDao 访问数据库，使用 AIMapper 完成 Entity/Domain 模型互转。
 *
 * 注意：AIUsage 表使用 timestamp (Long, 毫秒) 存储，而接口使用 String 日期。
 * 内部通过 ZoneId.systemDefault() 完成 LocalDate 与 epochMilli 的转换。
 */
class AIUsageRepositoryImpl @Inject constructor(
    private val aiUsageDao: AIUsageDao
) : AIUsageRepository {

    /** 获取所有 AI 使用记录（按时间倒序） */
    override fun getAllUsage(): Flow<List<AIUsage>> =
        aiUsageDao.getAllUsage().map { entities -> entities.map { it.toDomain() } }

    /** 获取日期范围内的 AI 使用记录 */
    override fun getUsageByDateRange(startDate: String, endDate: String): Flow<List<AIUsage>> {
        val (startTs, endTs) = dateRangeToTimestamps(startDate, endDate)
        return aiUsageDao.getUsageByDateRange(startTs, endTs)
            .map { entities -> entities.map { it.toDomain() } }
    }

    /** 获取指定配置的 AI 使用记录 */
    override fun getUsageByConfig(configId: Long): Flow<List<AIUsage>> =
        aiUsageDao.getUsageByConfig(configId).map { entities -> entities.map { it.toDomain() } }

    /** 获取指定功能类型的 AI 使用记录 */
    override fun getUsageByFeature(feature: AIFeature): Flow<List<AIUsage>> =
        aiUsageDao.getUsageByFeature(feature.name)
            .map { entities -> entities.map { it.toDomain() } }

    /** 获取今日的 AI 使用记录（从今天 00:00:00 起） */
    override fun getTodayUsage(): Flow<List<AIUsage>> {
        val startTs = dateToTimestamp(LocalDate.now().toString())
        return aiUsageDao.getUsageSince(startTs).map { entities -> entities.map { it.toDomain() } }
    }

    /** 获取最近 7 天的 AI 使用记录 */
    override fun getWeekUsage(): Flow<List<AIUsage>> {
        val startTs = dateToTimestamp(LocalDate.now().minusDays(7).toString())
        return aiUsageDao.getUsageSince(startTs).map { entities -> entities.map { it.toDomain() } }
    }

    /** 获取最近 30 天的 AI 使用记录 */
    override fun getMonthUsage(): Flow<List<AIUsage>> {
        val startTs = dateToTimestamp(LocalDate.now().minusDays(30).toString())
        return aiUsageDao.getUsageSince(startTs).map { entities -> entities.map { it.toDomain() } }
    }

    /** 新增 AI 使用记录，返回自增 ID */
    override suspend fun insertUsage(usage: AIUsage): Long =
        aiUsageDao.insert(usage.toEntity())

    /** 获取指定日期的 Token 总量（输入、输出） */
    override suspend fun getTotalTokensByDate(date: String): Pair<Int, Int> {
        val (startTs, endTs) = dateRangeToTimestamps(date, date)
        val input = aiUsageDao.getTotalInputTokens(startTs, endTs) ?: 0
        val output = aiUsageDao.getTotalOutputTokens(startTs, endTs) ?: 0
        return input to output
    }

    /** 获取日期范围内的总费用 */
    override suspend fun getTotalCostByDateRange(startDate: String, endDate: String): Double {
        val (startTs, endTs) = dateRangeToTimestamps(startDate, endDate)
        return aiUsageDao.getTotalCost(startTs, endTs) ?: 0.0
    }

    /** 将日期字符串（yyyy-MM-dd）转换为当天 00:00:00 的毫秒时间戳 */
    private fun dateToTimestamp(date: String): Long {
        val localDate = LocalDate.parse(date)
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /** 将日期范围字符串转换为起止毫秒时间戳（结束为当天 23:59:59.999） */
    private fun dateRangeToTimestamps(startDate: String, endDate: String): Pair<Long, Long> {
        val start = LocalDate.parse(startDate)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val end = LocalDate.parse(endDate).plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli() - 1
        return start to end
    }
}
