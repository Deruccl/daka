package com.timemark.app.ai

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * 网络请求日志记录器（Task 32.6）
 *
 * 作为 OkHttp Interceptor 记录所有 AI 网络请求：
 * - URL、HTTP 方法、请求体大小
 * - 响应码、响应时间、Token 数（从响应头解析）
 *
 * 日志保存策略：
 * - 内存中保留最近 100 条（使用 ConcurrentLinkedDeque 线程安全）
 * - 通过 StateFlow 暴露给 UI 层订阅
 *
 * 使用方式：作为 OkHttp Interceptor 添加到 OkHttpClient。
 */
object NetworkLogger : Interceptor {

    private const val TAG = "NetworkLogger"
    private const val MAX_LOG_SIZE = 100

    /** 日志记录存储（线程安全的双端队列） */
    private val logDeque = ConcurrentLinkedDeque<NetworkLogEntry>()

    /** 日志列表 StateFlow，供 UI 订阅 */
    private val _logs = MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    val logs: StateFlow<List<NetworkLogEntry>> = _logs.asStateFlow()

    /** 是否启用日志记录 */
    @Volatile
    var enabled: Boolean = false

    /**
     * 拦截网络请求并记录日志。
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()

        // 请求体大小
        val requestSize = request.body?.contentLength() ?: 0L

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            // 记录失败的请求
            if (enabled) {
                val elapsed = System.currentTimeMillis() - startTime
                addLog(
                    NetworkLogEntry(
                        url = request.url.toString(),
                        method = request.method,
                        requestSize = requestSize,
                        responseCode = -1,
                        responseTimeMs = elapsed,
                        tokenCount = 0,
                        timestamp = System.currentTimeMillis(),
                        errorMessage = e.message
                    )
                )
            }
            throw e
        }

        // 记录成功的响应
        if (enabled) {
            val elapsed = System.currentTimeMillis() - startTime
            val tokenCount = parseTokenCount(response)
            addLog(
                NetworkLogEntry(
                    url = request.url.toString(),
                    method = request.method,
                    requestSize = requestSize,
                    responseCode = response.code,
                    responseTimeMs = elapsed,
                    tokenCount = tokenCount,
                    timestamp = System.currentTimeMillis(),
                    errorMessage = null
                )
            )
        }

        return response
    }

    /**
     * 添加日志条目，超出最大数量时移除最旧的。
     */
    private fun addLog(entry: NetworkLogEntry) {
        logDeque.addFirst(entry)
        while (logDeque.size > MAX_LOG_SIZE) {
            logDeque.removeLast()
        }
        _logs.value = logDeque.toList()
        Log.d(TAG, "网络请求：${entry.method} ${entry.url} -> ${entry.responseCode} (${entry.responseTimeMs}ms)")
    }

    /**
     * 从响应头解析 Token 数量。
     * 常见 AI API 会在响应头中返回 token 用量。
     */
    private fun parseTokenCount(response: Response): Int {
        // 尝试从常见 header 解析 token 数
        val tokenHeaders = listOf(
            "x-token-usage",
            "x-total-tokens",
            "openai-usage-total-tokens"
        )
        for (header in tokenHeaders) {
            response.header(header)?.toIntOrNull()?.let { return it }
        }
        return 0
    }

    /**
     * 清除所有日志。
     */
    fun clearLogs() {
        logDeque.clear()
        _logs.value = emptyList()
    }

    /**
     * 获取日志的可读格式字符串（用于文件导出）。
     */
    fun getLogsAsText(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return logDeque.reversed().joinToString("\n") { entry ->
            buildString {
                append("[${dateFormat.format(Date(entry.timestamp))}] ")
                append("${entry.method} ${entry.url}")
                append(" -> ${entry.responseCode}")
                append(" (${entry.responseTimeMs}ms)")
                if (entry.tokenCount > 0) {
                    append(" tokens=${entry.tokenCount}")
                }
                if (entry.requestSize > 0) {
                    append(" reqSize=${entry.requestSize}")
                }
                entry.errorMessage?.let { append(" ERROR: $it") }
            }
        }
    }
}

/**
 * 网络请求日志条目
 *
 * @param url 请求 URL
 * @param method HTTP 方法（GET/POST 等）
 * @param requestSize 请求体大小（字节）
 * @param responseCode 响应码（-1 表示请求失败）
 * @param responseTimeMs 响应耗时（毫秒）
 * @param tokenCount Token 消耗数
 * @param timestamp 时间戳
 * @param errorMessage 错误信息（成功时为 null）
 */
data class NetworkLogEntry(
    val url: String,
    val method: String,
    val requestSize: Long,
    val responseCode: Int,
    val responseTimeMs: Long,
    val tokenCount: Int,
    val timestamp: Long,
    val errorMessage: String?
)
