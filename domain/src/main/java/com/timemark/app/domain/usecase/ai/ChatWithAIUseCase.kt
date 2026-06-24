package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.Result
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIService
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * AI 聊天用例
 *
 * 与 AI 进行多轮对话。
 * 接收消息列表（role/content），调用文本模型返回响应。
 *
 * 注意：调用方负责维护对话历史，本用例仅负责单次请求。
 */
class ChatWithAIUseCase @Inject constructor(
    private val aiService: AIService,
    private val aiConfigRepository: AIConfigRepository
) {

    /** 与 AI 聊天，messages 为用户输入的消息列表（按时间顺序） */
    suspend operator fun invoke(messages: List<String>): Result<ChatResponse> {
        if (messages.isEmpty()) {
            return Result.Error("消息不能为空")
        }

        // 获取文本模型配置
        val config = aiConfigRepository.getConfigsByFeature(AIFeature.CHAT)
            .first()
            .firstOrNull { it.enabled }
            ?: aiConfigRepository.getDefaultTextConfig().first()
            ?: return Result.Error("未配置文本 AI 模型，无法聊天")

        // 构造消息列表
        val chatMessages = buildList {
            add(ChatMessage(role = "system", content = "你是 TimeMark 应用的智能助手，可以帮助用户管理打卡习惯、分析数据、提供建议。请用简洁友好的中文回复。"))
            messages.forEach { msg -> add(ChatMessage(role = "user", content = msg)) }
        }

        val request = ChatRequest(
            messages = chatMessages,
            model = config.model,
            temperature = 0.7,
            maxTokens = config.maxTokens
        )

        val response = aiService.chat(request, config)
        return if (response.success) {
            Result.Success(response)
        } else {
            Result.Error(response.errorMessage ?: "AI 聊天失败")
        }
    }

    /** 与 AI 聊天（直接传入 ChatMessage 列表，支持多轮上下文） */
    suspend fun chat(messages: List<ChatMessage>): Result<ChatResponse> {
        if (messages.isEmpty()) {
            return Result.Error("消息不能为空")
        }

        val config = aiConfigRepository.getConfigsByFeature(AIFeature.CHAT)
            .first()
            .firstOrNull { it.enabled }
            ?: aiConfigRepository.getDefaultTextConfig().first()
            ?: return Result.Error("未配置文本 AI 模型，无法聊天")

        val request = ChatRequest(
            messages = messages,
            model = config.model,
            temperature = 0.7,
            maxTokens = config.maxTokens
        )

        val response = aiService.chat(request, config)
        return if (response.success) {
            Result.Success(response)
        } else {
            Result.Error(response.errorMessage ?: "AI 聊天失败")
        }
    }
}
