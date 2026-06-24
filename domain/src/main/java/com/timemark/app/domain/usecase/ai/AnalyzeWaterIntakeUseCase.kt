package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.Result
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.WaterIntakeAnalysis
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * 饮水习惯分析用例
 *
 * 基于用户提供的饮水记录（时间+量）调用文本 AI 模型分析：
 * - 每日平均饮水量
 * - 饮水时间分布
 * - 规律性评分
 * - 个性化建议
 */
class AnalyzeWaterIntakeUseCase @Inject constructor(
    private val aiService: AIService,
    private val aiConfigRepository: AIConfigRepository
) {

    /** 默认 JSON 解析器 */
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * 分析饮水习惯
     *
     * @param waterRecords 饮水记录列表，每条形如 "2026-06-21 08:30 250ml"
     * @param targetMl 每日目标饮水量（毫升）
     */
    suspend operator fun invoke(waterRecords: List<String>, targetMl: Double = 2000.0): Result<WaterIntakeAnalysis> {
        if (waterRecords.isEmpty()) {
            return Result.Error("没有可分析的饮水记录")
        }

        // 获取文本模型配置
        val config = aiConfigRepository.getConfigsByFeature(AIFeature.WATER_ANALYSIS)
            .first()
            .firstOrNull { it.enabled }
            ?: aiConfigRepository.getDefaultTextConfig().first()
            ?: return Result.Error("未配置文本 AI 模型，无法进行饮水分析")

        // 构造请求
        val prompt = buildAnalysisPrompt(waterRecords, targetMl)
        val request = ChatRequest(
            messages = listOf(
                ChatMessage(role = "system", content = "你是健康饮水分析专家，请基于用户的饮水记录分析饮水习惯，返回 JSON 格式结果。"),
                ChatMessage(role = "user", content = prompt)
            ),
            model = config.model,
            temperature = 0.3,
            maxTokens = config.maxTokens
        )

        // 调用 AI
        val response = aiService.chat(request, config)
        if (!response.success) {
            return Result.Error(response.errorMessage ?: "AI 分析失败")
        }

        // 解析结果
        return runCatching {
            val result = parseAnalysisResult(response.content)
            Result.Success(result.copy(targetMl = targetMl))
        }.getOrElse { e ->
            Result.Error("解析分析结果失败: ${e.message}")
        }
    }

    /** 构造分析提示词 */
    private fun buildAnalysisPrompt(records: List<String>, targetMl: Double): String = """
        请基于以下饮水记录分析用户的饮水习惯，返回 JSON 格式结果：
        {
          "dailyAverageMl": 1800.0,
          "timeDistribution": {
            "morningRatio": 0.4,
            "afternoonRatio": 0.4,
            "eveningRatio": 0.2
          },
          "regularityScore": 75,
          "suggestions": ["建议1", "建议2"]
        }
        仅返回 JSON，不要包含其他文字。
        每日目标饮水量：${targetMl.toInt()}ml

        饮水记录：
        ${records.joinToString("\n")}
    """.trimIndent()

    /** 解析分析结果 */
    private fun parseAnalysisResult(content: String): WaterIntakeAnalysis {
        val jsonStr = extractJson(content)
        return json.decodeFromString(WaterIntakeAnalysis.serializer(), jsonStr)
    }

    /** 从可能包含 markdown 代码块的文本中提取 JSON */
    private fun extractJson(text: String): String {
        val trimmed = text.trim()
        if (trimmed.startsWith("```")) {
            val lines = trimmed.lines()
            return lines.drop(1).dropLast(1).joinToString("\n").trim()
        }
        return trimmed
    }
}
