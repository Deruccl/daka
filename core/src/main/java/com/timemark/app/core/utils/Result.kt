package com.timemark.app.core.utils

/**
 * 统一的结果包装类
 *
 * 用于封装业务操作的返回结果，明确区分成功、失败、加载中三种状态，
 * 便于在 ViewModel / UI 层进行状态分发与处理。
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

    /**
     * 对成功结果进行映射变换
     * - Success：应用 transform 后返回新的 Success
     * - 其他状态：原样返回
     */
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    /**
     * 成功时执行 [action]，便于链式调用处理副作用
     * @return 原始 Result，便于继续链式调用
     */
    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * 失败时执行 [action]，便于链式调用处理副作用
     * @return 原始 Result，便于继续链式调用
     */
    fun onError(action: (String, Throwable?) -> Unit): Result<T> {
        if (this is Error) action(message, cause)
        return this
    }
}
