package com.timemark.app.feature.stats.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timemark.app.core.ui.theme.Primary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** 热力图粒度 */
enum class HeatMapGranularity { WEEK, MONTH, YEAR }

/**
 * 热力图
 *
 * 支持 周/月/年 三种粒度：
 * - WEEK：7 天一行
 * - MONTH：完整月历网格（按周分行）
 * - YEAR：12 个月按行排列（GitHub 风格）
 *
 * 颜色深浅表示数值（0-1）的完成度，0 为空（浅灰），1 为最深。
 *
 * @param data 日期 -> 完成度 0..1
 * @param modifier 修饰符
 * @param granularity 粒度
 * @param onDayClick 点击日期回调
 * @param baseColor 基础颜色（最深色）
 * @param height 整体高度
 */
@Composable
fun HeatMapView(
    data: Map<LocalDate, Float>,
    modifier: Modifier = Modifier,
    granularity: HeatMapGranularity = HeatMapGranularity.MONTH,
    onDayClick: (LocalDate) -> Unit = {},
    baseColor: Color = Primary,
    height: Dp = 200.dp
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
    )
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(granularity, data) {
                    detectTapGestures { offset ->
                        val cell = hitTestCell(
                            offset, granularity,
                            size.width.toFloat(), size.height.toFloat()
                        )
                        cell?.let { onDayClick(it) }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            when (granularity) {
                HeatMapGranularity.WEEK -> drawWeekHeatMap(
                    data, w, h, baseColor, emptyColor, textMeasurer, labelStyle
                )
                HeatMapGranularity.MONTH -> drawMonthHeatMap(
                    data, w, h, baseColor, emptyColor, textMeasurer, labelStyle
                )
                HeatMapGranularity.YEAR -> drawYearHeatMap(
                    data, w, h, baseColor, emptyColor, textMeasurer, labelStyle
                )
            }
        }
    }
}

/** 绘制周热力图：7 天一行 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWeekHeatMap(
    data: Map<LocalDate, Float>,
    w: Float,
    h: Float,
    baseColor: Color,
    emptyColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    labelStyle: TextStyle
) {
    val today = LocalDate.now()
    val monday = today.with(DayOfWeek.MONDAY)
    val cellSize = h * 0.7f
    val gap = 6f
    val startX = (w - (cellSize * 7 + gap * 6)) / 2f
    val startY = (h - cellSize) / 2f
    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")

    for (i in 0 until 7) {
        val date = monday.plusDays(i.toLong())
        val value = data[date] ?: 0f
        val color = if (data.containsKey(date)) baseColor.copy(alpha = 0.3f + value * 0.7f)
        else emptyColor
        val x = startX + i * (cellSize + gap)
        drawRoundRect(
            color = color,
            topLeft = Offset(x, startY),
            size = Size(cellSize, cellSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        // 标签
        val label = weekDays[i]
        val measured = textMeasurer.measure(label, labelStyle)
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                x + cellSize / 2f - measured.size.width / 2f,
                startY + cellSize / 2f - measured.size.height / 2f
            )
        )
    }
}

/** 绘制月热力图：完整月历网格 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMonthHeatMap(
    data: Map<LocalDate, Float>,
    w: Float,
    h: Float,
    baseColor: Color,
    emptyColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    labelStyle: TextStyle
) {
    val today = LocalDate.now()
    val yearMonth = YearMonth.from(today)
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // 周一为一周开始
    val firstDayOffset = (firstDay.dayOfWeek.value - 1)
    val rows = ((firstDayOffset + daysInMonth + 6) / 7)

    val gap = 4f
    val labelHeight = 14f
    val availableH = h - labelHeight
    val cellSize = minOf(
        (w - gap * 6) / 7,
        (availableH - gap * (rows - 1)) / rows
    )
    val totalW = cellSize * 7 + gap * 6
    val totalH = cellSize * rows + gap * (rows - 1)
    val startX = (w - totalW) / 2f
    val startY = labelHeight + (availableH - totalH) / 2f

    // 周标签
    val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")
    for (i in 0 until 7) {
        val measured = textMeasurer.measure(weekLabels[i], labelStyle)
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                startX + i * (cellSize + gap) + cellSize / 2f - measured.size.width / 2f,
                0f
            )
        )
    }

    // 日期格子
    for (day in 1..daysInMonth) {
        val index = firstDayOffset + day - 1
        val row = index / 7
        val col = index % 7
        val date = yearMonth.atDay(day)
        val value = data[date] ?: 0f
        val color = if (data.containsKey(date)) baseColor.copy(alpha = 0.3f + value * 0.7f)
        else emptyColor
        val x = startX + col * (cellSize + gap)
        val y = startY + row * (cellSize + gap)
        drawRoundRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(cellSize, cellSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // 日期数字
        val dayLabel = day.toString()
        val measured = textMeasurer.measure(dayLabel, labelStyle)
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                x + cellSize / 2f - measured.size.width / 2f,
                y + cellSize / 2f - measured.size.height / 2f
            )
        )
    }
}

/** 绘制年热力图：12 个月按行排列（GitHub 风格） */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawYearHeatMap(
    data: Map<LocalDate, Float>,
    w: Float,
    h: Float,
    baseColor: Color,
    emptyColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    labelStyle: TextStyle
) {
    val today = LocalDate.now()
    val year = today.year
    // 12 个月，3 行 4 列
    val rows = 3
    val cols = 4
    val gap = 8f
    val labelHeight = 14f
    val cellW = (w - gap * (cols - 1)) / cols
    val cellH = (h - labelHeight - gap * (rows - 1)) / rows

    for (month in 1..12) {
        val row = (month - 1) / cols
        val col = (month - 1) % cols
        val x = col * (cellW + gap)
        val y = row * (cellH + gap) + labelHeight
        // 月份标签
        val monthLabel = "${month}月"
        val measured = textMeasurer.measure(monthLabel, labelStyle)
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(x, y - labelHeight)
        )
        // 月份内的小格子（简化为 7x5 的网格表示一个月）
        val yearMonth = YearMonth.of(year, month)
        val daysInMonth = yearMonth.lengthOfMonth()
        val innerGap = 1f
        val innerCols = 7
        val innerRows = (daysInMonth + innerCols - 1) / innerCols
        val innerCellSize = minOf(
            (cellW - innerGap * (innerCols - 1)) / innerCols,
            (cellH - innerGap * (innerRows - 1)) / innerRows
        )
        val innerStartX = x + (cellW - innerCellSize * innerCols - innerGap * (innerCols - 1)) / 2f
        val innerStartY = y + (cellH - innerCellSize * innerRows - innerGap * (innerRows - 1)) / 2f

        for (day in 1..daysInMonth) {
            val index = day - 1
            val r = index / innerCols
            val c = index % innerCols
            val date = yearMonth.atDay(day)
            val value = data[date] ?: 0f
            val color = if (data.containsKey(date)) baseColor.copy(alpha = 0.3f + value * 0.7f)
            else emptyColor
            drawRect(
                color = color,
                topLeft = Offset(
                    innerStartX + c * (innerCellSize + innerGap),
                    innerStartY + r * (innerCellSize + innerGap)
                ),
                size = Size(innerCellSize, innerCellSize)
            )
        }
    }
}

/**
 * 点击命中测试：根据粒度反推被点击的日期
 * 简化实现：根据粒度与点击位置估算日期
 */
private fun hitTestCell(
    offset: Offset,
    granularity: HeatMapGranularity,
    w: Float,
    h: Float
): LocalDate? {
    val today = LocalDate.now()
    return when (granularity) {
        HeatMapGranularity.WEEK -> {
            val cellSize = h * 0.7f
            val gap = 6f
            val startX = (w - (cellSize * 7 + gap * 6)) / 2f
            val startY = (h - cellSize) / 2f
            if (offset.y in startY..(startY + cellSize)) {
                val index = ((offset.x - startX) / (cellSize + gap)).toInt()
                if (index in 0..6) {
                    today.with(DayOfWeek.MONDAY).plusDays(index.toLong())
                } else null
            } else null
        }
        HeatMapGranularity.MONTH -> {
            val yearMonth = YearMonth.from(today)
            val firstDay = yearMonth.atDay(1)
            val firstDayOffset = (firstDay.dayOfWeek.value - 1)
            val daysInMonth = yearMonth.lengthOfMonth()
            val rows = ((firstDayOffset + daysInMonth + 6) / 7)
            val gap = 4f
            val labelHeight = 14f
            val availableH = h - labelHeight
            val cellSize = minOf(
                (w - gap * 6) / 7,
                (availableH - gap * (rows - 1)) / rows
            )
            val totalW = cellSize * 7 + gap * 6
            val totalH = cellSize * rows + gap * (rows - 1)
            val startX = (w - totalW) / 2f
            val startY = labelHeight + (availableH - totalH) / 2f
            val col = ((offset.x - startX) / (cellSize + gap)).toInt()
            val row = ((offset.y - startY) / (cellSize + gap)).toInt()
            if (col in 0..6 && row in 0 until rows) {
                val index = row * 7 + col - firstDayOffset + 1
                if (index in 1..daysInMonth) yearMonth.atDay(index) else null
            } else null
        }
        HeatMapGranularity.YEAR -> {
            // 年视图点击命中较复杂，简化处理：返回今天
            today
        }
    }
}
