package com.timemark.app.domain

/**
 * 统一的结果包装类（domain 层）
 *
 * 用于封装业务操作的返回结果，明确区分成功、失败、加载中三种状态。
 * domain 模块为纯 Kotlin/JVM 模块，无法依赖 core 模块的 Result 类，
 * 因此在此定义同构的 Result 类型，供 AI 相关 UseCase 占位实现使用。
 */
sealed class Result<out T> {

    /** 成功，携带数据 */
    data class Success<out T>(val data: T) : Result<T>()

    /** 失败，携带错误消息与可选异常 */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : Result<Nothing>()

    /** 加载中 */
    object Loading : Result<Nothing>()
}
