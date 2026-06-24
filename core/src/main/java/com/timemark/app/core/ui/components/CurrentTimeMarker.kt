package com.timemark.app.core.ui.components

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 标记线条方向
 *
 * - VERTICAL：绘制竖线（用于横向时间轴上的当前时间标记）
 * - HORIZONTAL：绘制横线（用于纵向时间轴上的当前时间标记）
 */
enum class MarkerOrientation {
    VERTICAL,
    HORIZONTAL
}

/**
 * 当前时间标记组件
 *
 * 可复用的红色时间标记，用于在时间轴上标识"当前时间"位置。
 *
 * - 横向时间轴（orientation = VERTICAL）：绘制红色竖线 + 圆点
 * - 纵向时间轴（orientation = HORIZONTAL）：绘制红色横线 + 圆点
 *
 * 带有脉冲动画效果：透明度在 0.5 ~ 1.0 之间循环变化，周期 1 秒。
 *
 * @param modifier 修饰符
 * @param orientation 标记方向（VERTICAL=竖线 / HORIZONTAL=横线）
 * @param color 标记颜色，默认红色
 * @param lineWidth 线条宽度
 * @param dotRadius 圆点半径
 * @param label 可选标签文字（如"现在"、"今天"）
 */
@Composable
fun CurrentTimeMarker(
    modifier: Modifier = Modifier,
    orientation: MarkerOrientation = MarkerOrientation.VERTICAL,
    color: Color = Color(0xFFEF4444),
    lineWidth: Dp = 2.dp,
    dotRadius: Dp = 5.dp,
    label: String? = null
) {
    // 脉冲动画：透明度 0.5 ~ 1.0 循环，1 秒周期
    val infiniteTransition = rememberInfiniteTransition(label = "currentTimePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val lineColor = color.copy(alpha = pulseAlpha)
    val dotColor = color
    val lineColorFaded = color.copy(alpha = pulseAlpha * 0.6f)

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

            when (orientation) {
                MarkerOrientation.VERTICAL -> {
                    val centerX = w / 2f
                    drawCurrentTimeVerticalLine(
                        centerX = centerX,
                        height = h,
                        lineWidth = linePx,
                        dotRadius = dotPx,
                        lineColor = lineColor,
                        lineColorFaded = lineColorFaded,
                        dotColor = dotColor
                    )
                }
                MarkerOrientation.HORIZONTAL -> {
                    val centerY = h / 2f
                    drawCurrentTimeHorizontalLine(
                        centerY = centerY,
                        width = w,
                        lineWidth = linePx,
                        dotRadius = dotPx,
                        lineColor = lineColor,
                        lineColorFaded = lineColorFaded,
                        dotColor = dotColor
                    )
                }
            }
        }

        // 可选标签
        if (label != null) {
            val labelStyle = TextStyle(
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            when (orientation) {
                MarkerOrientation.VERTICAL -> {
                    androidx.compose.material3.Text(
                        text = label,
                        style = labelStyle,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(bottom = 2.dp)
                    )
                }
                MarkerOrientation.HORIZONTAL -> {
                    androidx.compose.material3.Text(
                        text = label,
                        style = labelStyle,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(end = 4.dp)
                    )
                }
            }
        }
    }
}

/** 绘制竖向当前时间标记线（用于横向时间轴） */
private fun DrawScope.drawCurrentTimeVerticalLine(
    centerX: Float,
    height: Float,
    lineWidth: Float,
    dotRadius: Float,
    lineColor: Color,
    lineColorFaded: Color,
    dotColor: Color
) {
    drawLine(
        color = lineColor,
        start = Offset(centerX, 0f),
        end = Offset(centerX, height),
        strokeWidth = lineWidth,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = dotColor,
        radius = dotRadius,
        center = Offset(centerX, dotRadius + 2f)
    )
    drawCircle(
        color = lineColorFaded,
        radius = dotRadius * 0.7f,
        center = Offset(centerX, height - dotRadius - 2f)
    )
}

/** 绘制横向当前时间标记线（用于纵向时间轴） */
private fun DrawScope.drawCurrentTimeHorizontalLine(
    centerY: Float,
    width: Float,
    lineWidth: Float,
    dotRadius: Float,
    lineColor: Color,
    lineColorFaded: Color,
    dotColor: Color
) {
    drawLine(
        color = lineColor,
        start = Offset(0f, centerY),
        end = Offset(width, centerY),
        strokeWidth = lineWidth,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = dotColor,
        radius = dotRadius,
        center = Offset(dotRadius + 2f, centerY)
    )
    drawCircle(
        color = lineColorFaded,
        radius = dotRadius * 0.7f,
        center = Offset(width - dotRadius - 2f, centerY)
    )
}

/**
 * 在 Canvas 中绘制当前时间竖线标记（供其他 Canvas 直接调用）
 *
 * @param centerX 竖线中心 X 坐标
 * @param top 竖线顶部 Y
 * @param bottom 竖线底部 Y
 * @param color 标记颜色
 * @param alpha 透明度（用于脉冲动画）
 * @param lineWidth 线宽（px）
 * @param dotRadius 圆点半径（px）
 */
fun DrawScope.drawCurrentTimeVerticalMarker(
    centerX: Float,
    top: Float,
    bottom: Float,
    color: Color,
    alpha: Float = 1.0f,
    lineWidth: Float = 3f,
    dotRadius: Float = 6f
) {
    val lineColor = color.copy(alpha = alpha)
    drawLine(
        color = lineColor,
        start = Offset(centerX, top),
        end = Offset(centerX, bottom),
        strokeWidth = lineWidth,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = dotRadius,
        center = Offset(centerX, top + dotRadius)
    )
    drawCircle(
        color = color.copy(alpha = alpha * 0.7f),
        radius = dotRadius * 0.8f,
        center = Offset(centerX, bottom - dotRadius)
    )
}

/**
 * 在 Canvas 中绘制当前时间横线标记（供其他 Canvas 直接调用）
 */
fun DrawScope.drawCurrentTimeHorizontalMarker(
    centerY: Float,
    left: Float,
    right: Float,
    color: Color,
    alpha: Float = 1.0f,
    lineWidth: Float = 3f,
    dotRadius: Float = 6f
) {
    val lineColor = color.copy(alpha = alpha)
    drawLine(
        color = lineColor,
        start = Offset(left, centerY),
        end = Offset(right, centerY),
        strokeWidth = lineWidth,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = dotRadius,
        center = Offset(left + dotRadius, centerY)
    )
    drawCircle(
        color = color.copy(alpha = alpha * 0.7f),
        radius = dotRadius * 0.8f,
        center = Offset(right - dotRadius, centerY)
    )
}

/**
 * 在 Canvas 中绘制红色边框高亮（用于高亮当前日期/月份单元格）
 *
 * @param topLeft 单元格左上角坐标
 * @param size 单元格尺寸
 * @param color 边框颜色
 * @param alpha 透明度
 * @param strokeWidth 边框线宽（px）
 * @param cornerRadius 圆角半径（px）
 */
fun DrawScope.drawHighlightBorder(
    topLeft: Offset,
    size: Size,
    color: Color,
    alpha: Float = 1.0f,
    strokeWidth: Float = 3f,
    cornerRadius: Float = 6f
) {
    val borderColor = color.copy(alpha = alpha)
    drawRoundRect(
        color = borderColor,
        topLeft = topLeft,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = strokeWidth)
    )
}
