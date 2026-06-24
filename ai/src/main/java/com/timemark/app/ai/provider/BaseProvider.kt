package com.timemark.app.ai.provider

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Provider 基类
 *
 * 提供通用的 HTTP 请求与 JSON 解析工具方法，
 * 各具体 Provider 复用这些方法实现自己的请求构造与响应解析。
 */
abstract class BaseProvider(
    protected val okHttpClient: OkHttpClient,
    protected val json: Json
) : AIProvider {

    /** 构造 POST 请求并发送，返回响应字符串与 HTTP 状态码（在 IO 线程执行） */
    protected suspend fun postJson(url: String, apiKey: String, body: String, extraHeaders: Map<String, String> = emptyMap()): Pair<Int, String> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .apply {
                extraHeaders.forEach { (k, v) -> header(k, v) }
                if (apiKey.isNotEmpty()) header("Authorization", "Bearer $apiKey")
            }
            .post(body.toRequestBody(JSON_MEDIA_TYPE))
            .build()
        okHttpClient.newCall(request).execute().use { resp ->
            resp.code to (resp.body?.string() ?: "")
        }
    }

    /** 从 OpenAI 兼容响应中解析统一结果 */
    protected fun parseOpenAIResponse(raw: String, model: String, errorMessage: String? = null): ChatResponse {
        if (errorMessage != null) {
            return ChatResponse(
                content = "",
                tokensInput = 0,
                tokensOutput = 0,
                model = model,
                success = false,
                errorMessage = errorMessage
            )
        }
        return runCatching {
            val obj = json.parseToJsonElement(raw).jsonObject
            val choices = obj["choices"]?.jsonArray ?: JsonArray(emptyList())
            val content = choices.firstOrNull()
                ?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull
                ?: ""
            val usage = obj["usage"]?.jsonObject
            val inputTokens = usage?.get("prompt_tokens")?.jsonPrimitive?.intOrNull ?: 0
            val outputTokens = usage?.get("completion_tokens")?.jsonPrimitive?.intOrNull ?: 0
            ChatResponse(
                content = content,
                tokensInput = inputTokens,
                tokensOutput = outputTokens,
                model = model,
                success = true
            )
        }.getOrElse { e ->
            ChatResponse(
                content = "",
                tokensInput = 0,
                tokensOutput = 0,
                model = model,
                success = false,
                errorMessage = "解析响应失败: ${e.message}"
            )
        }
    }

    /** 从 Anthropic Messages 响应中解析统一结果 */
    protected fun parseAnthropicResponse(raw: String, model: String): ChatResponse {
        return runCatching {
            val obj = json.parseToJsonElement(raw).jsonObject
            val content = obj["content"]?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: ""
            val usage = obj["usage"]?.jsonObject
            val inputTokens = usage?.get("input_tokens")?.jsonPrimitive?.intOrNull ?: 0
            val outputTokens = usage?.get("output_tokens")?.jsonPrimitive?.intOrNull ?: 0
            ChatResponse(
                content = content,
                tokensInput = inputTokens,
                tokensOutput = outputTokens,
                model = model,
                success = true
            )
        }.getOrElse { e ->
            ChatResponse(
                content = "",
                tokensInput = 0,
                tokensOutput = 0,
                model = model,
                success = false,
                errorMessage = "解析响应失败: ${e.message}"
            )
        }
    }

    /** 从 Gemini generateContent 响应中解析统一结果 */
    protected fun parseGeminiResponse(raw: String, model: String): ChatResponse {
        return runCatching {
            val obj = json.parseToJsonElement(raw).jsonObject
            val candidates = obj["candidates"]?.jsonArray ?: JsonArray(emptyList())
            val content = candidates.firstOrNull()
                ?.jsonObject?.get("content")?.jsonObject?.get("parts")?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: ""
            val usage = obj["usageMetadata"]?.jsonObject
            val inputTokens = usage?.get("promptTokenCount")?.jsonPrimitive?.intOrNull ?: 0
            val outputTokens = usage?.get("candidatesTokenCount")?.jsonPrimitive?.intOrNull ?: 0
            ChatResponse(
                content = content,
                tokensInput = inputTokens,
                tokensOutput = outputTokens,
                model = model,
                success = true
            )
        }.getOrElse { e ->
            ChatResponse(
                content = "",
                tokensInput = 0,
                tokensOutput = 0,
                model = model,
                success = false,
                errorMessage = "解析响应失败: ${e.message}"
            )
        }
    }

    /** 构造 OpenAI 兼容请求体（含可选图片） */
    protected fun buildOpenAIRequestBody(
        request: ChatRequest,
        imageBase64: String? = null,
        imagePrompt: String? = null
    ): String {
        val messages = buildJsonArray {
            request.messages.forEach { msg ->
                add(buildJsonObject {
                    put("role", JsonPrimitive(msg.role))
                    if (imageBase64 != null && msg.role == "user") {
                        put("content", buildJsonArray {
                            add(buildJsonObject {
                                put("type", JsonPrimitive("text"))
                                put("text", JsonPrimitive(imagePrompt ?: msg.content))
                            })
                            add(buildJsonObject {
                                put("type", JsonPrimitive("image_url"))
                                put("image_url", buildJsonObject {
                                    put("url", JsonPrimitive("data:image/jpeg;base64,$imageBase64"))
                                })
                            })
                        })
                    } else {
                        put("content", JsonPrimitive(msg.content))
                    }
                })
            }
        }
        val obj = buildJsonObject {
            put("model", JsonPrimitive(request.model))
            put("messages", messages)
            put("temperature", JsonPrimitive(request.temperature))
            put("max_tokens", JsonPrimitive(request.maxTokens))
        }
        return json.encodeToString(JsonObject.serializer(), obj)
    }

    /** 构造 Anthropic Messages 请求体 */
    protected fun buildAnthropicRequestBody(
        request: ChatRequest,
        imageBase64: String? = null,
        imagePrompt: String? = null
    ): String {
        val systemMessage = request.messages.firstOrNull { it.role == "system" }?.content
        val dialogMessages = request.messages.filter { it.role != "system" }
        val messages = buildJsonArray {
            dialogMessages.forEach { msg ->
                add(buildJsonObject {
                    put("role", JsonPrimitive(msg.role))
                    if (imageBase64 != null && msg.role == "user") {
                        put("content", buildJsonArray {
                            add(buildJsonObject {
                                put("type", JsonPrimitive("image"))
                                put("source", buildJsonObject {
                                    put("type", JsonPrimitive("base64"))
                                    put("media_type", JsonPrimitive("image/jpeg"))
                                    put("data", JsonPrimitive(imageBase64))
                                })
                            })
                            add(buildJsonObject {
                                put("type", JsonPrimitive("text"))
                                put("text", JsonPrimitive(imagePrompt ?: msg.content))
                            })
                        })
                    } else {
                        put("content", JsonPrimitive(msg.content))
                    }
                })
            }
        }
        val obj = buildJsonObject {
            put("model", JsonPrimitive(request.model))
            put("messages", messages)
            put("max_tokens", JsonPrimitive(request.maxTokens))
            if (systemMessage != null) put("system", JsonPrimitive(systemMessage))
        }
        return json.encodeToString(JsonObject.serializer(), obj)
    }

    /** 构造 Gemini generateContent 请求体 */
    protected fun buildGeminiRequestBody(
        request: ChatRequest,
        imageBase64: String? = null,
        imagePrompt: String? = null
    ): String {
        val contents = buildJsonArray {
            request.messages.forEach { msg ->
                add(buildJsonObject {
                    put("role", JsonPrimitive(if (msg.role == "assistant") "model" else "user"))
                    put("parts", buildJsonArray {
                        if (imageBase64 != null && msg.role == "user") {
                            add(buildJsonObject {
                                put("text", JsonPrimitive(imagePrompt ?: msg.content))
                            })
                            add(buildJsonObject {
                                put("inlineData", buildJsonObject {
                                    put("mimeType", JsonPrimitive("image/jpeg"))
                                    put("data", JsonPrimitive(imageBase64))
                                })
                            })
                        } else {
                            add(buildJsonObject {
                                put("text", JsonPrimitive(msg.content))
                            })
                        }
                    })
                })
            }
        }
        val obj = buildJsonObject {
            put("contents", contents)
            put("generationConfig", buildJsonObject {
                put("temperature", JsonPrimitive(request.temperature))
                put("maxOutputTokens", JsonPrimitive(request.maxTokens))
            })
        }
        return json.encodeToString(JsonObject.serializer(), obj)
    }

    /** 规范化 baseUrl，去除末尾斜杠 */
    protected fun normalizeBaseUrl(config: AIConfig): String =
        config.baseUrl.trimEnd('/')

    companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
