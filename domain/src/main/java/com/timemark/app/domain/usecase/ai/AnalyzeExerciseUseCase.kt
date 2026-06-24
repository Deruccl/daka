package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.Result
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ExerciseAnalysis
import com.timemark.app.domain.model.ExerciseTrend
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * 运动数据分析用例
 *
 * 基于用户提供的运动记录（类型+时长+热量）调用文本 AI 模型分析：
 * - 运动效果
 * - 趋势分析
 * - 计划建议
 */
class AnalyzeExerciseUseCase @Inject constructor(
    private val aiService: AIService,
    private val aiConfigRepository: AIConfigRepository
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * 分析运动数据
     *
     * @param exerciseRecords 运动记录列表，每条形如 "2026-06-21 跑步 30分钟 300大卡"
     */
    suspend operator fun invoke(exerciseRecords: List<String>): Result<ExerciseAnalysis> {
        if (exerciseRecords.isEmpty()) {
            return Result.Error("没有可分析的运动记录")
        }

        val config = aiConfigRepository.getConfigsByFeature(AIFeature.EXERCISE_ANALYSIS)
            .first()
            .firstOrNull { it.enabled }
            ?: aiConfigRepository.getDefaultTextConfig().first()
            ?: return Result.Error("未配置文本 AI 模型，无法进行运动分析")

        val prompt = buildAnalysisPrompt(exerciseRecords)
        val request = ChatRequest(
            messages = listOf(
                ChatMessage(role = "system", content = "你是运动健康分析专家，请基于用户的运动记录分析运动效果与趋势，返回 JSON 格式结果。"),
                ChatMessage(role = "user", content = prompt)
            ),
            model = config.model,
            temperature = 0.3,
            maxTokens = config.maxTokens
        )

        val response = aiService.chat(request, config)
        if (!response.success) {
            return Result.Error(response.errorMessage ?: "AI 分析失败")
        }

        return runCatching {
            val result = parseAnalysisResult(response.content)
            Result.Success(result)
        }.getOrElse { e ->
            Result.Error("解析分析结果失败: ${e.message}")
        }
    }

    /** 构造分析提示词 */
    private fun buildAnalysisPrompt(records: List<String>): String = """
        请基于以下运动记录分析用户的运动情况，返回 JSON 格式结果：
        {
          "totalDurationMinutes": 150,
          "totalCalories": 1200.0,
          "averageDurationPerDay": 21.4,
          "effect": "运动效果描述",
          "trend": "INCREASING",
          "planSuggestion": "下周运动计划建议"
        }
        trend 取值：INCREASING（上升）、STABLE（平稳）、DECREASING（下降）
        仅返回 JSON，不要包含其他文字。

        运动记录：
        ${records.joinToString("\n")}
    """.trimIndent()

    /** 解析分析结果 */
    private fun parseAnalysisResult(content: String): ExerciseAnalysis {
        val jsonStr = extractJson(content)
        return json.decodeFromString(ExerciseAnalysis.serializer(), jsonStr)
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
