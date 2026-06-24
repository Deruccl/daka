package com.timemark.app.feature.stats.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timemark.app.core.ui.components.EmptyState
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.domain.model.DailyValue
import com.timemark.app.feature.stats.StatsViewModel
import com.timemark.app.feature.stats.charts.HeatMapGranularity
import com.timemark.app.feature.stats.charts.HeatMapView
import com.timemark.app.feature.stats.charts.LineChartView
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 周视图
 *
 * 展示选中周的统计：
 * - 7 天热力图（颜色深浅表示完成度）
 * - 趋势折线图
 * - 周统计（总打卡次数、平均完成率、连续达标天数、最佳/最差一天）
 */
@Composable
fun WeekView(viewModel: StatsViewModel) {
    val rangeStats by viewModel.rangeStats.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val trackers by viewModel.trackers.collectAsStateWithLifecycle()
    val selectedTrackerId by viewModel.selectedTrackerId.collectAsStateWithLifecycle()

    val primaryColor = MaterialTheme.colorScheme.primary
    val stats = rangeStats
    val monday = selectedDate.with(DayOfWeek.MONDAY)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (stats == null) {
            item {
                EmptyState(
                    icon = Icons.Filled.Inbox,
                    title = "暂无数据",
                    description = "请先选择一个打卡项目查看周统计"
                )
            }
            return@LazyColumn
        }

        // 7 天热力图
        item {
            ChartCard(title = "本周热力图") {
                val heatData = remember(stats, monday) {
                    buildWeekHeatData(stats.dailyValues, monday)
                }
                HeatMapView(
                    data = heatData,
                    granularity = HeatMapGranularity.WEEK,
                    // 点击某一天：进入日视图
                    onDayClick = { date ->
                        viewModel.enterFinerView(date, TimeViewLevel.WEEK)
                    },
                    height = 80.dp,
                    baseColor = primaryColor
                )
            }
        }

        // 趋势折线图
        item {
            ChartCard(title = "趋势") {
                val lineData = remember(stats, monday) {
                    buildWeekLineData(stats.dailyValues, monday)
                }
                val labels = listOf("一", "二", "三", "四", "五", "六", "日")
                LineChartView(
                    data = lineData,
                    labels = labels,
                    color = primaryColor,
                    height = 140.dp
                )
            }
        }

        // 周统计
        item {
            WeekStatsCard(stats = stats, monday = monday)
        }
    }
}

/**
 * 构建周热力图数据：完成度 0..1
 */
private fun buildWeekHeatData(
    dailyValues: List<DailyValue>,
    monday: LocalDate
): Map<LocalDate, Float> {
    val valueMap = dailyValues.associate {
        LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE) to it
    }
    val result = mutableMapOf<LocalDate, Float>()
    for (i in 0 until 7) {
        val date = monday.plusDays(i.toLong())
        val dv = valueMap[date]
        if (dv != null) {
            // 完成度：已完成 1.0，未完成但有记录 0.5
            result[date] = if (dv.completed) 1f else 0.5f
        }
        // 未记录的不放入 map（显示为空）
    }
    return result
}

/**
 * 构建周折线图数据：每日数值
 */
private fun buildWeekLineData(
    dailyValues: List<DailyValue>,
    monday: LocalDate
): List<Float> {
    val valueMap = dailyValues.associate {
        LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE) to it.value
    }
    return (0 until 7).map { i ->
        val date = monday.plusDays(i.toLong())
        valueMap[date]?.toFloat() ?: 0f
    }
}

/**
 * 周统计卡片
 */
@Composable
private fun WeekStatsCard(
    stats: com.timemark.app.domain.model.RangeStats,
    monday: LocalDate
) {
    val valueMap = stats.dailyValues.associate {
        LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE) to it
    }
    val bestDay = stats.dailyValues.maxByOrNull { it.value }
    val worstDay = stats.dailyValues.filter { it.value > 0 }.minByOrNull { it.value }

    GlassCard(
        level = GlassLevel.LIGHT,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "周统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatRow(label = "总打卡次数", value = "${stats.totalCount} 次")
            StatRow(label = "总数值", value = "${formatVal(stats.totalValue)}")
            StatRow(label = "平均完成率", value = "${(stats.completionRate * 100).toInt()}%")
            StatRow(label = "完成天数", value = "${stats.completedDays} / ${stats.totalDays} 天")
            StatRow(label = "连续达标", value = "${stats.streak} 天")
            if (bestDay != null) {
                StatRow(label = "最佳一天", value = "${bestDay.date} (${formatVal(bestDay.value)})")
            }
            if (worstDay != null) {
                StatRow(label = "最少一天", value = "${worstDay.date} (${formatVal(worstDay.value)})")
            }
        }
    }
}

/**
 * 图表卡片容器
 */
@Composable
internal fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassCard(
        level = GlassLevel.LIGHT,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

/**
 * 统计行：标签 + 值
 */
@Composable
internal fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** 格式化数值 */
internal fun formatVal(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format("%.1f", value)
}

/**
 * 共享：浅色玻璃卡片容器（带标题）
 */
@Composable
internal fun GlassCardLevelLight(content: @Composable () -> Unit) {
    com.timemark.app.core.ui.components.glass.GlassCard(
        level = com.timemark.app.core.ui.components.glass.GlassLevel.LIGHT,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}
