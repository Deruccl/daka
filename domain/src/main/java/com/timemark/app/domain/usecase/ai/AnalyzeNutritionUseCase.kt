package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.Result
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.NutritionAnalysis
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * 营养分析用例
 *
 * 基于打卡记录（食物清单等）调用文本 AI 模型分析营养摄入，
 * 返回热量分析、营养均衡度、饮食建议等结构化结果。
 */
class AnalyzeNutritionUseCase @Inject constructor(
    private val aiService: AIService,
    private val aiConfigRepository: AIConfigRepository
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** 分析营养摄入 */
    suspend operator fun invoke(records: List<String>): Result<NutritionAnalysis> {
        if (records.isEmpty()) {
            return Result.Error("没有可分析的记录")
        }

        // 获取文本模型配置
        val config = aiConfigRepository.getConfigsByFeature(AIFeature.NUTRITION_ANALYSIS)
            .first()
            .firstOrNull { it.enabled }
            ?: aiConfigRepository.getDefaultTextConfig().first()
            ?: return Result.Error("未配置文本 AI 模型，无法进行分析")

        // 构造请求
        val prompt = buildAnalysisPrompt(records)
        val request = ChatRequest(
            messages = listOf(
                ChatMessage(role = "system", content = "你是营养分析专家，请基于用户提供的食物记录分析营养摄入情况。"),
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
            Result.Success(result)
        }.getOrElse { e ->
            Result.Error("解析分析结果失败: ${e.message}")
        }
    }

    /** 构造分析提示词 */
    private fun buildAnalysisPrompt(records: List<String>): String = """
        请基于以下食物记录分析营养摄入情况，返回 JSON 格式结果：
        {
          "calorieAnalysis": "热量摄入分析",
          "nutrientBalance": "营养均衡度分析",
          "dietHabits": "饮食习惯分析",
          "suggestions": ["建议1", "建议2"],
          "score": 75
        }
        仅返回 JSON，不要包含其他文字。

        食物记录：
        ${records.joinToString("\n")}
    """.trimIndent()

    /** 解析分析结果 */
    private fun parseAnalysisResult(content: String): NutritionAnalysis {
        val jsonStr = extractJson(content)
        return json.decodeFromString(NutritionAnalysis.serializer(), jsonStr)
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
