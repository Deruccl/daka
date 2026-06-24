package com.timemark.app.core.ui.gesture

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * 手势交互集合
 *
 * 提供侧滑返回、双指缩放、长按反馈、拖拽关闭等手势。
 * 所有手势均使用 Compose 的 pointerInput + detect*Gestures 实现。
 */

/** 侧滑返回触发的边缘宽度 */
private val SWIPE_BACK_EDGE_WIDTH = 24.dp

/** 侧滑返回触发的最小距离 */
private val SWIPE_BACK_THRESHOLD = 80.dp

/** 拖拽关闭触发的最小距离 */
private val DRAG_DISMISS_THRESHOLD = 120.dp

/**
 * 侧滑返回手势
 *
 * 从左边缘右滑触发返回，跟手移动。
 * 滑动距离超过阈值（80dp）时触发 onBack。
 *
 * @param onBack 返回回调
 */
fun Modifier.swipeBackGesture(onBack: () -> Unit): Modifier = composed {
    val density = LocalDensity.current
    val edgeWidthPx = with(density) { SWIPE_BACK_EDGE_WIDTH.toPx() }
    val thresholdPx = with(density) { SWIPE_BACK_THRESHOLD.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    this
        .graphicsLayer {
            translationX = dragOffset.coerceAtLeast(0f)
            alpha = 1f - (dragOffset / thresholdPx).coerceIn(0f, 0.5f)
        }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    if (dragOffset > thresholdPx) {
                        onBack()
                    }
                    // 回弹（动画由 graphicsLayer 自动处理）
                    dragOffset = 0f
                },
                onDragCancel = { dragOffset = 0f },
                onHorizontalDrag = { change, delta ->
                    // 仅在左边缘区域响应
                    if (change.position.x <= edgeWidthPx || dragOffset > 0f) {
                        dragOffset = (dragOffset + delta).coerceAtLeast(0f)
                    }
                }
            )
        }
}

/**
 * 双指缩放手势
 *
 * 用于图表等需要缩放查看的场景。
 * 每次手势开始时重置缩放为 1.0，手势结束后回调重置。
 * 支持连续多手势识别。
 *
 * @param onZoom 缩放回调，参数为当前累积缩放比例（1.0 = 原始大小）
 * @param onZoomEnd 手势结束回调（可选），用于重置 ViewModel 状态
 */
fun Modifier.pinchToZoom(
    onZoom: (Float) -> Unit,
    onZoomEnd: () -> Unit = {}
): Modifier = composed {
    var currentZoom by remember { mutableFloatStateOf(1f) }

    this.pointerInput(Unit) {
        // 循环处理多次手势，每次手势开始时重置缩放
        while (true) {
            currentZoom = 1f
            detectTransformGestures { _, _, zoom, _ ->
                currentZoom = (currentZoom * zoom).coerceIn(0.5f, 3f)
                onZoom(currentZoom)
            }
            // 手势结束：重置缩放并通知 ViewModel
            currentZoom = 1f
            onZoom(1f)
            onZoomEnd()
        }
    }
}

/**
 * 长按反馈手势
 *
 * 长按 500ms 触发回调，带有视觉反馈（轻微缩放）。
 *
 * @param onLongPress 长按回调
 */
fun Modifier.longPressFeedback(onLongPress: () -> Unit): Modifier = composed {
    var isLongPressed by remember { mutableStateOf(false) }

    // 长按时的缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isLongPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "longPressScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    isLongPressed = true
                    onLongPress()
                    // 触发后恢复
                    isLongPressed = false
                }
            )
        }
}

/**
 * 拖拽关闭手势
 *
 * 向下拖拽超过阈值（120dp）时触发关闭。
 * 跟手移动，带有透明度变化。
 *
 * @param onDismiss 关闭回调
 */
fun Modifier.dragToDismiss(onDismiss: () -> Unit): Modifier = composed {
    val density = LocalDensity.current
    val thresholdPx = with(density) { DRAG_DISMISS_THRESHOLD.toPx() }
    var dragY by remember { mutableFloatStateOf(0f) }

    this
        .graphicsLayer {
            translationY = dragY.coerceAtLeast(0f)
            alpha = 1f - (dragY / thresholdPx).coerceIn(0f, 0.7f)
        }
        .pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragEnd = {
                    if (dragY > thresholdPx) {
                        onDismiss()
                    }
                    dragY = 0f
                },
                onDragCancel = { dragY = 0f },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragY = (dragY + dragAmount.y).coerceAtLeast(0f)
                }
            )
        }
}
