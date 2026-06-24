package com.timemark.app.core.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Flow 相关扩展函数
 *
 * 提供将 Flow 转换为 StateFlow 的便捷方法，简化 ViewModel 中的常用写法。
 */

/**
 * 将 Flow 转换为 StateFlow（便捷扩展）
 * 使用 SharingStarted.WhileSubscribed(5000) 策略：订阅者取消后保留数据 5 秒
 *
 * @param viewModelScope 协程作用域（通常是 ViewModel 的 viewModelScope）
 * @param initialValue 初始值
 */
fun <T> Flow<T>.stateIn(
    viewModelScope: CoroutineScope,
    initialValue: T
): StateFlow<T> = stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = initialValue
)

/**
 * 将 Flow 转换为 StateFlow（别名，语义更贴近 "作为 StateFlow 使用"）
 *
 * @param scope 协程作用域
 * @param initial 初始值
 */
fun <T> Flow<T>.asStateFlow(
    scope: CoroutineScope,
    initial: T
): StateFlow<T> = stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = initial
)
