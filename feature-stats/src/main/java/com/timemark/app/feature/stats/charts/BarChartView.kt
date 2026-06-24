package com.timemark.app.feature.stats.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

/**
 * 柱状图数据项
 */
data class BarItem(
    val label: String,
    val value: Float,
    val color: Color? = null  // null 使用默认主色
)

/**
 * 柱状图
 *
 * 使用 Compose Canvas 自定义绘制：
 * - 渐变柱子
 * - 顶部数值标签
 * - 底部 X 轴标签
 *
 * @param items 数据项列表
 * @param modifier 修饰符
 * @param defaultColor 默认柱子颜色（item.color 为 null 时使用）
 * @param height 图表高度
 */
@Composable
fun BarChartView(
    items: List<BarItem>,
    modifier: Modifier = Modifier,
    defaultColor: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 160.dp
) {
    if (items.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
    )
    val valueStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 10.sp
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(top = 8.dp)
        ) {
            val w = size.width
            val h = size.height
            val topPadding = 16f
            val bottomPadding = 24f
            val chartH = h - topPadding - bottomPadding

            val maxValue = max(items.maxOf { it.value }, 0.0001f)
            val barCount = items.size
            // 柱子宽度与间距
            val totalGapRatio = 0.3f
            val totalGap = w * totalGapRatio
            val gap = if (barCount > 1) totalGap / (barCount - 1) else 0f
            val barWidth = (w - totalGap) / barCount

            items.forEachIndexed { index, item ->
                val x = index * (barWidth + gap)
                val barHeight = (item.value / maxValue) * chartH
                val y = topPadding + chartH - barHeight
                val color = item.color ?: defaultColor

                // 渐变柱子
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.6f)),
                        startY = y,
                        endY = y + barHeight
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 4f, barWidth / 4f)
                )

                // 顶部数值
                val valueText = formatBarValue(item.value)
                val measured = textMeasurer.measure(valueText, valueStyle)
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        x + barWidth / 2f - measured.size.width / 2f,
                        y - measured.size.height - 2f
                    )
                )

                // 底部标签
                val labelMeasured = textMeasurer.measure(item.label, labelStyle)
                drawText(
                    textLayoutResult = labelMeasured,
                    topLeft = Offset(
                        x + barWidth / 2f - labelMeasured.size.width / 2f,
                        h - bottomPadding + 4f
                    )
                )
            }
        }
    }
}

/** 格式化柱状图数值 */
private fun formatBarValue(value: Float): String {
    return if (value % 1.0f == 0f) value.toInt().toString()
    else String.format("%.1f", value)
}
