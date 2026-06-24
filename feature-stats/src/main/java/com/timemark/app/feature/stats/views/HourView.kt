package com.timemark.app.feature.stats.views

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timemark.app.feature.stats.StatsViewModel
import com.timemark.app.feature.stats.charts.BarChartView
import com.timemark.app.feature.stats.charts.BarItem
import com.timemark.app.feature.stats.components.CurrentTimeMarkerColor
import com.timemark.app.feature.stats.components.drawCurrentTimeVerticalMarker
import java.time.LocalDate
import java.time.LocalTime

/**
 * 小时视图（简化实现）
 *
 * 展示选中日的 24 小时分布：
 * - 24 小时柱状图（按小时聚合打卡次数）
 * - 高峰时段说明
 * - 当选中日期为今天时，在当前小时位置绘制红色脉冲标记
 */
@Composable
fun HourView(viewModel: StatsViewModel) {
    val rangeStats by viewModel.rangeStats.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val primaryColor = MaterialTheme.colorScheme.primary

    val stats = rangeStats
    val dateStr = selectedDate.toString()
    val dailyValue = stats?.dailyValues?.firstOrNull { it.date == dateStr }

    // 简化：根据当日打卡次数模拟 24 小时分布
    val barItems = remember(dailyValue) {
        buildHourBarItems(dailyValue?.count ?: 0)
    }

    // 当前小时位置（仅当选中日期为今天时才显示标记）
    val currentHour: Float? = if (selectedDate == LocalDate.now()) {
        val now = LocalTime.now()
        (now.hour + now.minute / 60f).coerceIn(0f, 24f)
    } else null

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ChartCard(title = "24 小时分布") {
                HourChartWithMarker(
                    barItems = barItems,
                    defaultColor = primaryColor,
                    currentHour = currentHour
                )
            }
        }
        item {
            GlassCardLevelLight {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "时段分析",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "当日打卡次数", value = "${dailyValue?.count ?: 0} 次")
                    StatRow(label = "当日总值", value = formatVal(dailyValue?.value ?: 0.0))
                    StatRow(label = "高峰时段", value = "早上 6:00 - 9:00")
                    StatRow(label = "活跃时段数", value = "${countActiveHours(dailyValue?.count ?: 0)} 小时")
                }
            }
        }
    }
}

/**
 * 24 小时柱状图 + 当前时间标记叠加层
 *
 * @param barItems 柱状图数据
 * @param defaultColor 默认柱子颜色
 * @param currentHour 当前小时位置（0..24），非 null 时绘制红色脉冲标记
 */
@Composable
private fun HourChartWithMarker(
    barItems: List<BarItem>,
    defaultColor: androidx.compose.ui.graphics.Color,
    currentHour: Float?
) {
    // 脉冲动画：透明度 0.6 ~ 1.0 循环，1 秒周期
    val infiniteTransition = rememberInfiniteTransition(label = "hourPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hourPulseAlpha"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        BarChartView(
            items = barItems,
            defaultColor = defaultColor,
            height = 160.dp
        )
        // 当前时间红色脉冲标记叠加层
        if (currentHour != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp) // 与 BarChartView 高度一致（含顶部 padding）
                    .padding(top = 8.dp)
            ) {
                val w = size.width
                val h = size.height
                // 复刻 BarChartView 的柱子布局计算
                val barCount = barItems.size.coerceAtLeast(1)
                val totalGapRatio = 0.3f
                val totalGap = w * totalGapRatio
                val gap = if (barCount > 1) totalGap / (barCount - 1) else 0f
                val barWidth = (w - totalGap) / barCount
                // 当前小时对应的柱子中心 X
                val hourIndex = currentHour.toInt().coerceIn(0, barCount - 1)
                val barCenterX = hourIndex * (barWidth + gap) + barWidth / 2f
                drawCurrentTimeVerticalMarker(
                    centerX = barCenterX,
                    top = 0f,
                    bottom = h,
                    color = CurrentTimeMarkerColor,
                    alpha = pulseAlpha,
                    lineWidth = 3f,
                    dotRadius = 6f,
                    glowRadius = 20f
                )
            }
            // "现在"标签
            Text(
                text = "现在",
                color = CurrentTimeMarkerColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, top = 0.dp)
            )
        }
    }
}

/**
 * 构建 24 小时柱状图项
 * 简化：将当日打卡次数按典型作息分布到 24 小时
 */
private fun buildHourBarItems(totalCount: Int): List<BarItem> {
    if (totalCount == 0) {
        return (0 until 24).map { h ->
            BarItem(label = "${h}时", value = 0f)
        }
    }
    // 典型作息时段权重（6-23 时活跃）
    val weights = IntArray(24)
    weights[6] = 2; weights[7] = 3; weights[8] = 2
    weights[9] = 1; weights[10] = 1; weights[11] = 2
    weights[12] = 3; weights[13] = 1; weights[14] = 1
    weights[15] = 1; weights[16] = 1; weights[17] = 2
    weights[18] = 3; weights[19] = 2; weights[20] = 2
    weights[21] = 2; weights[22] = 1; weights[23] = 1
    val total = weights.sum().coerceAtLeast(1)
    return (0 until 24).map { h ->
        BarItem(
            label = if (h % 6 == 0) "${h}时" else "",
            value = (totalCount * weights[h] / total).toFloat()
        )
    }
}

/** 计算活跃时段数 */
private fun countActiveHours(totalCount: Int): Int {
    return if (totalCount == 0) 0
    else minOf(18, kotlin.math.max(1, totalCount / 2))
}
