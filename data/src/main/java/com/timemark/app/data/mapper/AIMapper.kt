package com.timemark.app.data.mapper

import com.timemark.app.data.db.entity.AIConfigEntity
import com.timemark.app.data.db.entity.AIUsageEntity
import com.timemark.app.data.util.JsonUtils
import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIModelType
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.AIUsage

/**
 * AIConfig 实体与 Domain 模型互转。
 * applicableFeatures 以 JSON 数组字符串存储 AIFeature.name 列表。
 */
fun AIConfigEntity.toDomain(): AIConfig = AIConfig(
    id = id,
    name = name,
    provider = runCatching { AIProvider.valueOf(provider) }.getOrDefault(AIProvider.CUSTOM),
    apiKey = apiKey,
    baseUrl = baseUrl,
    model = model,
    modelType = runCatching { AIModelType.valueOf(modelType) }.getOrDefault(AIModelType.TEXT),
    priceInput = priceInput,
    priceOutput = priceOutput,
    rateLimitPerMinute = rateLimitPerMinute,
    maxTokens = maxTokens,
    enabled = enabled,
    priority = priority,
    applicableFeatures = JsonUtils.decodeStringList(applicableFeatures).mapNotNull { name ->
        runCatching { AIFeature.valueOf(name) }.getOrNull()
    },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AIConfig.toEntity(): AIConfigEntity = AIConfigEntity(
    id = id,
    name = name,
    provider = provider.name,
    apiKey = apiKey,
    baseUrl = baseUrl,
    model = model,
    modelType = modelType.name,
    priceInput = priceInput,
    priceOutput = priceOutput,
    rateLimitPerMinute = rateLimitPerMinute,
    maxTokens = maxTokens,
    enabled = enabled,
    priority = priority,
    applicableFeatures = JsonUtils.encodeStringList(applicableFeatures.map { it.name }),
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * AIUsage 实体与 Domain 模型互转。
 */
fun AIUsageEntity.toDomain(): AIUsage = AIUsage(
    id = id,
    configId = configId,
    feature = runCatching { AIFeature.valueOf(feature) }.getOrDefault(AIFeature.CHAT),
    tokensInput = tokensInput,
    tokensOutput = tokensOutput,
    cost = cost,
    timestamp = timestamp,
    success = success,
    errorMessage = errorMessage,
    responseTimeMs = responseTimeMs
)

fun AIUsage.toEntity(): AIUsageEntity = AIUsageEntity(
    id = id,
    configId = configId,
    feature = feature.name,
    tokensInput = tokensInput,
    tokensOutput = tokensOutput,
    cost = cost,
    timestamp = timestamp,
    success = success,
    errorMessage = errorMessage,
    responseTimeMs = responseTimeMs
)
