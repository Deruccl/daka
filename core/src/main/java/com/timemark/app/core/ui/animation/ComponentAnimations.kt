package com.timemark.app.core.ui.animation

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.OvershootInterpolator
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 组件交互动画集合
 *
 * 提供按钮、卡片、开关、进度条等组件的交互动画 Modifier。
 * 所有动画时长遵循设计规范：
 * - 按压缩放 150ms（EaseOut）
 * - 点击回弹 300ms（OvershootInterpolator）
 * - 进度条 Spring 物理动画（StiffnessLow）
 * - 开关切换 200ms
 */

/** 按压缩放目标值 */
private const val PRESS_SCALE_TARGET = 0.95f

/** 按压缩放动画时长 */
private const val PRESS_SCALE_DURATION = 150

/** 回弹过冲目标值 */
private const val BOUNCE_OVERSHOOT = 1.05f

/** 回弹动画时长 */
private const val BOUNCE_DURATION = 300

/** 开关切换动画时长 */
private const val SWITCH_DURATION = 200

/**
 * 按压缩放动画
 *
 * 当 pressed 为 true 时缩放到 0.95f，松开恢复 1f。
 * 使用 EaseOut 缓动，150ms。
 *
 * @param pressed 是否处于按压状态
 */
@Composable
fun Modifier.pressScaleAnimation(pressed: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) PRESS_SCALE_TARGET else 1f,
        animationSpec = tween(
            durationMillis = PRESS_SCALE_DURATION,
            easing = EaseOut
        ),
        label = "pressScale"
    )
    return this.scale(scale)
}

/**
 * 点击回弹效果
 *
 * 点击时先放大到 1.05f（过冲），再回弹到 1f。
 * 使用 OvershootInterpolator 风格的 Spring 动画，300ms。
 *
 * 用法：Modifier.bounceClickEffect() { /* 点击回调 */ }
 *
 * @param onClick 点击回调
 */
fun Modifier.bounceClickEffect(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按压时缩小，松开时过冲回弹
    val scale by animateFloatAsState(
        targetValue = if (isPressed) PRESS_SCALE_TARGET else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounceClick"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * 进度条动画
 *
 * 使用 Spring 物理动画，stiffness = Spring.StiffnessLow，
 * 带有自然的弹性过渡效果。
 *
 * @param target 目标进度值 0..1
 * @return 当前的动画进度值
 */
@Composable
fun animatedProgress(target: Float): Float {
    val clamped = target.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = clamped,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedProgress"
    )
    return animated
}

/**
 * 发光效果
 *
 * 在组件外围绘制柔和的发光光晕，用于选中状态高亮。
 *
 * @param color 发光颜色
 * @param radius 发光半径
 */
fun Modifier.glowEffect(
    color: Color,
    radius: Dp = 12.dp
): Modifier = this.drawBehind {
    val paint = Paint().apply {
        this.color = color
        this.asFrameworkPaint().apply {
            isAntiAlias = true
            setShadowLayer(
                radius.toPx(),
                0f,
                0f,
                color.toArgb()
            )
        }
    }
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            size.height / 2f,
            size.height / 2f,
            paint.asFrameworkPaint()
        )
    }
}

/**
 * 开关切换动画
 *
 * 返回滑块位移比例（0f=关闭，1f=开启）与颜色过渡。
 * 使用 FastOutSlowInEasing，200ms。
 *
 * @param checked 是否开启
 * @return Pair(滑块位移比例, 当前颜色混合值 0f..1f)
 */
@Composable
fun switchToggleAnimation(checked: Boolean): Pair<Float, Float> {
    val thumbPosition by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(
            durationMillis = SWITCH_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "switchThumb"
    )
    val colorBlend by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(
            durationMillis = SWITCH_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "switchColor"
    )
    return Pair(thumbPosition, colorBlend)
}
