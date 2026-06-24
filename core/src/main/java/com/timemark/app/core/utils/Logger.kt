package com.timemark.app.core.utils

import com.timemark.app.core.BuildConfig
import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * 日志工具类
 *
 * 统一封装 Android 日志输出与文件日志管理：
 * - 分级日志 v/d/i/w/e
 * - Release 构建自动关闭 d/v 级别日志（通过 BuildConfig.DEBUG 判断）
 * - e 级别始终输出，便于线上问题排查
 * - 提供格式化方法，避免不必要的字符串拼接开销
 *
 * Task 38.3: 增强文件日志能力
 * - 日志写入文件（app/logs/timemark.log）
 * - 文件大小限制（默认 5MB，可配置）
 * - 超过限制时自动轮转（保留最近 1 个备份 timemark.log.bak）
 * - 日志格式：[时间] [级别] [标签] 消息
 * - 用户可开关日志（setLoggingEnabled）
 * - 用户可设置日志级别（setLogLevel）
 * - 导出日志文件（exportLogs）
 * - 清除所有日志（clearLogs）
 */
object Logger {

    private const val DEFAULT_TAG = "TimeMark"

    /** 日志目录名 */
    private const val LOG_DIR_NAME = "logs"

    /** 主日志文件名 */
    private const val LOG_FILE_NAME = "timemark.log"

    /** 备份日志文件名（轮转时使用） */
    private const val LOG_BAK_FILE_NAME = "timemark.log.bak"

    /** 默认日志文件大小上限（5MB） */
    private const val DEFAULT_MAX_FILE_SIZE: Long = 5L * 1024 * 1024

    /** 日志时间格式 */
    private val logDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    /** 日志级别枚举 */
    enum class LogLevel(val priority: Int, val label: String) {
        VERBOSE(Log.VERBOSE, "V"),
        DEBUG(Log.DEBUG, "D"),
        INFO(Log.INFO, "I"),
        WARN(Log.WARN, "W"),
        ERROR(Log.ERROR, "E")
    }

    /** 应用上下文（由 init 方法设置） */
    private val contextRef = AtomicReference<Context?>(null)

    /** 日志开关（默认开启） */
    private val loggingEnabled = AtomicBoolean(true)

    /** 当前日志级别（默认 DEBUG，仅输出 DEBUG 及以上级别到文件） */
    private val logLevel = AtomicReference(LogLevel.DEBUG)

    /** 日志文件大小上限 */
    @Volatile
    private var maxFileSize: Long = DEFAULT_MAX_FILE_SIZE

    /**
     * 初始化日志工具，设置应用上下文。
     *
     * 应在 Application.onCreate 中调用，用于启用文件日志功能。
     * 使用 applicationContext 避免内存泄漏。
     *
     * @param context 应用上下文
     */
    fun init(context: Context) {
        contextRef.set(context.applicationContext)
    }

    /**
     * Task 38.3: 设置日志开关
     *
     * 用户可在设置中关闭日志。关闭后：
     * - 不再写入文件日志
     * - Logcat 输出不受影响（仍受 BuildConfig.DEBUG 控制）
     *
     * @param enabled 是否启用日志
     */
    fun setLoggingEnabled(enabled: Boolean) {
        loggingEnabled.set(enabled)
    }

    /**
     * Task 38.3: 查询日志是否启用
     */
    fun isLoggingEnabled(): Boolean = loggingEnabled.get()

    /**
     * Task 38.3: 设置日志级别
     *
     * 仅不低于设定级别的日志会写入文件。
     * 例如设置为 WARN 时，仅 WARN 和 ERROR 写入文件。
     *
     * @param level 日志级别
     */
    fun setLogLevel(level: LogLevel) {
        logLevel.set(level)
    }

    /**
     * Task 38.3: 查询当前日志级别
     */
    fun getLogLevel(): LogLevel = logLevel.get()

    /**
     * Task 38.3: 设置日志文件大小上限
     *
     * @param maxBytes 最大字节数，默认 5MB
     */
    fun setMaxFileSize(maxBytes: Long) {
        maxFileSize = maxBytes.coerceAtLeast(1024L) // 最小 1KB
    }

    /**
     * Task 38.3: 获取日志文件
     *
     * @return 主日志文件，若未初始化返回 null
     */
    fun getLogFile(): File? {
        val context = contextRef.get() ?: return null
        val logDir = File(context.filesDir, LOG_DIR_NAME)
        return File(logDir, LOG_FILE_NAME)
    }

    /**
     * Task 38.3: 获取日志文件大小（字节）
     *
     * @return 主日志文件大小，文件不存在返回 0
     */
    fun getLogFileSize(): Long {
        return getLogFile()?.length() ?: 0L
    }

    /**
     * Task 38.3: 获取日志文件大小（人类可读格式）
     *
     * @return 如 "1.23 MB"、"456 KB"、"789 B"
     */
    fun getLogFileSizeFormatted(): String {
        val size = getLogFileSize()
        return formatFileSize(size)
    }

    /**
     * Task 38.3: 导出日志文件
     *
     * 将主日志与备份日志合并导出到应用缓存目录，便于分享。
     *
     * @return 导出的日志文件，若日志为空返回 null
     */
    fun exportLogs(): File? {
        val context = contextRef.get() ?: return null
        val logDir = File(context.filesDir, LOG_DIR_NAME)
        val logFile = File(logDir, LOG_FILE_NAME)
        val bakFile = File(logDir, LOG_BAK_FILE_NAME)

        if (!logFile.exists() && !bakFile.exists()) return null

        // 导出到缓存目录，文件名带时间戳
        val exportDir = File(context.cacheDir, "exported_logs").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val exportFile = File(exportDir, "timemark_log_$timestamp.txt")

        exportFile.bufferedWriter().use { writer ->
            // 先写入备份日志（较旧）
            if (bakFile.exists()) {
                writer.appendLine("=== 备份日志（较旧）===")
                bakFile.bufferedReader().use { reader ->
                    reader.forEachLine { writer.appendLine(it) }
                }
                writer.appendLine()
            }
            // 再写入主日志（较新）
            if (logFile.exists()) {
                writer.appendLine("=== 主日志（较新）===")
                logFile.bufferedReader().use { reader ->
                    reader.forEachLine { writer.appendLine(it) }
                }
            }
        }

        return exportFile
    }

    /**
     * Task 38.3: 清除所有日志
     *
     * 删除主日志与备份日志文件。
     *
     * @return 是否成功清除（文件不存在也返回 true）
     */
    fun clearLogs(): Boolean {
        val context = contextRef.get() ?: return false
        val logDir = File(context.filesDir, LOG_DIR_NAME)
        val logFile = File(logDir, LOG_FILE_NAME)
        val bakFile = File(logDir, LOG_BAK_FILE_NAME)
        var success = true
        if (logFile.exists()) success = logFile.delete() && success
        if (bakFile.exists()) success = bakFile.delete() && success
        return success
    }

    /** verbose 级别日志，仅在 Debug 构建输出 */
    fun v(tag: String = DEFAULT_TAG, msg: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg, tr)
        }
        writeToFile(LogLevel.VERBOSE, tag, msg, tr)
    }

    /** debug 级别日志，仅在 Debug 构建输出 */
    fun d(tag: String = DEFAULT_TAG, msg: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg, tr)
        }
        writeToFile(LogLevel.DEBUG, tag, msg, tr)
    }

    /** info 级别日志，始终输出 */
    fun i(tag: String = DEFAULT_TAG, msg: String, tr: Throwable? = null) {
        Log.i(tag, msg, tr)
        writeToFile(LogLevel.INFO, tag, msg, tr)
    }

    /** warn 级别日志，始终输出 */
    fun w(tag: String = DEFAULT_TAG, msg: String, tr: Throwable? = null) {
        Log.w(tag, msg, tr)
        writeToFile(LogLevel.WARN, tag, msg, tr)
    }

    /** error 级别日志，始终输出 */
    fun e(tag: String = DEFAULT_TAG, msg: String, tr: Throwable? = null) {
        Log.e(tag, msg, tr)
        writeToFile(LogLevel.ERROR, tag, msg, tr)
    }

    /** debug 格式化日志，仅在 Debug 构建输出（避免 Release 下格式化开销） */
    fun d(tag: String = DEFAULT_TAG, format: String, vararg args: Any?) {
        val msg = format.format(*args)
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
        writeToFile(LogLevel.DEBUG, tag, msg, null)
    }

    /** verbose 格式化日志，仅在 Debug 构建输出 */
    fun v(tag: String = DEFAULT_TAG, format: String, vararg args: Any?) {
        val msg = format.format(*args)
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg)
        }
        writeToFile(LogLevel.VERBOSE, tag, msg, null)
    }

    /** info 格式化日志，始终输出 */
    fun i(tag: String = DEFAULT_TAG, format: String, vararg args: Any?) {
        val msg = format.format(*args)
        Log.i(tag, msg)
        writeToFile(LogLevel.INFO, tag, msg, null)
    }

    /** warn 格式化日志，始终输出 */
    fun w(tag: String = DEFAULT_TAG, format: String, vararg args: Any?) {
        val msg = format.format(*args)
        Log.w(tag, msg)
        writeToFile(LogLevel.WARN, tag, msg, null)
    }

    /** error 格式化日志，始终输出 */
    fun e(tag: String = DEFAULT_TAG, format: String, vararg args: Any?) {
        val msg = format.format(*args)
        Log.e(tag, msg)
        writeToFile(LogLevel.ERROR, tag, msg, null)
    }

    /**
     * Task 38.3: 将日志写入文件
     *
     * 日志格式：[时间] [级别] [标签] 消息
     * 若带异常堆栈，追加堆栈信息。
     *
     * 文件大小超过 [maxFileSize] 时自动轮转：
     * - 删除旧备份 timemark.log.bak
     * - 将 timemark.log 重命名为 timemark.log.bak
     * - 创建新的 timemark.log
     *
     * @param level 日志级别
     * @param tag 标签
     * @param msg 消息
     * @param tr 可选异常
     */
    private fun writeToFile(level: LogLevel, tag: String, msg: String, tr: Throwable?) {
        // 检查日志开关与级别
        if (!loggingEnabled.get()) return
        if (level.priority < logLevel.get().priority) return

        val context = contextRef.get() ?: return
        val logDir = File(context.filesDir, LOG_DIR_NAME)
        if (!logDir.exists()) logDir.mkdirs()

        val logFile = File(logDir, LOG_FILE_NAME)

        // 检查文件大小，超过限制时轮转
        if (logFile.exists() && logFile.length() >= maxFileSize) {
            rotateLogs(logDir)
        }

        // 构建日志行
        val timestamp = logDateFormat.format(Date())
        val logLine = buildString {
            append("[$timestamp] [${level.label}] [$tag] $msg")
            if (tr != null) {
                append("\n")
                append(getStackTraceString(tr))
            }
            append("\n")
        }

        // 写入文件（追加模式）
        runCatching {
            logFile.appendText(logLine)
        }
    }

    /**
     * Task 38.3: 日志文件轮转
     *
     * 保留最近 1 个备份：
     * 1. 删除旧备份 timemark.log.bak
     * 2. 将 timemark.log 重命名为 timemark.log.bak
     */
    private fun rotateLogs(logDir: File) {
        val bakFile = File(logDir, LOG_BAK_FILE_NAME)
        val logFile = File(logDir, LOG_FILE_NAME)
        // 删除旧备份
        if (bakFile.exists()) bakFile.delete()
        // 当前日志重命名为备份
        logFile.renameTo(bakFile)
    }

    /** 获取异常堆栈字符串 */
    private fun getStackTraceString(tr: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        return sw.toString()
    }

    /** 格式化文件大小为人类可读 */
    private fun formatFileSize(size: Long): String {
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024
        return when {
            size >= gb -> String.format(Locale.getDefault(), "%.2f GB", size / gb)
            size >= mb -> String.format(Locale.getDefault(), "%.2f MB", size / mb)
            size >= kb -> String.format(Locale.getDefault(), "%.2f KB", size / kb)
            else -> "$size B"
        }
    }
}
