package com.timemark.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * AI 对话历史实体表（Task 33.2）
 *
 * 用于本地持久化 AI 对话消息，支持按 provider 过滤、按时间倒序查询。
 * 每条记录对应一条消息（user 或 assistant）。
 */
@Entity(
    tableName = "chat_history",
    indices = [
        Index("timestamp"),
        Index("provider"),
        Index("role")
    ]
)
data class ChatHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** AI 配置厂商名称（AIProvider.name） */
    @ColumnInfo(name = "provider") val provider: String,
    /** 消息角色：user / assistant / system */
    @ColumnInfo(name = "role") val role: String,
    /** 消息内容 */
    @ColumnInfo(name = "content") val content: String,
    /** 创建时间戳（毫秒） */
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    /** 该条消息消耗的 Token 数（assistant 消息记录输出 Token，user 消息记录输入 Token） */
    @ColumnInfo(name = "tokenCount") val tokenCount: Int = 0
)
