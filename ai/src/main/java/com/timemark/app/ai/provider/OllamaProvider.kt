package com.timemark.app.ai.provider

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/**
 * 本地 Ollama Provider
 *
 * 使用 Ollama 提供的 OpenAI 兼容接口（/v1/chat/completions）。
 * 默认 baseUrl: http://localhost:11434
 * 鉴权方式：无需 API Key（本地部署）
 */
class OllamaProvider(
    okHttpClient: OkHttpClient,
    json: Json
) : BaseProvider(okHttpClient, json) {

    override val providerType: AIProvider = AIProvider.OLLAMA

    private val defaultBaseUrl = "http://localhost:11434"

    override suspend fun chat(request: ChatRequest, config: AIConfig): ChatResponse {
        val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
        val url = "$baseUrl/v1/chat/completions"
        val body = buildOpenAIRequestBody(request)
        val (code, raw) = postJson(url, "", body)
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
        val (code, raw) = postJson(url, "", body)
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
            val (code, _) = postJson(url, "", body)
            code in 200..299
        }.getOrDefault(false)
    }
}
