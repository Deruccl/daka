package com.timemark.app.ai.provider

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/**
 * 阿里通义千问 Provider
 *
 * 简化实现：采用 OpenAI 兼容格式（DashScope 已提供兼容接口）。
 * 默认 baseUrl: https://dashscope.aliyuncs.com/compatible-mode
 */
class AlibabaProvider(
    okHttpClient: OkHttpClient,
    json: Json
) : BaseProvider(okHttpClient, json) {

    override val providerType: AIProvider = AIProvider.ALIBABA

    private val defaultBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode"

    override suspend fun chat(request: ChatRequest, config: AIConfig): ChatResponse {
        val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
        val url = "$baseUrl/v1/chat/completions"
        val body = buildOpenAIRequestBody(request)
        val (code, raw) = postJson(url, config.apiKey, body)
        return if (code in 200..299) {
            parseOpenAIResponse(raw, request.model)
        } else {
            parseOpenAIResponse(raw, request.model, "HTTP $code: ${raw.take(200)}")
        }
    }

    override suspend fun recognizeImage(imageBase64: String, prompt: String, config: AIConfig): ChatResponse {
        val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
        val url = "$baseUrl/v1/chat/completions"
        val request = ChatRequest(
            messages = listOf(com.timemark.app.domain.model.ChatMessage(role = "user", content = prompt)),
            model = config.model,
            temperature = 0.2,
            maxTokens = config.maxTokens
        )
        val body = buildOpenAIRequestBody(request, imageBase64 = imageBase64, imagePrompt = prompt)
        val (code, raw) = postJson(url, config.apiKey, body)
        return if (code in 200..299) {
            parseOpenAIResponse(raw, config.model)
        } else {
            parseOpenAIResponse(raw, config.model, "HTTP $code: ${raw.take(200)}")
        }
    }

    override suspend fun testConnection(config: AIConfig): Boolean {
        return runCatching {
            val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
            val url = "$baseUrl/v1/chat/completions"
            val request = ChatRequest(
                messages = listOf(com.timemark.app.domain.model.ChatMessage(role = "user", content = "ping")),
                model = config.model,
                maxTokens = 5
            )
            val body = buildOpenAIRequestBody(request)
            val (code, _) = postJson(url, config.apiKey, body)
            code in 200..299
        }.getOrDefault(false)
    }
}
