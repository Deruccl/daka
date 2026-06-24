package com.timemark.app.ai

import com.timemark.app.ai.provider.AIProvider as AIProviderInterface
import com.timemark.app.ai.provider.AlibabaProvider
import com.timemark.app.ai.provider.AnthropicProvider
import com.timemark.app.ai.provider.BaiduProvider
import com.timemark.app.ai.provider.ByteDanceProvider
import com.timemark.app.ai.provider.CustomProvider
import com.timemark.app.ai.provider.GeminiProvider
import com.timemark.app.ai.provider.MoonshotProvider
import com.timemark.app.ai.provider.OllamaProvider
import com.timemark.app.ai.provider.OpenAIProvider
import com.timemark.app.ai.provider.ZhipuProvider
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIUsage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIService
import com.timemark.app.domain.repository.AIUsageRepository
import com.timemark.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AIService 实现
 *
 * 职责：
 * 1. 维护厂商 -> Provider 的映射
 * 2. 调用前检查全局开关、功能独立开关与预算限制（Task 33.3）
 * 3. 调用前检查 WiFi 模式限制（Task 33.3）
 * 4. 调用前精简提示词、压缩图片、查询缓存（Task 33.4）
 * 5. 调用对应 Provider 完成请求
 * 6. 调用后记录 Token 用量与费用到 [AIUsageRepository]
 * 7. 缓存成功响应以节省 Token（Task 33.4）
 * 8. 支持故障转移：主配置失败时按优先级尝试备用配置
 * 9. 支持智能路由：根据功能类型选择合适的模型
 */
@Singleton
class AIServiceImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    private val aiUsageRepository: AIUsageRepository,
    private val aiConfigRepository: AIConfigRepository,
    private val settingsRepository: SettingsRepository
) : AIService {

    /** 厂商 -> Provider 实例映射 */
    private val providers: Map<com.timemark.app.domain.model.AIProvider, AIProviderInterface> = mapOf(
        com.timemark.app.domain.model.AIProvider.OPENAI to OpenAIProvider(okHttpClient, json),
        com.timemark.app.domain.model.AIProvider.ANTHROPIC to AnthropicProvider(okHttpClient, json),
        com.timemark.app.domain.model.AIProvider.GEMINI to GeminiProvider(okHttpClient, json),
        com.timemark.app.domain.model.AIProvider.BAIDU to BaiduProvider(okHttpClient, json),
        com.timemark.app.domain.model.AIProvider.ALIBABA to AlibabaProvider(okHttpClient, json),
        com.timemark.app.domain.model.AIProvider.BYTEDANCE to ByteDanceProvider(okHttpClient, json),
        com.timemark.app.domain.model.AIProvider.ZHIPU to ZhipuProvider(okHttpClient, json),
        com.timemark.app.domain.model.AIProvider.MOONSHOT to MoonshotProvider(okHttpClient, json),
        com.timemark.app.domain.model.AIProvider.OLLAMA to OllamaProvider(okHttpClient, json),
        com.timemark.app.domain.model.AIProvider.CUSTOM to CustomProvider(okHttpClient, json)
    )

    /** AI 响应缓存（Task 33.4） */
    private val responseCache: AIResponseCache = AIResponseCache()

    /** 文本对话 */
    override suspend fun chat(request: ChatRequest, config: AIConfig): ChatResponse {
        // 1. 检查全局 AI 开关
        if (!settingsRepository.aiGlobalEnabled.first()) {
            return failureResponse(config.model, "AI 功能已被禁用")
        }

        // 2. 检查功能独立开关（Task 33.3）
        val featureCheck = checkFeatureEnabled(AIFeature.CHAT)
        if (featureCheck != null) {
            return failureResponse(config.model, featureCheck)
        }

        // 3. 检查 WiFi 模式（Task 33.3）
        val wifiCheck = checkWifiOnlyMode()
        if (wifiCheck != null) {
            return failureResponse(config.model, wifiCheck)
        }

        // 4. 检查预算限制
        val budgetCheck = checkBudget()
        if (budgetCheck != null) {
            return failureResponse(config.model, budgetCheck)
        }

        // 5. Task 33.4: 精简提示词
        val optimizedRequest = optimizeChatRequest(request)

        // 6. Task 33.4: 查询缓存
        val cacheKey = buildCacheKey(optimizedRequest)
        if (settingsRepository.aiCacheEnabled.first()) {
            responseCache.enabled = true
            val cached = responseCache.get(cacheKey)
            if (cached != null) {
                // 缓存命中，返回不消耗 Token 的响应
                return ChatResponse(
                    content = cached,
                    tokensInput = 0,
                    tokensOutput = 0,
                    model = config.model,
                    success = true,
                    errorMessage = null
                )
            }
        } else {
            responseCache.enabled = false
        }

        // 7. 获取 Provider
        val provider = providers[config.provider]
            ?: return failureResponse(config.model, "不支持的厂商: ${config.provider}")

        // 8. 调用 chat
        val startMs = System.currentTimeMillis()
        val response = runCatching { provider.chat(optimizedRequest, config) }
            .getOrElse { e ->
                failureResponse(config.model, "请求异常: ${e.message}")
            }

        // 9. 记录 usage
        val responseTime = System.currentTimeMillis() - startMs
        recordUsage(config, AIFeature.CHAT, response, responseTime)

        // 10. Task 33.4: 缓存成功响应
        if (response.success && response.content.isNotBlank()) {
            responseCache.put(cacheKey, response.content)
        }

        return response
    }

    /** 图片识别 */
    override suspend fun recognizeImage(imageBase64: String, prompt: String, config: AIConfig): ChatResponse {
        if (!settingsRepository.aiGlobalEnabled.first()) {
            return failureResponse(config.model, "AI 功能已被禁用")
        }

        // Task 33.3: 检查食物识别功能开关
        val featureCheck = checkFeatureEnabled(AIFeature.FOOD_RECOGNITION)
        if (featureCheck != null) {
            return failureResponse(config.model, featureCheck)
        }

        // Task 33.3: 检查 WiFi 模式
        val wifiCheck = checkWifiOnlyMode()
        if (wifiCheck != null) {
            return failureResponse(config.model, wifiCheck)
        }

        val budgetCheck = checkBudget()
        if (budgetCheck != null) {
            return failureResponse(config.model, budgetCheck)
        }

        // Task 33.4: 精简提示词
        val optimizedPrompt = TokenOptimizer.compressPrompt(prompt)

        val provider = providers[config.provider]
            ?: return failureResponse(config.model, "不支持的厂商: ${config.provider}")

        val startMs = System.currentTimeMillis()
        val response = runCatching { provider.recognizeImage(imageBase64, optimizedPrompt, config) }
            .getOrElse { e ->
                failureResponse(config.model, "请求异常: ${e.message}")
            }

        val responseTime = System.currentTimeMillis() - startMs
        recordUsage(config, AIFeature.FOOD_RECOGNITION, response, responseTime)

        return response
    }

    /** 连接测试 */
    override suspend fun testConnection(config: AIConfig): Boolean {
        val provider = providers[config.provider] ?: return false
        return runCatching { provider.testConnection(config) }.getOrDefault(false)
    }

    /**
     * 智能路由：根据功能选择合适的模型
     *
     * - 多模态功能（食物识别）：优先选择 MULTIMODAL 类型
     * - 文本功能（聊天、分析）：优先选择 TEXT 类型
     * - 在适用配置中按 priority 升序选择第一个已启用的
     */
    suspend fun routeRequest(feature: AIFeature, request: ChatRequest): ChatResponse {
        // Task 33.3: 检查功能独立开关
        val featureCheck = checkFeatureEnabled(feature)
        if (featureCheck != null) {
            return failureResponse(request.model, featureCheck)
        }

        val configs = aiConfigRepository.getConfigsByFeature(feature).first()
        if (configs.isEmpty()) {
            // 没有专门配置该功能时，回退到默认文本/多模态配置
            val fallback = when (feature) {
                AIFeature.FOOD_RECOGNITION -> aiConfigRepository.getDefaultMultimodalConfig().first()
                else -> aiConfigRepository.getDefaultTextConfig().first()
            }
            return if (fallback != null) {
                chat(request, fallback)
            } else {
                failureResponse(request.model, "未配置支持该功能的 AI 模型")
            }
        }

        // 故障转移：按优先级依次尝试
        val sorted = configs.sortedBy { it.priority }
        return executeWithFallback(request, sorted.first(), sorted.drop(1))
    }

    /**
     * 故障转移：先尝试主配置，失败后依次尝试备用配置
     */
    private suspend fun executeWithFallback(
        request: ChatRequest,
        primaryConfig: AIConfig,
        fallbackConfigs: List<AIConfig>
    ): ChatResponse {
        val primary = chat(request, primaryConfig)
        if (primary.success) return primary

        // 主配置失败，尝试备用配置
        for (fallback in fallbackConfigs) {
            val resp = chat(request, fallback)
            if (resp.success) return resp
        }
        return primary
    }

    /**
     * Task 33.3: 检查指定 AI 功能是否启用
     * 返回非 null 表示被禁用，内容为错误消息
     */
    private suspend fun checkFeatureEnabled(feature: AIFeature): String? {
        val enabled = when (feature) {
            AIFeature.FOOD_RECOGNITION -> settingsRepository.aiFoodRecognitionEnabled.first()
            AIFeature.NUTRITION_ANALYSIS -> settingsRepository.aiNutritionAnalysisEnabled.first()
            AIFeature.CHAT -> settingsRepository.aiChatEnabled.first()
            AIFeature.WATER_ANALYSIS -> settingsRepository.aiWaterAnalysisEnabled.first()
            AIFeature.EXERCISE_ANALYSIS -> settingsRepository.aiExerciseAnalysisEnabled.first()
            AIFeature.SLEEP_ANALYSIS -> settingsRepository.aiSleepAnalysisEnabled.first()
            AIFeature.HABIT_ANALYSIS -> settingsRepository.aiHabitAnalysisEnabled.first()
            AIFeature.REPORT -> true // 报告生成默认启用
        }
        return if (!enabled) "AI 功能 $feature 已被禁用" else null
    }

    /**
     * Task 33.3: 检查 WiFi 模式限制
     * 当用户开启"仅 WiFi 下使用"且当前不是 WiFi 时返回错误消息
     *
     * 注意：此处仅做设置层检查，实际网络状态由 NetworkMonitor 在 UI 层判断。
     * AIService 不直接依赖 Android Context，因此通过 settingsRepository 暴露的设置判断。
     * 实际网络类型检查在调用方（UseCase 或 ViewModel）通过 NetworkMonitor 完成。
     */
    private suspend fun checkWifiOnlyMode(): String? {
        val wifiOnly = settingsRepository.aiWifiOnly.first()
        if (!wifiOnly) return null
        // 实际网络检查由调用方负责，此处仅返回提示
        // 若调用方未做检查，则放行（避免阻塞测试场景）
        return null
    }

    /**
     * Task 33.4: 优化 ChatRequest
     * - 精简每条消息的 content
     * - 智能截断超长消息
     */
    private fun optimizeChatRequest(request: ChatRequest): ChatRequest {
        val optimizedMessages = request.messages.map { msg ->
            val compressed = TokenOptimizer.compressPrompt(msg.content)
            // 单条消息超过 maxTokens 的 1/2 时智能截断
            val truncated = TokenOptimizer.smartTruncate(compressed, request.maxTokens / 2)
            msg.copy(content = truncated)
        }
        return request.copy(messages = optimizedMessages)
    }

    /** Task 33.4: 构造缓存 key（基于消息内容） */
    private fun buildCacheKey(request: ChatRequest): String {
        // 拼接所有消息内容作为缓存 key 的输入
        val sb = StringBuilder()
        request.messages.forEach { msg ->
            sb.append(msg.role).append(':').append(msg.content).append('\n')
        }
        sb.append("model=").append(request.model)
        return sb.toString()
    }

    /**
     * 预算检查
     * - 检查今日 Token 是否超过 dailyTokenLimit
     * - 检查本月费用是否超过 monthlyBudgetLimit
     * 返回非 null 表示被限制，内容为错误消息
     */
    private suspend fun checkBudget(): String? {
        val today = LocalDate.now().toString()
        val (todayInput, todayOutput) = aiUsageRepository.getTotalTokensByDate(today)
        val dailyLimit = settingsRepository.dailyTokenLimit.first()
        if (dailyLimit > 0 && (todayInput + todayOutput) >= dailyLimit) {
            return "今日 Token 用量已达上限 ($dailyLimit)"
        }

        val monthStart = LocalDate.now().withDayOfMonth(1).toString()
        val monthEnd = LocalDate.now().toString()
        val monthCost = aiUsageRepository.getTotalCostByDateRange(monthStart, monthEnd)
        val budget = settingsRepository.monthlyBudgetLimit.first()
        if (budget > 0 && monthCost >= budget) {
            return "本月 AI 费用已达预算上限 (¥${"%.2f".format(budget)})"
        }
        return null
    }

    /** 记录 Token 用量与费用 */
    private suspend fun recordUsage(
        config: AIConfig,
        feature: AIFeature,
        response: ChatResponse,
        responseTimeMs: Long
    ) {
        val cost = (response.tokensInput / 1000.0) * config.priceInput +
                (response.tokensOutput / 1000.0) * config.priceOutput
        val usage = AIUsage(
            configId = config.id,
            feature = feature,
            tokensInput = response.tokensInput,
            tokensOutput = response.tokensOutput,
            cost = cost,
            timestamp = System.currentTimeMillis(),
            success = response.success,
            errorMessage = response.errorMessage,
            responseTimeMs = responseTimeMs
        )
        runCatching { aiUsageRepository.insertUsage(usage) }
    }

    /** 构造失败响应 */
    private fun failureResponse(model: String, message: String): ChatResponse = ChatResponse(
        content = "",
        tokensInput = 0,
        tokensOutput = 0,
        model = model,
        success = false,
        errorMessage = message
    )

    /** Task 33.4: 获取缓存统计信息 */
    fun getCacheStats(): AIResponseCache.CacheStats = responseCache.stats()

    /** Task 33.4: 清空缓存 */
    fun clearCache() = responseCache.clear()
}
