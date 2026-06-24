package com.timemark.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.timemark.app.data.db.entity.AIUsageEntity
import kotlinx.coroutines.flow.Flow

/**
 * AI 使用记录数据访问对象。
 */
@Dao
interface AIUsageDao {
    @Query("SELECT * FROM ai_usage ORDER BY timestamp DESC")
    fun getAllUsage(): Flow<List<AIUsageEntity>>

    @Query("SELECT * FROM ai_usage WHERE timestamp BETWEEN :startTs AND :endTs ORDER BY timestamp DESC")
    fun getUsageByDateRange(startTs: Long, endTs: Long): Flow<List<AIUsageEntity>>

    @Query("SELECT * FROM ai_usage WHERE config_id = :configId ORDER BY timestamp DESC")
    fun getUsageByConfig(configId: Long): Flow<List<AIUsageEntity>>

    @Query("SELECT * FROM ai_usage WHERE feature = :feature ORDER BY timestamp DESC")
    fun getUsageByFeature(feature: String): Flow<List<AIUsageEntity>>

    @Query("SELECT * FROM ai_usage WHERE timestamp >= :startTs ORDER BY timestamp DESC")
    fun getUsageSince(startTs: Long): Flow<List<AIUsageEntity>>

    @Insert
    suspend fun insert(usage: AIUsageEntity): Long

    @Query("SELECT SUM(tokensInput) FROM ai_usage WHERE timestamp BETWEEN :startTs AND :endTs")
    suspend fun getTotalInputTokens(startTs: Long, endTs: Long): Int?

    @Query("SELECT SUM(tokensOutput) FROM ai_usage WHERE timestamp BETWEEN :startTs AND :endTs")
    suspend fun getTotalOutputTokens(startTs: Long, endTs: Long): Int?

    @Query("SELECT SUM(cost) FROM ai_usage WHERE timestamp BETWEEN :startTs AND :endTs")
    suspend fun getTotalCost(startTs: Long, endTs: Long): Double?

    @Query("DELETE FROM ai_usage WHERE timestamp < :beforeTs")
    suspend fun deleteOldUsage(beforeTs: Long)
}
