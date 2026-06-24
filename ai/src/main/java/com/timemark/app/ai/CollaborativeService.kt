package com.timemark.app.ai

import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIModelType
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 *
 * Task 36.3: 增加协同效果统计，记录协同模式 vs 单模型模式的 Token 消耗对比。
 */
@Singleton
class CollaborativeService @Inject constructor(
    private val aiService: AIService,
    private val aiConfigRepository: AIConfigRepository
) {

    /**
     * 协同效果统计（Task 36.3）
     *
     * @param totalRequests 协同模式总请求次数
     * @param singleModelTokens 单模型模式估算 Token 消耗（若仅用多模态模型完成全部工作）
     * @param collaborativeTokens 协同模式实际 Token 消耗（两阶段之和）
     * @param savedTokens 节省的 Token 数量（singleModelTokens - collaborativeTokens）
     * @param savedPercentage 节省比例（0-1）
     * @param providerUsage 各 Provider 在协同模式中的使用次数
     */
    data class CollaborativeStats(
        val totalRequests: Int = 0,
        val singleModelTokens: Int = 0,
        val collaborativeTokens: Int = 0,
        val savedTokens: Int = 0,
        val savedPercentage: Float = 0f,
        val providerUsage: Map<AIProvider, Int> = emptyMap()
    )

    companion object {
        /**
         * 单模型模式估算系数（Task 36.3）
         *
         * 单个多模态模型同时完成识别与分析时，由于需要处理图片并生成长文本，
         * Token 消耗通常高于协同模式。此系数用于估算单模型模式的 Token 消耗。
         * 经验值 1.3：单模型模式比协同模式多消耗约 30% Token。
         */
        private const val SINGLE_MODEL_ESTIMATE_FACTOR = 1.3f
    }

    /** 协同效果统计（内存中维护，通过 Flow 暴露） */
    private val _collaborativeStats = MutableStateFlow(CollaborativeStats())

    /** 协同效果统计 Flow（Task 36.3） */
    fun getCollaborativeStats(): Flow<CollaborativeStats> = _collaborativeStats.asStateFlow()

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
        val finalResponse = if (analyzeResponse.success) {
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

        // Task 36.3: 更新协同效果统计
        updateCollaborativeStats(
            multimodalProvider = multimodalConfig.provider,
            textProvider = textConfig.provider,
            collaborativeTokens = finalResponse.tokensInput + finalResponse.tokensOutput
        )

        return finalResponse
    }

    /**
     * Task 36.3: 更新协同效果统计
     *
     * 估算单模型模式 Token 消耗并计算节省比例。
     *
     * @param multimodalProvider 多模态模型厂商
     * @param textProvider 文本模型厂商
     * @param collaborativeTokens 协同模式实际消耗的 Token 总数
     */
    private fun updateCollaborativeStats(
        multimodalProvider: AIProvider,
        textProvider: AIProvider,
        collaborativeTokens: Int
    ) {
        val current = _collaborativeStats.value

        // 估算单模型模式 Token 消耗
        val singleModelTokens = (collaborativeTokens * SINGLE_MODEL_ESTIMATE_FACTOR).toInt()
        val savedTokens = (singleModelTokens - collaborativeTokens).coerceAtLeast(0)
        val savedPercentage = if (singleModelTokens > 0) {
            savedTokens.toFloat() / singleModelTokens.toFloat()
        } else {
            0f
        }

        // 更新各 Provider 使用次数
        val updatedProviderUsage = current.providerUsage.toMutableMap().apply {
            put(multimodalProvider, getOrElse(multimodalProvider) { 0 } + 1)
            put(textProvider, getOrElse(textProvider) { 0 } + 1)
        }

        _collaborativeStats.value = CollaborativeStats(
            totalRequests = current.totalRequests + 1,
            singleModelTokens = current.singleModelTokens + singleModelTokens,
            collaborativeTokens = current.collaborativeTokens + collaborativeTokens,
            savedTokens = current.savedTokens + savedTokens,
            savedPercentage = if (current.singleModelTokens + singleModelTokens > 0) {
                (current.savedTokens + savedTokens).toFloat() /
                        (current.singleModelTokens + singleModelTokens).toFloat()
            } else {
                0f
            },
            providerUsage = updatedProviderUsage
        )
    }

    /** Task 36.3: 重置协同效果统计 */
    fun resetStats() {
        _collaborativeStats.value = CollaborativeStats()
    }
}
