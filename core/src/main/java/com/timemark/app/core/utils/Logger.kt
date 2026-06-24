package com.timemark.app.core.utils

import com.timemark.app.core.BuildConfig
import android.util.Log

/**
 * 日志工具类
 *
 * 统一封装 Android 日志输出：
 * - 分级日志 v/d/i/w/e
 * - Release 构建自动关闭 d/v 级别日志（通过 BuildConfig.DEBUG 判断）
 * - e 级别始终输出，便于线上问题排查
 * - 提供格式化方法，避免不必要的字符串拼接开销
 */
object Logger {

    private const val DEFAULT_TAG = "TimeMark"

    /** verbose 级别日志，仅在 Debug 构建输出 */
    fun v(tag: String = DEFAULT_TAG, msg: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg, tr)
        }
    }

    /** debug 级别日志，仅在 Debug 构建输出 */
    fun d(tag: String = DEFAULT_TAG, msg: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg, tr)
        }
    }

    /** info 级别日志，始终输出 */
    fun i(tag: String = DEFAULT_TAG, msg: String, tr: Throwable? = null) {
        Log.i(tag, msg, tr)
    }

    /** warn 级别日志，始终输出 */
    fun w(tag: String = DEFAULT_TAG, msg: String, tr: Throwable? = null) {
        Log.w(tag, msg, tr)
    }

    /** error 级别日志，始终输出 */
    fun e(tag: String = DEFAULT_TAG, msg: String, tr: Throwable? = null) {
        Log.e(tag, msg, tr)
    }

    /** debug 格式化日志，仅在 Debug 构建输出（避免 Release 下格式化开销） */
    fun d(tag: String = DEFAULT_TAG, format: String, vararg args: Any?) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, format.format(*args))
        }
    }

    /** verbose 格式化日志，仅在 Debug 构建输出 */
    fun v(tag: String = DEFAULT_TAG, format: String, vararg args: Any?) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, format.format(*args))
        }
    }

    /** info 格式化日志，始终输出 */
    fun i(tag: String = DEFAULT_TAG, format: String, vararg args: Any?) {
        Log.i(tag, format.format(*args))
    }

    /** warn 格式化日志，始终输出 */
    fun w(tag: String = DEFAULT_TAG, format: String, vararg args: Any?) {
        Log.w(tag, format.format(*args))
    }

    /** error 格式化日志，始终输出 */
    fun e(tag: String = DEFAULT_TAG, format: String, vararg args: Any?) {
        Log.e(tag, format.format(*args))
    }
}
