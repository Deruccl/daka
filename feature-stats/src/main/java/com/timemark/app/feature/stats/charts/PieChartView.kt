package com.timemark.app.feature.stats.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 饼图数据项
 */
data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

/**
 * 饼图
 *
 * 使用 Compose Canvas 自定义绘制：
 * - 多段饼图
 * - 百分比标签
 * - 颜色区分
 * - 中心显示总值
 *
 * @param data 数据列表
 * @param modifier 修饰符
 * @param size 饼图直径
 */
@Composable
fun PieChartView(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    if (data.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return

    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    )
    val centerStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 饼图本体
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val diameter = this.size.minDimension
                val radius = diameter / 2f
                val center = Offset(radius, radius)
                var startAngle = -90f  // 从顶部开始

                data.forEach { item ->
                    val sweep = (item.value / total) * 360f
                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(0f, 0f),
                        size = Size(diameter, diameter)
                    )
                    // 百分比标签
                    val midAngle = startAngle + sweep / 2f
                    val labelRadius = radius * 0.65f
                    val rad = Math.toRadians(midAngle.toDouble())
                    val labelX = center.x + (labelRadius * cos(rad)).toFloat()
                    val labelY = center.y + (labelRadius * sin(rad)).toFloat()
                    val percent = (item.value / total * 100).toInt()
                    if (sweep > 20f) {  // 仅在扇形足够大时显示标签
                        val measured = textMeasurer.measure("$percent%", labelStyle)
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(
                                labelX - measured.size.width / 2f,
                                labelY - measured.size.height / 2f
                            )
                        )
                    }
                    startAngle += sweep
                }

                // 中心白色圆环（甜甜圈效果）
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.4f,
                    center = center
                )
            }
            // 中心文字
            Text(
                text = formatTotal(total),
                style = centerStyle,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 图例
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            data.forEach { item ->
                val percent = (item.value / total * 100).toInt()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(color = item.color)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.label}  $percent%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** 格式化总值 */
private fun formatTotal(total: Float): String {
    return if (total % 1.0f == 0f) total.toInt().toString()
    else String.format("%.1f", total)
}
