package com.timemark.app.ai

import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIModelType
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 协同模式服务
 *
 * 多模态与非多模态模型协同工作：
 * 1. 多模态模型识别图片，输出结构化文字（食物清单等）
 * 2. 非多模态模型基于文字进一步分析，输出最终结果（营养建议等）
 *
 * 适用于：本地无多模态模型但需要图片分析的场景，
 * 或希望结合多模态识别能力与文本模型推理能力的场景。
 */
@Singleton
class CollaborativeService @Inject constructor(
    private val aiService: AIService,
    private val aiConfigRepository: AIConfigRepository
) {

    /**
     * 识别并分析
     *
     * @param imageBase64 图片 base64 编码
     * @param recognizePrompt 识别阶段提示词（如"识别图中所有食物"）
     * @param analyzePrompt 分析阶段提示词（如"基于以下食物信息分析营养"）
     * @return 最终分析结果
     */
    suspend fun recognizeAndAnalyze(
        imageBase64: String,
        recognizePrompt: String,
        analyzePrompt: String
    ): ChatResponse {
        // 1. 获取多模态模型（优先 FOOD_RECOGNITION 功能配置，回退到默认多模态）
        val multimodalConfig = aiConfigRepository.getConfigsByFeature(AIFeature.FOOD_RECOGNITION)
            .first()
            .firstOrNull { it.modelType == AIModelType.MULTIMODAL && it.enabled }
            ?: aiConfigRepository.getDefaultMultimodalConfig().first()
            ?: return ChatResponse(
                content = "",
                tokensInput = 0,
                tokensOutput = 0,
                model = "",
                success = false,
                errorMessage = "未配置多模态模型，无法识别图片"
            )

        // 2. 多模态模型识别图片
        val recognizeResponse = aiService.recognizeImage(imageBase64, recognizePrompt, multimodalConfig)
        if (!recognizeResponse.success) {
            return recognizeResponse
        }

        // 3. 获取非多模态模型（优先 NUTRITION_ANALYSIS 功能配置，回退到默认文本）
        val textConfig = aiConfigRepository.getConfigsByFeature(AIFeature.NUTRITION_ANALYSIS)
            .first()
            .firstOrNull { it.modelType == AIModelType.TEXT && it.enabled }
            ?: aiConfigRepository.getDefaultTextConfig().first()
            ?: return recognizeResponse // 没有文本模型时直接返回识别结果

        // 4. 文本模型分析识别结果
        val analyzeRequest = ChatRequest(
            messages = listOf(
                ChatMessage(role = "system", content = analyzePrompt),
                ChatMessage(role = "user", content = recognizeResponse.content)
            ),
            model = textConfig.model,
            temperature = 0.3,
            maxTokens = textConfig.maxTokens
        )

        // 5. 调用文本模型分析
        val analyzeResponse = aiService.chat(analyzeRequest, textConfig)

        // 合并两次响应的 Token 用量
        return if (analyzeResponse.success) {
            analyzeResponse.copy(
                tokensInput = analyzeResponse.tokensInput + recognizeResponse.tokensInput,
                tokensOutput = analyzeResponse.tokensOutput + recognizeResponse.tokensOutput
            )
        } else {
            // 分析失败时返回识别结果（已包含识别信息）
            recognizeResponse.copy(
                errorMessage = "识别成功但分析失败: ${analyzeResponse.errorMessage}"
            )
        }
    }
}
