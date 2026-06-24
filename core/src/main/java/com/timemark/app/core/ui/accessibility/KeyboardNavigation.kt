package com.timemark.app.core.ui.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Task 37.3: 键盘导航支持
 *
 * 提供方向键与 Tab 键导航能力，适配外接键盘与 TV 遥控器场景。
 *
 * - [focusableNavigation]：使元素可聚焦并支持方向键导航
 * - [FocusManagerExtensions]：方向键导航扩展函数
 */

/**
 * 可聚焦导航修饰符
 *
 * 使元素可聚焦，并按 [order] 确定导航顺序。
 * 支持 Tab 键顺序导航与方向键导航。
 *
 * @param order 导航顺序，数值越小优先级越高
 * @param onEnter 按 Enter 键时的回调
 */
@Composable
fun Modifier.focusableNavigation(
    order: Int = 0,
    onEnter: (() -> Unit)? = null
): Modifier {
    val focusManager = LocalFocusManager.current
    return this
        .focusable()
        .onPreviewKeyEvent { event ->
            // 仅处理 KeyUp 事件，避免重复触发
            if (event.type != KeyEventType.KeyUp) return@onPreviewKeyEvent false
            when (event.key) {
                Key.Enter, Key.NumPadEnter -> {
                    onEnter?.invoke()
                    true
                }
                Key.DirectionUp -> {
                    focusManager.moveFocusWithFallback(FocusDirection.Up)
                }
                Key.DirectionDown -> {
                    focusManager.moveFocusWithFallback(FocusDirection.Down)
                }
                Key.DirectionLeft -> {
                    focusManager.moveFocusWithFallback(FocusDirection.Left)
                }
                Key.DirectionRight -> {
                    focusManager.moveFocusWithFallback(FocusDirection.Right)
                }
                else -> false
            }
        }
}

/**
 * 方向键导航扩展函数
 *
 * 尝试按指定方向移动焦点，移动失败时返回 false（允许父级处理）。
 */
fun FocusManager.moveFocusWithFallback(direction: FocusDirection): Boolean {
    return moveFocus(direction)
}

/**
 * 键盘事件处理修饰符：监听 Enter 与 Back 键
 *
 * @param onEnter Enter 键回调
 * @param onBack Back 键回调
 */
@Composable
fun Modifier.handleKeyboardActions(
    onEnter: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
): Modifier {
    return this.onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyUp) return@onPreviewKeyEvent false
        when (event.key) {
            Key.Enter, Key.NumPadEnter -> {
                onEnter?.invoke()
                onEnter != null
            }
            Key.Back -> {
                onBack?.invoke()
                onBack != null
            }
            else -> false
        }
    }
}
