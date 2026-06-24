package com.timemark.app.ai.provider

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse

/**
 * AI 厂商 Provider 接口
 *
 * 每个具体厂商（OpenAI、Anthropic、Gemini 等）实现该接口，
 * 负责将统一的 [ChatRequest] 转换为对应厂商的 HTTP 请求，
 * 并将响应解析为统一的 [ChatResponse]。
 */
interface AIProvider {

    /** 厂商类型 */
    val providerType: com.timemark.app.domain.model.AIProvider

    /** 文本对话 */
    suspend fun chat(request: ChatRequest, config: AIConfig): ChatResponse

    /** 图片识别（多模态） */
    suspend fun recognizeImage(imageBase64: String, prompt: String, config: AIConfig): ChatResponse

    /** 连接测试 */
    suspend fun testConnection(config: AIConfig): Boolean
}
