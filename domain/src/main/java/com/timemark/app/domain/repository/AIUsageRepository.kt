package com.timemark.app.domain.repository

import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIUsage
import kotlinx.coroutines.flow.Flow

/** AI 使用记录仓库接口 */
interface AIUsageRepository {
    fun getAllUsage(): Flow<List<AIUsage>>
    fun getUsageByDateRange(startDate: String, endDate: String): Flow<List<AIUsage>>
    fun getUsageByConfig(configId: Long): Flow<List<AIUsage>>
    fun getUsageByFeature(feature: AIFeature): Flow<List<AIUsage>>
    fun getTodayUsage(): Flow<List<AIUsage>>
    fun getWeekUsage(): Flow<List<AIUsage>>
    fun getMonthUsage(): Flow<List<AIUsage>>
    suspend fun insertUsage(usage: AIUsage): Long
    suspend fun getTotalTokensByDate(date: String): Pair<Int, Int>  // input, output
    suspend fun getTotalCostByDateRange(startDate: String, endDate: String): Double
}
