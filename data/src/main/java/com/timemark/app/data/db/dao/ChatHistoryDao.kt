package com.timemark.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timemark.app.data.db.entity.ChatHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * AI 对话历史数据访问对象（Task 33.2）
 *
 * 提供：
 * - 插入单条消息
 * - 查询全部历史（按时间倒序）
 * - 按 provider 查询
 * - 删除单条
 * - 清空全部
 */
@Dao
interface ChatHistoryDao {

    /** 插入一条对话消息，返回自增 ID */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ChatHistoryEntity): Long

    /** 获取全部对话历史（按时间倒序） */
    @Query("SELECT * FROM chat_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ChatHistoryEntity>>

    /** 获取全部对话历史（按时间正序，用于还原对话上下文） */
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllHistoryAsc(): Flow<List<ChatHistoryEntity>>

    /** 按 provider 查询对话历史（按时间倒序） */
    @Query("SELECT * FROM chat_history WHERE provider = :provider ORDER BY timestamp DESC")
    fun getHistoryByProvider(provider: String): Flow<List<ChatHistoryEntity>>

    /** 按 ID 删除单条对话 */
    @Query("DELETE FROM chat_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** 清空全部对话历史 */
    @Query("DELETE FROM chat_history")
    suspend fun clearAll()

    /** 清空指定 provider 的对话历史 */
    @Query("DELETE FROM chat_history WHERE provider = :provider")
    suspend fun clearByProvider(provider: String)
}
