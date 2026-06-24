package com.timemark.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.timemark.app.data.db.entity.AIConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * AI 配置数据访问对象。
 */
@Dao
interface AIConfigDao {
    @Query("SELECT * FROM ai_configs ORDER BY priority ASC")
    fun getAllConfigs(): Flow<List<AIConfigEntity>>

    @Query("SELECT * FROM ai_configs WHERE enabled = 1 ORDER BY priority ASC")
    fun getEnabledConfigs(): Flow<List<AIConfigEntity>>

    @Query("SELECT * FROM ai_configs WHERE id = :id")
    fun getConfigById(id: Long): Flow<AIConfigEntity?>

    @Query("SELECT * FROM ai_configs WHERE enabled = 1 AND applicableFeatures LIKE '%' || :feature || '%' ORDER BY priority ASC")
    fun getConfigsByFeature(feature: String): Flow<List<AIConfigEntity>>

    @Query("SELECT * FROM ai_configs WHERE enabled = 1 AND modelType = 'MULTIMODAL' ORDER BY priority ASC LIMIT 1")
    fun getDefaultMultimodalConfig(): Flow<AIConfigEntity?>

    @Query("SELECT * FROM ai_configs WHERE enabled = 1 AND modelType = 'TEXT' ORDER BY priority ASC LIMIT 1")
    fun getDefaultTextConfig(): Flow<AIConfigEntity?>

    @Insert
    suspend fun insert(config: AIConfigEntity): Long

    @Update
    suspend fun update(config: AIConfigEntity)

    @Delete
    suspend fun delete(config: AIConfigEntity)

    @Query("DELETE FROM ai_configs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE ai_configs SET priority = :priority WHERE id = :id")
    suspend fun updatePriority(id: Long, priority: Int)
}
