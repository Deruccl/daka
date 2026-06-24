package com.timemark.app.domain.repository

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse

/**
 * AI 服务接口
 *
 * 定义 AI 能力的统一入口，由 ai 模块提供实现。
 * domain 模块仅声明接口，避免对 ai 模块产生依赖（ai 模块依赖 domain）。
 *
 * 主要能力：
 * - 文本对话
 * - 图片识别（多模态）
 * - 连接测试
 */
interface AIService {

    /** 文本对话 */
    suspend fun chat(request: ChatRequest, config: AIConfig): ChatResponse

    /** 图片识别（多模态），传入 base64 编码的图片与提示词 */
    suspend fun recognizeImage(imageBase64: String, prompt: String, config: AIConfig): ChatResponse

    /** 测试与指定配置的连通性，返回是否可用 */
    suspend fun testConnection(config: AIConfig): Boolean
}
