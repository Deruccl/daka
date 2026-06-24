package com.timemark.app.domain.repository

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIFeature
import kotlinx.coroutines.flow.Flow

/** AI 配置仓库接口 */
interface AIConfigRepository {
    fun getAllConfigs(): Flow<List<AIConfig>>
    fun getEnabledConfigs(): Flow<List<AIConfig>>
    fun getConfigById(id: Long): Flow<AIConfig?>
    fun getConfigsByFeature(feature: AIFeature): Flow<List<AIConfig>>
    fun getDefaultMultimodalConfig(): Flow<AIConfig?>
    fun getDefaultTextConfig(): Flow<AIConfig?>
    suspend fun insertConfig(config: AIConfig): Long
    suspend fun updateConfig(config: AIConfig)
    suspend fun deleteConfig(id: Long)
    suspend fun updatePriority(orders: List<Pair<Long, Int>>)
}
