package com.timemark.app.ai.provider

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/**
 * OpenAI GPT 系列 Provider
 *
 * 使用 OpenAI 官方 /v1/chat/completions 接口，
 * 支持文本对话与图片识别（GPT-4V 等多模态模型）。
 *
 * 默认 baseUrl: https://api.openai.com
 */
class OpenAIProvider(
    okHttpClient: OkHttpClient,
    json: Json
) : BaseProvider(okHttpClient, json) {

    override val providerType: AIProvider = AIProvider.OPENAI

    /** 默认 OpenAI API 地址 */
    private val defaultBaseUrl = "https://api.openai.com"

    /** 文本对话 */
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

    /** 图片识别（多模态） */
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

    /** 连接测试：发送一个最小请求，判断是否返回 200 */
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
