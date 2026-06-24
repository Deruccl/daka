package com.timemark.app.data.repository

import com.timemark.app.data.db.dao.ChatHistoryDao
import com.timemark.app.data.db.entity.ChatHistoryEntity
import com.timemark.app.domain.model.ChatHistoryEntry
import com.timemark.app.domain.repository.ChatHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * ChatHistoryRepository 接口实现（Task 33.2）
 *
 * 通过 ChatHistoryDao 访问数据库，完成 Entity/Domain 模型互转。
 */
class ChatHistoryRepositoryImpl @Inject constructor(
    private val chatHistoryDao: ChatHistoryDao
) : ChatHistoryRepository {

    override fun getAllHistory(): Flow<List<ChatHistoryEntry>> =
        chatHistoryDao.getAllHistory().map { entities -> entities.map { it.toDomain() } }

    override fun getAllHistoryAsc(): Flow<List<ChatHistoryEntry>> =
        chatHistoryDao.getAllHistoryAsc().map { entities -> entities.map { it.toDomain() } }

    override fun getHistoryByProvider(provider: String): Flow<List<ChatHistoryEntry>> =
        chatHistoryDao.getHistoryByProvider(provider).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(entry: ChatHistoryEntry): Long =
        chatHistoryDao.insert(entry.toEntity())

    override suspend fun deleteById(id: Long) =
        chatHistoryDao.deleteById(id)

    override suspend fun clearAll() =
        chatHistoryDao.clearAll()
}

/** Entity -> Domain */
private fun ChatHistoryEntity.toDomain(): ChatHistoryEntry = ChatHistoryEntry(
    id = id,
    provider = provider,
    role = role,
    content = content,
    timestamp = timestamp,
    tokenCount = tokenCount
)

/** Domain -> Entity */
private fun ChatHistoryEntry.toEntity(): ChatHistoryEntity = ChatHistoryEntity(
    id = id,
    provider = provider,
    role = role,
    content = content,
    timestamp = timestamp,
    tokenCount = tokenCount
)
