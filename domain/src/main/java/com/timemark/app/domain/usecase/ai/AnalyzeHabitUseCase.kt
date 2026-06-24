package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.Result
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.HabitAnalysis
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * 习惯养成分析用例
 *
 * 基于用户提供的打卡记录调用文本 AI 模型分析：
 * - 坚持度
 * - 连续天数
 * - 成功率
 * - 建议
 * - 鼓励话语
 */
class AnalyzeHabitUseCase @Inject constructor(
    private val aiService: AIService,
    private val aiConfigRepository: AIConfigRepository
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * 分析习惯养成情况
     *
     * @param habitRecords 习惯打卡记录列表，每条形如 "2026-06-21 习惯名 完成"
     * @param habitName 习惯名称（可选）
     */
    suspend operator fun invoke(habitRecords: List<String>, habitName: String? = null): Result<HabitAnalysis> {
        if (habitRecords.isEmpty()) {
            return Result.Error("没有可分析的习惯记录")
        }

        val config = aiConfigRepository.getConfigsByFeature(AIFeature.HABIT_ANALYSIS)
            .first()
            .firstOrNull { it.enabled }
            ?: aiConfigRepository.getDefaultTextConfig().first()
            ?: return Result.Error("未配置文本 AI 模型，无法进行习惯分析")

        val prompt = buildAnalysisPrompt(habitRecords, habitName)
        val request = ChatRequest(
            messages = listOf(
                ChatMessage(role = "system", content = "你是习惯养成教练，请基于用户的打卡记录分析习惯坚持情况，并给出鼓励性建议，返回 JSON 格式结果。"),
                ChatMessage(role = "user", content = prompt)
            ),
            model = config.model,
            temperature = 0.5,
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
    private fun buildAnalysisPrompt(records: List<String>, habitName: String?): String {
        val habitDesc = habitName?.let { "习惯名称：$it" } ?: "综合习惯"
        return """
            请基于以下打卡记录分析用户的习惯养成情况，返回 JSON 格式结果：
            {
              "consistencyScore": 80,
              "currentStreakDays": 7,
              "longestStreakDays": 15,
              "successRate": 0.85,
              "suggestions": ["建议1", "建议2"],
              "encouragement": "鼓励话语"
            }
            仅返回 JSON，不要包含其他文字。
            $habitDesc

            打卡记录：
            ${records.joinToString("\n")}
        """.trimIndent()
    }

    /** 解析分析结果 */
    private fun parseAnalysisResult(content: String): HabitAnalysis {
        val jsonStr = extractJson(content)
        return json.decodeFromString(HabitAnalysis.serializer(), jsonStr)
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
