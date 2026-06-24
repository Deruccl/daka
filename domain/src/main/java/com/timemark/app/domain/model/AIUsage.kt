package com.timemark.app.domain.model

import kotlinx.serialization.Serializable

/** AI 使用记录 */
@Serializable
data class AIUsage(
    val id: Long = 0,
    val configId: Long,
    val feature: AIFeature,
    val tokensInput: Int,
    val tokensOutput: Int,
    val cost: Double,
    val timestamp: Long,
    val success: Boolean,
    val errorMessage: String? = null,
    val responseTimeMs: Long = 0    // 响应时间
)
