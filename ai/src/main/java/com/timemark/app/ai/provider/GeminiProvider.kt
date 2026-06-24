package com.timemark.app.ai.provider

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/**
 * Google Gemini 系列 Provider
 *
 * 使用 Gemini generateContent API，
 * 支持文本对话与图片识别（Gemini 1.5 / 2.0 系列多模态）。
 *
 * 默认 baseUrl: https://generativelanguage.googleapis.com
 * 鉴权方式：URL 参数 key=apiKey
 */
class GeminiProvider(
    okHttpClient: OkHttpClient,
    json: Json
) : BaseProvider(okHttpClient, json) {

    override val providerType: AIProvider = AIProvider.GEMINI

    private val defaultBaseUrl = "https://generativelanguage.googleapis.com"

    override suspend fun chat(request: ChatRequest, config: AIConfig): ChatResponse {
        val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
        val url = "$baseUrl/v1beta/models/${config.model}:generateContent?key=${config.apiKey}"
        val body = buildGeminiRequestBody(request)
        val (code, raw) = postJson(url, "", body)
        return if (code in 200..299) {
            parseGeminiResponse(raw, request.model)
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
        val url = "$baseUrl/v1beta/models/${config.model}:generateContent?key=${config.apiKey}"
        val request = ChatRequest(
            messages = listOf(com.timemark.app.domain.model.ChatMessage(role = "user", content = prompt)),
            model = config.model,
            temperature = 0.2,
            maxTokens = config.maxTokens
        )
        val body = buildGeminiRequestBody(request, imageBase64 = imageBase64, imagePrompt = prompt)
        val (code, raw) = postJson(url, "", body)
        return if (code in 200..299) {
            parseGeminiResponse(raw, config.model)
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
            val url = "$baseUrl/v1beta/models/${config.model}:generateContent?key=${config.apiKey}"
            val request = ChatRequest(
                messages = listOf(com.timemark.app.domain.model.ChatMessage(role = "user", content = "ping")),
                model = config.model,
                maxTokens = 5
            )
            val body = buildGeminiRequestBody(request)
            val (code, _) = postJson(url, "", body)
            code in 200..299
        }.getOrDefault(false)
    }
}
