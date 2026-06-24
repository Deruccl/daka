package com.timemark.app.feature.stats.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 当前时间标记红色（#EF4444） */
val CurrentTimeMarkerColor = Color(0xFFEF4444)

/** 标记线条方向 */
enum class StatsMarkerOrientation {
    /** 竖线：用于横向时间轴 */
    VERTICAL,
    /** 横线：用于纵向时间轴 */
    HORIZONTAL
}

/**
 * 可复用的当前时间标记组件（统计模块专用）
 *
 * 在时间轴上标识"当前时间"位置：
 * - VERTICAL：绘制红色竖线 + 光晕 + 圆点，上方显示"现在"文字
 * - HORIZONTAL：绘制红色横线 + 光晕 + 圆点，右侧显示当前时间文字
 *
 * 脉冲动画：透明度 0.6 → 1.0 循环，1 秒周期。
 * 光晕效果：以标记线为中心绘制径向渐变。
 *
 * @param modifier 修饰符
 * @param orientation 标记方向
 * @param color 标记颜色，默认 #EF4444
 * @param lineWidth 线条宽度
 * @param dotRadius 圆点半径
 * @param glowRadius 光晕半径
 * @param label 可选标签文字（如"现在"）
 */
@Composable
fun CurrentTimeMarker(
    modifier: Modifier = Modifier,
    orientation: StatsMarkerOrientation = StatsMarkerOrientation.VERTICAL,
    color: Color = CurrentTimeMarkerColor,
    lineWidth: Dp = 2.dp,
    dotRadius: Dp = 5.dp,
    glowRadius: Dp = 16.dp,
    label: String? = null
) {
    // 脉冲动画：透明度 0.6 ~ 1.0 循环，1 秒周期
    val infiniteTransition = rememberInfiniteTransition(label = "statsCurrentTimePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "statsPulseAlpha"
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val w = size.width
            val h = size.height
            val linePx = lineWidth.toPx()
            val dotPx = dotRadius.toPx()
            val glowPx = glowRadius.toPx()

            when (orientation) {
                StatsMarkerOrientation.VERTICAL -> {
                    val centerX = w / 2f
                    drawVerticalMarkerWithGlow(
                        centerX = centerX,
                        height = h,
                        lineWidth = linePx,
                        dotRadius = dotPx,
                        glowRadius = glowPx,
                        color = color,
                        alpha = pulseAlpha
                    )
                }
                StatsMarkerOrientation.HORIZONTAL -> {
                    val centerY = h / 2f
                    drawHorizontalMarkerWithGlow(
                        centerY = centerY,
                        width = w,
                        lineWidth = linePx,
                        dotRadius = dotPx,
                        glowRadius = glowPx,
                        color = color,
                        alpha = pulseAlpha
                    )
                }
            }
        }

        // 可选标签
        if (label != null) {
            when (orientation) {
                StatsMarkerOrientation.VERTICAL -> {
                    Text(
                        text = label,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(bottom = 2.dp)
                    )
                }
                StatsMarkerOrientation.HORIZONTAL -> {
                    Text(
                        text = label,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 绘制竖向当前时间标记（含光晕）
 *
 * @param centerX 竖线中心 X
 * @param height 画布高度
 * @param lineWidth 线宽（px）
 * @param dotRadius 圆点半径（px）
 * @param glowRadius 光晕半径（px）
 * @param color 标记颜色
 * @param alpha 透明度（脉冲动画值）
 */
fun DrawScope.drawVerticalMarkerWithGlow(
    centerX: Float,
    height: Float,
    lineWidth: Float,
    dotRadius: Float,
    glowRadius: Float,
    color: Color,
    alpha: Float
) {
    // 光晕：以竖线为中心的径向渐变
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha * 0.35f),
                color.copy(alpha = 0f)
            ),
            radius = glowRadius,
            center = Offset(centerX, height / 2f)
        ),
        radius = glowRadius,
        center = Offset(centerX, height / 2f)
    )

    // 主竖线
    drawLine(
        color = color.copy(alpha = alpha),
        start = Offset(centerX, 0f),
        end = Offset(centerX, height),
        strokeWidth = lineWidth,
        cap = StrokeCap.Round
    )

    // 顶部圆点
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = dotRadius,
        center = Offset(centerX, dotRadius + 2f)
    )
}

/**
 * 绘制横向当前时间标记（含光晕）
 *
 * @param centerY 横线中心 Y
 * @param width 画布宽度
 * @param lineWidth 线宽（px）
 * @param dotRadius 圆点半径（px）
 * @param glowRadius 光晕半径（px）
 * @param color 标记颜色
 * @param alpha 透明度（脉冲动画值）
 */
fun DrawScope.drawHorizontalMarkerWithGlow(
    centerY: Float,
    width: Float,
    lineWidth: Float,
    dotRadius: Float,
    glowRadius: Float,
    color: Color,
    alpha: Float
) {
    // 光晕：以横线为中心的径向渐变
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha * 0.35f),
                color.copy(alpha = 0f)
            ),
            radius = glowRadius,
            center = Offset(width / 2f, centerY)
        ),
        radius = glowRadius,
        center = Offset(width / 2f, centerY)
    )

    // 主横线
    drawLine(
        color = color.copy(alpha = alpha),
        start = Offset(0f, centerY),
        end = Offset(width, centerY),
        strokeWidth = lineWidth,
        cap = StrokeCap.Round
    )

    // 左侧圆点
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = dotRadius,
        center = Offset(dotRadius + 2f, centerY)
    )
}

/**
 * 在 Canvas 中绘制当前时间竖线标记（供其他 Canvas 直接调用，含光晕）
 *
 * @param centerX 竖线中心 X 坐标
 * @param top 竖线顶部 Y
 * @param bottom 竖线底部 Y
 * @param color 标记颜色
 * @param alpha 透明度（用于脉冲动画）
 * @param lineWidth 线宽（px）
 * @param dotRadius 圆点半径（px）
 * @param glowRadius 光晕半径（px）
 */
fun DrawScope.drawCurrentTimeVerticalMarker(
    centerX: Float,
    top: Float,
    bottom: Float,
    color: Color,
    alpha: Float = 1.0f,
    lineWidth: Float = 3f,
    dotRadius: Float = 6f,
    glowRadius: Float = 18f
) {
    val centerY = (top + bottom) / 2f
    // 光晕
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha * 0.3f),
                color.copy(alpha = 0f)
            ),
            radius = glowRadius,
            center = Offset(centerX, centerY)
        ),
        radius = glowRadius,
        center = Offset(centerX, centerY)
    )
    // 主线
    drawLine(
        color = color.copy(alpha = alpha),
        start = Offset(centerX, top),
        end = Offset(centerX, bottom),
        strokeWidth = lineWidth,
        cap = StrokeCap.Round
    )
    // 顶部圆点
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = dotRadius,
        center = Offset(centerX, top + dotRadius)
    )
}

/**
 * 在 Canvas 中绘制当前时间横线标记（供其他 Canvas 直接调用，含光晕）
 */
fun DrawScope.drawCurrentTimeHorizontalMarker(
    centerY: Float,
    left: Float,
    right: Float,
    color: Color,
    alpha: Float = 1.0f,
    lineWidth: Float = 3f,
    dotRadius: Float = 6f,
    glowRadius: Float = 18f
) {
    val centerX = (left + right) / 2f
    // 光晕
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha * 0.3f),
                color.copy(alpha = 0f)
            ),
            radius = glowRadius,
            center = Offset(centerX, centerY)
        ),
        radius = glowRadius,
        center = Offset(centerX, centerY)
    )
    // 主线
    drawLine(
        color = color.copy(alpha = alpha),
        start = Offset(left, centerY),
        end = Offset(right, centerY),
        strokeWidth = lineWidth,
        cap = StrokeCap.Round
    )
    // 左侧圆点
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = dotRadius,
        center = Offset(left + dotRadius, centerY)
    )
}
