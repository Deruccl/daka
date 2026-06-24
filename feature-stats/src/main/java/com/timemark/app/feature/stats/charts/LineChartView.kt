package com.timemark.app.feature.stats.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

/**
 * 折线图
 *
 * 使用 Compose Canvas 自定义绘制：
 * - 网格线（可选）
 * - 平滑曲线（贝塞尔近似）
 * - 渐变填充
 * - 数据点（可选）
 * - X 轴标签（可选）
 *
 * @param data 数据点列表
 * @param modifier 修饰符
 * @param color 曲线颜色
 * @param fillColor 填充颜色（自动应用透明度）
 * @param showDots 是否显示数据点
 * @param showGrid 是否显示网格
 * @param labels X 轴标签（与 data 等长，可选）
 * @param height 图表高度
 */
@Composable
fun LineChartView(
    data: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = color.copy(alpha = 0.2f),
    showDots: Boolean = true,
    showGrid: Boolean = true,
    labels: List<String>? = null,
    height: androidx.compose.ui.unit.Dp = 160.dp
) {
    if (data.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = TextStyle(color = labelColor, fontSize = 10.sp)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(top = 8.dp)
        ) {
            val w = size.width
            val h = size.height
            // 留出顶部空间与底部标签空间
            val topPadding = 16f
            val bottomPadding = if (labels != null) 24f else 8f
            val leftPadding = 8f
            val rightPadding = 8f
            val chartW = w - leftPadding - rightPadding
            val chartH = h - topPadding - bottomPadding

            val maxValue = max(data.max(), 0.0001f)
            val minValue = 0f
            val range = (maxValue - minValue).coerceAtLeast(0.0001f)

            // 网格线（4 条水平线）
            if (showGrid) {
                val gridCount = 4
                for (i in 0..gridCount) {
                    val y = topPadding + chartH * (1f - i.toFloat() / gridCount)
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPadding, y),
                        end = Offset(w - rightPadding, y),
                        strokeWidth = 1f
                    )
                }
            }

            // 计算每个点的坐标
            val pointCount = data.size
            val stepX = if (pointCount > 1) chartW / (pointCount - 1) else 0f
            val points = data.mapIndexed { index, value ->
                val x = leftPadding + index * stepX
                val y = topPadding + chartH * (1f - (value - minValue) / range)
                Offset(x, y)
            }

            // 平滑曲线路径（使用相邻点的中点作为控制点近似贝塞尔）
            val linePath = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val midX = (prev.x + curr.x) / 2f
                        cubicTo(
                            midX, prev.y,
                            midX, curr.y,
                            curr.x, curr.y
                        )
                    }
                }
            }

            // 渐变填充区域
            if (points.size >= 2) {
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(points.last().x, topPadding + chartH)
                    lineTo(points.first().x, topPadding + chartH)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillColor, Color.Transparent),
                        startY = topPadding,
                        endY = topPadding + chartH
                    )
                )
            }

            // 曲线
            drawPath(
                path = linePath,
                color = color,
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 数据点
            if (showDots) {
                points.forEach { p ->
                    drawCircle(
                        color = color,
                        radius = 4f,
                        center = p
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2f,
                        center = p
                    )
                }
            }

            // X 轴标签
            if (labels != null && labels.isNotEmpty()) {
                val labelCount = minOf(labels.size, pointCount)
                for (i in 0 until labelCount) {
                    val x = leftPadding + i * stepX
                    val measured = textMeasurer.measure(labels[i], labelStyle)
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            x - measured.size.width / 2f,
                            h - bottomPadding + 4f
                        )
                    )
                }
            }
        }
    }
}
