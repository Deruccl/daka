package com.timemark.app.ai.provider

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/**
 * Anthropic Claude 系列 Provider
 *
 * 使用 Anthropic Messages API（/v1/messages），
 * 支持文本对话与图片识别（Claude 3 系列多模态）。
 *
 * 默认 baseUrl: https://api.anthropic.com
 * 鉴权方式：header("x-api-key", apiKey) + anthropic-version
 */
class AnthropicProvider(
    okHttpClient: OkHttpClient,
    json: Json
) : BaseProvider(okHttpClient, json) {

    override val providerType: AIProvider = AIProvider.ANTHROPIC

    private val defaultBaseUrl = "https://api.anthropic.com"

    override suspend fun chat(request: ChatRequest, config: AIConfig): ChatResponse {
        val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
        val url = "$baseUrl/v1/messages"
        val body = buildAnthropicRequestBody(request)
        val (code, raw) = postJson(
            url = url,
            apiKey = "",
            body = body,
            extraHeaders = mapOf(
                "x-api-key" to config.apiKey,
                "anthropic-version" to "2023-06-01"
            )
        )
        return if (code in 200..299) {
            parseAnthropicResponse(raw, request.model)
        } else {
            ChatResponse(
                content = "",
                tokensInput = 0,
                tokensOutput = 0,
                model = request.model,
                success = false,
                errorMessage = "HTTP $code: ${raw.take(200)}"
            )
        }
    }

    override suspend fun recognizeImage(imageBase64: String, prompt: String, config: AIConfig): ChatResponse {
        val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
        val url = "$baseUrl/v1/messages"
        val request = ChatRequest(
            messages = listOf(com.timemark.app.domain.model.ChatMessage(role = "user", content = prompt)),
            model = config.model,
            temperature = 0.2,
            maxTokens = config.maxTokens
        )
        val body = buildAnthropicRequestBody(request, imageBase64 = imageBase64, imagePrompt = prompt)
        val (code, raw) = postJson(
            url = url,
            apiKey = "",
            body = body,
            extraHeaders = mapOf(
                "x-api-key" to config.apiKey,
                "anthropic-version" to "2023-06-01"
            )
        )
        return if (code in 200..299) {
            parseAnthropicResponse(raw, config.model)
        } else {
            ChatResponse(
                content = "",
                tokensInput = 0,
                tokensOutput = 0,
                model = config.model,
                success = false,
                errorMessage = "HTTP $code: ${raw.take(200)}"
            )
        }
    }

    override suspend fun testConnection(config: AIConfig): Boolean {
        return runCatching {
            val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
            val url = "$baseUrl/v1/messages"
            val request = ChatRequest(
                messages = listOf(com.timemark.app.domain.model.ChatMessage(role = "user", content = "ping")),
                model = config.model,
                maxTokens = 5
            )
            val body = buildAnthropicRequestBody(request)
            val (code, _) = postJson(
                url = url,
                apiKey = "",
                body = body,
                extraHeaders = mapOf(
                    "x-api-key" to config.apiKey,
                    "anthropic-version" to "2023-06-01"
                )
            )
            code in 200..299
        }.getOrDefault(false)
    }
}
