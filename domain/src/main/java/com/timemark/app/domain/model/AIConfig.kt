package com.timemark.app.domain.model

import kotlinx.serialization.Serializable

/** AI 模型配置 */
@Serializable
data class AIConfig(
    val id: Long = 0,
    val name: String,               // 配置名称
    val provider: AIProvider,
    val apiKey: String,             // 加密后的 API Key
    val baseUrl: String = "",       // API 地址
    val model: String,              // 模型名称
    val modelType: AIModelType,
    val priceInput: Double = 0.0,   // 每 1K Token 输入价格
    val priceOutput: Double = 0.0,  // 每 1K Token 输出价格
    val rateLimitPerMinute: Int = 0, // 速率限制
    val maxTokens: Int = 4096,
    val enabled: Boolean = true,
    val priority: Int = 0,          // 优先级（数字越小越优先）
    val applicableFeatures: List<AIFeature> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
