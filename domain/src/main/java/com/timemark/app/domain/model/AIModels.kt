package com.timemark.app.domain.model

import kotlinx.serialization.Serializable

/**
 * HTTP 代理配置（Task 36.2）
 *
 * 用于为单个 AI Provider 配置网络代理，支持需要科学上网或内网穿透的场景。
 * 代理认证（username/password）为可选项，仅当代理服务器需要认证时填写。
 *
 * @param host 代理服务器地址
 * @param port 代理服务器端口
 * @param username 代理认证用户名（可选）
 * @param password 代理认证密码（可选）
 * @param enabled 是否启用代理
 */
@Serializable
data class ProxyConfig(
    val host: String = "",
    val port: Int = 0,
    val username: String? = null,
    val password: String? = null,
    val enabled: Boolean = false
)

/** 聊天消息 */
@Serializable
data class ChatMessage(
    val role: String,       // "system"/"user"/"assistant"
    val content: String,
    val images: List<String> = emptyList()  // base64 图片（多模态）
)

/** AI 聊天请求 */
@Serializable
data class ChatRequest(
    val messages: List<ChatMessage>,
    val model: String,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2048
)

/** AI 聊天响应 */
@Serializable
data class ChatResponse(
    val content: String,
    val tokensInput: Int,
    val tokensOutput: Int,
    val model: String,
    val success: Boolean,
    val errorMessage: String? = null
)

/** 食物识别结果项 */
@Serializable
data class FoodItem(
    val name: String,
    val portion: String,        // 份量描述
    val portionGrams: Double,   // 份量克数
    val calories: Double,       // 热量大卡
    val protein: Double,        // 蛋白质 g
    val carbs: Double,          // 碳水 g
    val fat: Double,            // 脂肪 g
    val fiber: Double = 0.0,    // 膳食纤维 g
    val confidence: Double = 0.0 // 置信度 0-1
)

/** 食物识别结果 */
@Serializable
data class FoodRecognitionResult(
    val items: List<FoodItem>,
    val totalCalories: Double,
    val mealType: String? = null  // 餐次
)

/** 营养分析结果 */
@Serializable
data class NutritionAnalysis(
    val calorieAnalysis: String,
    val nutrientBalance: String,
    val dietHabits: String,
    val suggestions: List<String>,
    val score: Int  // 0-100
)

/** AI 聊天历史 */
@Serializable
data class ChatHistory(
    val id: Long = 0,
    val messages: List<ChatMessage>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * AI 对话历史单条记录（Task 33.2）
 *
 * 用于本地持久化对话消息，每条记录对应一条 user/assistant 消息。
 */
@Serializable
data class ChatHistoryEntry(
    val id: Long = 0,
    val provider: String,           // AIProvider.name
    val role: String,               // user / assistant / system
    val content: String,
    val timestamp: Long,
    val tokenCount: Int = 0
)
