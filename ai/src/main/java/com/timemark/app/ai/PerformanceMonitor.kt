package com.timemark.app.ai

import com.timemark.app.domain.model.AIProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentLinkedDeque
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API 性能监控器（Task 36.4）
 *
 * 职责：
 * 1. 记录每次 AI 请求的响应时间、成功/失败、Token 用量
 * 2. 计算各 Provider 的性能指标：成功率、平均响应时间、质量评分
 * 3. 质量评分综合考量响应完整性、速度、Token 效率（0-100）
 * 4. 数据保存在内存中，仅保留最近 [MAX_RECORDS] 条请求记录
 *
 * 通过 [recordRequest] 在每次请求后记录，通过 [getProviderPerformance] 查询聚合指标。
 */
@Singleton
class PerformanceMonitor @Inject constructor() {

    /** 单次请求记录 */
    data class RequestRecord(
        val provider: AIProvider,
        val timestamp: Long,
        val success: Boolean,
        val responseTimeMs: Long,
        val tokensInput: Int,
        val tokensOutput: Int,
        val contentLength: Int
    )

    /**
     * Provider 性能指标（Task 36.4）
     *
     * @param provider 厂商
     * @param totalRequests 总请求数
     * @param successCount 成功请求数
     * @param failCount 失败请求数
     * @param avgResponseTime 平均响应时间（毫秒）
     * @param successRate 成功率（0-1）
     * @param qualityScore 质量评分（0-100，基于响应完整性、速度、Token 效率综合评分）
     */
    data class ProviderPerformance(
        val provider: AIProvider,
        val totalRequests: Int,
        val successCount: Int,
        val failCount: Int,
        val avgResponseTime: Long,
        val successRate: Float,
        val qualityScore: Int
    )

    companion object {
        /** 内存中保留的最大记录数 */
        private const val MAX_RECORDS = 1000

        /** 响应时间基准（毫秒），低于此值视为快速，评分满分 */
        private const val FAST_RESPONSE_MS = 2000L

        /** 响应时间上限（毫秒），超过此值速度评分为 0 */
        private const val SLOW_RESPONSE_MS = 30000L
    }

    /** 请求记录队列（线程安全） */
    private val records = ConcurrentLinkedDeque<RequestRecord>()

    /** 用于通知 Flow 订阅者数据更新的触发器 */
    private val _updateTrigger = MutableStateFlow(0L)

    /**
     * 记录一次请求（Task 36.4）
     *
     * 在每次 AI 请求完成后调用，记录响应时间、成功/失败、Token 用量等信息。
     * 当记录数超过 [MAX_RECORDS] 时，移除最早的记录。
     *
     * @param provider AI 厂商
     * @param success 是否成功
     * @param responseTimeMs 响应时间（毫秒）
     * @param tokensInput 输入 Token 数
     * @param tokensOutput 输出 Token 数
     * @param contentLength 响应内容长度（字符数）
     */
    fun recordRequest(
        provider: AIProvider,
        success: Boolean,
        responseTimeMs: Long,
        tokensInput: Int,
        tokensOutput: Int,
        contentLength: Int
    ) {
        val record = RequestRecord(
            provider = provider,
            timestamp = System.currentTimeMillis(),
            success = success,
            responseTimeMs = responseTimeMs,
            tokensInput = tokensInput,
            tokensOutput = tokensOutput,
            contentLength = contentLength
        )
        records.addLast(record)

        // 超出上限时移除最早记录
        while (records.size > MAX_RECORDS) {
            records.pollFirst()
        }

        // 通知订阅者
        _updateTrigger.value = System.currentTimeMillis()
    }

    /**
     * 获取指定 Provider 的性能指标（Task 36.4）
     *
     * 返回一个 Flow，当有新请求记录时自动更新。
     *
     * @param provider AI 厂商
     * @return 该厂商的性能指标 Flow
     */
    fun getProviderPerformance(provider: AIProvider): Flow<ProviderPerformance> =
        _updateTrigger.asStateFlow().map {
            calculatePerformance(provider)
        }

    /**
     * 获取所有 Provider 的性能指标（Task 36.4）
     *
     * @return 所有有记录的 Provider 的性能指标列表，按质量评分降序排列
     */
    fun getAllPerformance(): Flow<List<ProviderPerformance>> =
        _updateTrigger.asStateFlow().map {
            records.map { it.provider }.toSet()
                .map { calculatePerformance(it) }
                .sortedByDescending { it.qualityScore }
        }

    /**
     * 获取指定 Provider 的最近响应时间列表（用于趋势图）
     *
     * @param provider AI 厂商
     * @param limit 返回的最大记录数
     * @return 按时间正序排列的响应时间列表
     */
    fun getResponseTimeHistory(provider: AIProvider, limit: Int = 50): List<Long> =
        records.filter { it.provider == provider }
            .takeLast(limit)
            .map { it.responseTimeMs }

    /**
     * 计算指定 Provider 的性能指标
     *
     * 质量评分算法（0-100）：
     * - 成功率权重 40%：successRate * 40
     * - 速度评分权重 30%：响应时间越短得分越高（线性插值）
     * - Token 效率权重 30%：输出 Token 占比越高（有效输出越多）得分越高
     */
    private fun calculatePerformance(provider: AIProvider): ProviderPerformance {
        val providerRecords = records.filter { it.provider == provider }

        val totalRequests = providerRecords.size
        val successCount = providerRecords.count { it.success }
        val failCount = totalRequests - successCount

        // 平均响应时间（仅统计成功请求，避免超时失败拉高均值）
        val successRecords = providerRecords.filter { it.success }
        val avgResponseTime = if (successRecords.isNotEmpty()) {
            successRecords.sumOf { it.responseTimeMs } / successRecords.size
        } else {
            0L
        }

        val successRate = if (totalRequests > 0) {
            successCount.toFloat() / totalRequests.toFloat()
        } else {
            0f
        }

        // 质量评分计算
        val qualityScore = calculateQualityScore(
            successRate = successRate,
            avgResponseTime = avgResponseTime,
            records = successRecords,
            hasData = totalRequests > 0
        )

        return ProviderPerformance(
            provider = provider,
            totalRequests = totalRequests,
            successCount = successCount,
            failCount = failCount,
            avgResponseTime = avgResponseTime,
            successRate = successRate,
            qualityScore = qualityScore
        )
    }

    /**
     * 计算质量评分（0-100）
     *
     * - 成功率维度（40 分）：successRate * 40
     * - 速度维度（30 分）：响应时间在 [FAST_RESPONSE_MS] 以内满分，超过 [SLOW_RESPONSE_MS] 为 0 分
     * - Token 效率维度（30 分）：输出 Token 占总 Token 比例越高得分越高（有效输出越多）
     */
    private fun calculateQualityScore(
        successRate: Float,
        avgResponseTime: Long,
        records: List<RequestRecord>,
        hasData: Boolean
    ): Int {
        if (!hasData) return 0

        // 1. 成功率评分（满分 40）
        val successScore = (successRate * 40).toInt()

        // 2. 速度评分（满分 30）
        val speedScore = if (avgResponseTime <= 0) {
            0
        } else if (avgResponseTime <= FAST_RESPONSE_MS) {
            30
        } else if (avgResponseTime >= SLOW_RESPONSE_MS) {
            0
        } else {
            // 线性插值：FAST(30) -> SLOW(0)
            val ratio = (SLOW_RESPONSE_MS - avgResponseTime).toFloat() /
                    (SLOW_RESPONSE_MS - FAST_RESPONSE_MS).toFloat()
            (ratio * 30).toInt()
        }

        // 3. Token 效率评分（满分 30）
        val tokenScore = if (records.isEmpty()) {
            0
        } else {
            val totalInput = records.sumOf { it.tokensInput }.coerceAtLeast(1)
            val totalOutput = records.sumOf { it.tokensOutput }
            // 输出 Token 占比越高，效率越好（上限 30 分）
            val outputRatio = (totalOutput.toFloat() / totalInput.toFloat()).coerceAtMost(1f)
            (outputRatio * 30).toInt()
        }

        return (successScore + speedScore + tokenScore).coerceIn(0, 100)
    }

    /** 清空所有记录 */
    fun clear() {
        records.clear()
        _updateTrigger.value = System.currentTimeMillis()
    }
}
