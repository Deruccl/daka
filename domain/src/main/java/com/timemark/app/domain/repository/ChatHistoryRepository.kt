package com.timemark.app.domain.repository

import com.timemark.app.domain.model.ChatHistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * AI 对话历史仓库接口（Task 33.2）
 *
 * 负责本地对话历史的读写：
 * - 查询全部历史（按时间倒序，用于历史列表展示）
 * - 查询全部历史（按时间正序，用于还原对话上下文）
 * - 按 provider 查询
 * - 插入新消息
 * - 删除单条
 * - 清空全部
 */
interface ChatHistoryRepository {
    /** 获取全部对话历史（按时间倒序） */
    fun getAllHistory(): Flow<List<ChatHistoryEntry>>

    /** 获取全部对话历史（按时间正序，用于还原上下文） */
    fun getAllHistoryAsc(): Flow<List<ChatHistoryEntry>>

    /** 按 provider 查询对话历史（按时间倒序） */
    fun getHistoryByProvider(provider: String): Flow<List<ChatHistoryEntry>>

    /** 插入一条对话消息，返回自增 ID */
    suspend fun insert(entry: ChatHistoryEntry): Long

    /** 按 ID 删除单条对话 */
    suspend fun deleteById(id: Long)

    /** 清空全部对话历史 */
    suspend fun clearAll()
}
