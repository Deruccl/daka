package com.timemark.app.core.ui.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * 高级视觉效果集合
 *
 * 提供视差滚动、卡片悬停、玻璃折射偏移等高级动画效果。
 * 所有效果均基于 graphicsLayer 实现，保证性能。
 */

/** 悬停上浮高度（dp） */
private val HOVER_ELEVATION = 3.dp

/** 悬停缩放比例 */
private const val HOVER_SCALE = 1.02f

/** 悬停动画时长（毫秒） */
private const val HOVER_DURATION_MS = 200

/** 折射偏移最大值（像素），1-2dp */
private const val REFRACTION_OFFSET_MAX_PX = 2f

/**
 * 视差滚动效果
 *
 * 前景快背景慢，根据滚动位置以不同速度移动背景元素。
 * 用于首页背景与内容的不同滚动速度，增强空间纵深感。
 *
 * 原理：根据 LazyListState 的 scrollOffset 计算偏移量，
 * 通过 graphicsLayer 的 translationY 实现位移。
 *
 * @param ratio 视差比率，0f=不动，1f=与滚动同速
 *  - 背景元素使用较小比率（如 0.3f）
 *  - 前景元素使用较大比率（如 1f）
 * @param scrollState 滚动状态
 */
@Composable
fun Modifier.parallaxScroll(
    ratio: Float,
    scrollState: LazyListState
): Modifier {
    // 获取第一个可见项的偏移量
    val scrollOffset = if (scrollState.firstVisibleItemIndex >= 0) {
        scrollState.firstVisibleItemScrollOffset.toFloat()
    } else {
        0f
    }

    val parallaxOffset = scrollOffset * ratio

    return this.graphicsLayer {
        translationY = -parallaxOffset
    }
}

/**
 * 视差滚动效果（基于原始偏移值）
 *
 * 适用于非 LazyList 的滚动场景，直接传入滚动偏移量。
 *
 * @param ratio 视差比率
 * @param scrollOffset 原始滚动偏移量（像素）
 */
fun Modifier.parallaxScrollByOffset(
    ratio: Float,
    scrollOffset: Float
): Modifier = this.graphicsLayer {
    translationY = -scrollOffset * ratio
}

/**
 * 卡片悬停效果
 *
 * 检测鼠标悬停或长按时，卡片上浮 2-4dp 并缩放 1.02f，200ms 动画。
 * 适用于桌面端鼠标悬停或移动端长按预览。
 *
 * 实现原理：
 * - 使用 pointerInput 检测按压状态（移动端长按等效悬停）
 * - animateFloatAsState 平滑过渡上浮与缩放
 *
 * @param enabled 是否启用悬停效果
 */
fun Modifier.hoverElevation(
    enabled: Boolean = true
): Modifier = if (!enabled) {
    this
} else {
    composed {
        var isHovered by remember { mutableStateOf(false) }

        // 悬停/按压时的动画值
        val animationProgress by animateFloatAsState(
            targetValue = if (isHovered) 1f else 0f,
            animationSpec = tween(durationMillis = HOVER_DURATION_MS),
            label = "hoverElevation"
        )

        // 计算上浮高度（像素）
        val elevationPx = with(androidx.compose.ui.platform.LocalDensity.current) {
            HOVER_ELEVATION.toPx()
        }

        this
            .graphicsLayer {
                // 上浮：translationY 负方向
                translationY = -elevationPx * animationProgress
                // 缩放
                scaleX = 1f + (HOVER_SCALE - 1f) * animationProgress
                scaleY = 1f + (HOVER_SCALE - 1f) * animationProgress
                // 阴影（通过 translationZ 模拟）
                shadowElevation = elevationPx * animationProgress * 2f
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHovered = true
                        tryAwaitRelease()
                        isHovered = false
                    }
                )
            }
    }
}

/**
 * 玻璃折射偏移效果
 *
 * 卡片移动时，背景内容有 1-2dp 的偏移，模拟光线折射。
 * 使用 graphicsLayer 实现，性能友好。
 *
 * 原理：根据偏移量对内容进行微小位移，模拟光线穿过玻璃时的折射。
 *
 * @param offsetX X 方向偏移量（像素）
 * @param offsetY Y 方向偏移量（像素）
 */
fun Modifier.glassRefractionOffset(
    offsetX: Float,
    offsetY: Float
): Modifier = this.graphicsLayer {
    // 限制偏移量在 1-2dp 范围内，避免过度变形
    val clampedX = offsetX.coerceIn(-REFRACTION_OFFSET_MAX_PX, REFRACTION_OFFSET_MAX_PX)
    val clampedY = offsetY.coerceIn(-REFRACTION_OFFSET_MAX_PX, REFRACTION_OFFSET_MAX_PX)
    translationX = clampedX
    translationY = clampedY
}

/**
 * 玻璃折射偏移效果（带动画）
 *
 * 与 [glassRefractionOffset] 类似，但偏移量通过动画平滑过渡。
 * 适用于卡片移动时需要平滑折射效果的场景。
 *
 * @param offsetX 目标 X 方向偏移量（像素）
 * @param offsetY 目标 Y 方向偏移量（像素）
 */
@Composable
fun Modifier.animatedGlassRefractionOffset(
    offsetX: Float,
    offsetY: Float
): Modifier {
    val animatedX by animateFloatAsState(
        targetValue = offsetX.coerceIn(-REFRACTION_OFFSET_MAX_PX, REFRACTION_OFFSET_MAX_PX),
        animationSpec = tween(durationMillis = 200),
        label = "refractionX"
    )
    val animatedY by animateFloatAsState(
        targetValue = offsetY.coerceIn(-REFRACTION_OFFSET_MAX_PX, REFRACTION_OFFSET_MAX_PX),
        animationSpec = tween(durationMillis = 200),
        label = "refractionY"
    )

    return this.graphicsLayer {
        translationX = animatedX
        translationY = animatedY
    }
}
