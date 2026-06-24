package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.Result
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.SleepAnalysis
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * 睡眠质量分析用例
 *
 * 基于用户提供的睡眠记录（入睡时间+起床时间）调用文本 AI 模型分析：
 * - 平均睡眠时长
 * - 睡眠质量评分
 * - 规律性
 * - 改善建议
 */
class AnalyzeSleepUseCase @Inject constructor(
    private val aiService: AIService,
    private val aiConfigRepository: AIConfigRepository
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * 分析睡眠质量
     *
     * @param sleepRecords 睡眠记录列表，每条形如 "2026-06-21 23:30 - 06:30"
     */
    suspend operator fun invoke(sleepRecords: List<String>): Result<SleepAnalysis> {
        if (sleepRecords.isEmpty()) {
            return Result.Error("没有可分析的睡眠记录")
        }

        val config = aiConfigRepository.getConfigsByFeature(AIFeature.SLEEP_ANALYSIS)
            .first()
            .firstOrNull { it.enabled }
            ?: aiConfigRepository.getDefaultTextConfig().first()
            ?: return Result.Error("未配置文本 AI 模型，无法进行睡眠分析")

        val prompt = buildAnalysisPrompt(sleepRecords)
        val request = ChatRequest(
            messages = listOf(
                ChatMessage(role = "system", content = "你是睡眠健康分析专家，请基于用户的睡眠记录分析睡眠质量，返回 JSON 格式结果。"),
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
        请基于以下睡眠记录分析用户的睡眠质量，返回 JSON 格式结果：
        {
          "averageDurationMinutes": 420,
          "qualityScore": 80,
          "regularityScore": 75,
          "bedtimePattern": "入睡时间模式描述",
          "suggestions": ["建议1", "建议2"]
        }
        仅返回 JSON，不要包含其他文字。

        睡眠记录：
        ${records.joinToString("\n")}
    """.trimIndent()

    /** 解析分析结果 */
    private fun parseAnalysisResult(content: String): SleepAnalysis {
        val jsonStr = extractJson(content)
        return json.decodeFromString(SleepAnalysis.serializer(), jsonStr)
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
