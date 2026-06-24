package com.timemark.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * AI 模型配置实体表。
 * apiKey 字段存储加密后的密钥，applicableFeatures 为 JSON 数组字符串。
 * proxyConfig 为 ProxyConfig 的 JSON 字符串（Task 36.2），空字符串表示无代理。
 */
@Entity(tableName = "ai_configs", indices = [Index("priority")])
data class AIConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val provider: String,       // AIProvider.name
    val apiKey: String,         // 加密后
    val baseUrl: String,
    val model: String,
    val modelType: String,      // AIModelType.name
    val priceInput: Double,
    val priceOutput: Double,
    val rateLimitPerMinute: Int,
    val maxTokens: Int,
    val enabled: Boolean,
    val priority: Int,
    val applicableFeatures: String,  // JSON 数组
    val proxyConfig: String = "",    // Task 36.2: ProxyConfig JSON 字符串
    val createdAt: Long,
    val updatedAt: Long
)
