package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.Result
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIModelType
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.FoodRecognitionResult
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * 食物识别用例
 *
 * 流程：
 * 1. 读取图片并转 base64
 * 2. 构造提示词（要求模型输出 JSON 格式食物清单）
 * 3. 通过 [AIService] 调用多模态模型识别图片
 * 4. 解析 JSON 结果为 [FoodRecognitionResult]
 *
 * 若未配置多模态模型，返回错误提示。
 */
class RecognizeFoodUseCase @Inject constructor(
    private val aiService: AIService,
    private val aiConfigRepository: AIConfigRepository
) {

    /** 默认 JSON 解析器（domain 模块不依赖 data 模块的 JsonUtils） */
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** 识别图片中的食物 */
    suspend operator fun invoke(imagePath: String): Result<FoodRecognitionResult> {
        // 1. 获取多模态配置
        val config = aiConfigRepository.getConfigsByFeature(AIFeature.FOOD_RECOGNITION)
            .first()
            .firstOrNull { it.modelType == AIModelType.MULTIMODAL && it.enabled }
            ?: aiConfigRepository.getDefaultMultimodalConfig().first()
            ?: return Result.Error("未配置多模态 AI 模型，无法识别图片")

        // 2. 读取图片并转 base64
        val imageBase64 = runCatching { imageToBase64(imagePath) }
            .getOrElse { return Result.Error("读取图片失败: ${it.message}") }

        // 3. 构造提示词
        val prompt = buildRecognitionPrompt()

        // 4. 调用 AI
        val response = aiService.recognizeImage(imageBase64, prompt, config)
        if (!response.success) {
            return Result.Error(response.errorMessage ?: "AI 识别失败")
        }

        // 5. 解析 JSON 结果
        return runCatching {
            val result = parseRecognitionResult(response.content)
            Result.Success(result)
        }.getOrElse { e ->
            Result.Error("解析识别结果失败: ${e.message}")
        }
    }

    /** 构造食物识别提示词 */
    private fun buildRecognitionPrompt(): String = """
        请识别图片中的所有食物，并返回 JSON 格式结果。
        格式如下：
        {
          "items": [
            {
              "name": "食物名称",
              "portion": "份量描述（如：1 碗）",
              "portionGrams": 200,
              "calories": 150.0,
              "protein": 5.0,
              "carbs": 30.0,
              "fat": 2.0,
              "fiber": 1.0,
              "confidence": 0.9
            }
          ],
          "totalCalories": 150.0,
          "mealType": "早餐"
        }
        仅返回 JSON，不要包含其他文字。
    """.trimIndent()

    /** 解析识别结果 JSON */
    private fun parseRecognitionResult(content: String): FoodRecognitionResult {
        // 提取 JSON 片段（兼容模型可能添加的 markdown 代码块）
        val jsonStr = extractJson(content)
        return json.decodeFromString(FoodRecognitionResult.serializer(), jsonStr)
    }

    /** 从可能包含 markdown 代码块的文本中提取 JSON */
    private fun extractJson(text: String): String {
        val trimmed = text.trim()
        // 去除 ```json ... ``` 包裹
        if (trimmed.startsWith("```")) {
            val lines = trimmed.lines()
            return lines.drop(1).dropLast(1).joinToString("\n").trim()
        }
        return trimmed
    }

    /** 将图片文件转为 base64 字符串（使用 JDK 内置 Base64，避免依赖 Android API） */
    private fun imageToBase64(path: String): String {
        val file = java.io.File(path)
        val bytes = file.readBytes()
        return java.util.Base64.getEncoder().encodeToString(bytes)
    }
}
