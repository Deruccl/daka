package com.timemark.app.core.ui.components.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 水波纹扩散动画时长（毫秒）
 *
 * 点击后从点击位置扩散的半透明圆环动画时长。
 * 使用 EaseOut 缓动，模拟水波自然衰减。
 */
private const val RIPPLE_DURATION_MS = 600

/**
 * 水波纹最大半径系数
 *
 * 水波纹扩散的最大半径相对于组件最大边长的比例。
 * 1.5f 确保波纹能覆盖整个卡片并稍微溢出。
 */
private const val RIPPLE_MAX_RADIUS_FACTOR = 1.5f

/**
 * 水波纹环线宽度（dp）
 */
private val RIPPLE_STROKE_WIDTH = 2.dp

/**
 * 水波纹状态持有者
 *
 * 管理水波纹的点击位置与动画进度。
 * 支持多次点击（每次点击重置动画）。
 */
class RippleState {
    /** 当前波纹的点击位置（相对于组件左上角） */
    var origin: Offset by mutableStateOf(Offset.Zero)
        private set

    /** 动画进度 0f..1f */
    var progress: Float by mutableStateOf(0f)
        private set

    /** 是否正在播放动画 */
    var isAnimating: Boolean by mutableStateOf(false)
        private set

    private val animatable = Animatable(0f)

    /**
     * 触发水波纹动画
     *
     * @param origin 点击位置
     */
    suspend fun start(origin: Offset) {
        this.origin = origin
        isAnimating = true
        animatable.snapTo(0f)
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = RIPPLE_DURATION_MS,
                easing = EaseOut
            )
        ) {
            progress = value
        }
        isAnimating = false
    }
}

/**
 * 创建并记住水波纹状态
 */
@Composable
fun rememberRippleState(): RippleState = remember { RippleState() }

/**
 * 水波纹点击手势 Modifier
 *
 * 拦截点击事件，记录点击位置并触发水波纹动画。
 *
 * @param state 水波纹状态
 * @param enabled 是否启用
 */
fun Modifier.rippleClick(
    state: RippleState,
    enabled: Boolean = true
): Modifier = if (!enabled) {
    this
} else {
    composed {
        val scope = rememberCoroutineScope()
        this.pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    // 在协程中启动水波纹动画
                    scope.launch { state.start(offset) }
                }
            )
        }
    }
}

/**
 * 水波纹绘制层
 *
 * 根据当前波纹状态绘制扩散的半透明圆环。
 * 应作为 Box 的子组件叠加在内容之上。
 *
 * @param state 水波纹状态
 * @param color 波纹颜色
 * @param modifier 修饰符
 */
@Composable
fun RippleEffectOverlay(
    state: RippleState,
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.4f)
) {
    // 仅在动画进行中或刚结束时绘制
    if (!state.isAnimating && state.progress >= 1f) return

    Canvas(modifier = modifier) {
        val maxRadius = size.maxDimension * RIPPLE_MAX_RADIUS_FACTOR
        val currentRadius = maxRadius * state.progress
        // 透明度随进度衰减
        val currentAlpha = (1f - state.progress) * color.alpha

        if (currentRadius > 0f && currentAlpha > 0f) {
            // 外层波纹
            drawCircle(
                color = color.copy(alpha = currentAlpha),
                radius = currentRadius,
                center = state.origin,
                style = Stroke(width = RIPPLE_STROKE_WIDTH.toPx())
            )

            // 内层波纹（更淡），增加层次感
            drawCircle(
                color = color.copy(alpha = currentAlpha * 0.5f),
                radius = currentRadius * 0.7f,
                center = state.origin,
                style = Stroke(width = RIPPLE_STROKE_WIDTH.toPx() * 0.5f)
            )
        }
    }
}
