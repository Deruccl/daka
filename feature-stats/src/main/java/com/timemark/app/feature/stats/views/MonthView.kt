package com.timemark.app.feature.stats.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timemark.app.core.ui.components.EmptyState
import com.timemark.app.domain.model.DailyValue
import com.timemark.app.domain.model.RangeStats
import com.timemark.app.feature.stats.StatsViewModel
import com.timemark.app.feature.stats.charts.BarChartView
import com.timemark.app.feature.stats.charts.BarItem
import com.timemark.app.feature.stats.charts.HeatMapGranularity
import com.timemark.app.feature.stats.charts.HeatMapView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 月视图
 *
 * 展示选中月的统计：
 * - 月历热力图（完整月历，每天用颜色填充表示完成度）
 * - 月度趋势柱状图（按周聚合）
 * - 月度统计
 */
@Composable
fun MonthView(viewModel: StatsViewModel) {
    val rangeStats by viewModel.rangeStats.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val primaryColor = MaterialTheme.colorScheme.primary

    val stats = rangeStats
    val yearMonth = YearMonth.from(selectedDate)

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
                    description = "请先选择一个打卡项目查看月统计"
                )
            }
            return@LazyColumn
        }

        // 月历热力图
        item {
            ChartCard(title = "${yearMonth.year}年${yearMonth.monthValue}月 热力图") {
                val heatData = remember(stats, yearMonth) {
                    buildMonthHeatData(stats.dailyValues, yearMonth)
                }
                HeatMapView(
                    data = heatData,
                    granularity = HeatMapGranularity.MONTH,
                    // 点击某一天：进入日视图
                    onDayClick = { date ->
                        viewModel.enterFinerView(date, TimeViewLevel.MONTH)
                    },
                    height = 240.dp,
                    baseColor = primaryColor
                )
            }
        }

        // 月度柱状图（按周聚合）
        item {
            ChartCard(title = "按周统计") {
                val barItems = remember(stats, yearMonth) {
                    buildMonthBarItems(stats.dailyValues, yearMonth)
                }
                BarChartView(
                    items = barItems,
                    defaultColor = primaryColor,
                    height = 140.dp
                )
            }
        }

        // 月度统计
        item {
            MonthStatsCard(stats = stats, yearMonth = yearMonth)
        }
    }
}

/**
 * 构建月热力图数据
 */
private fun buildMonthHeatData(
    dailyValues: List<DailyValue>,
    yearMonth: YearMonth
): Map<LocalDate, Float> {
    val valueMap = dailyValues.associate {
        LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE) to it
    }
    val result = mutableMapOf<LocalDate, Float>()
    for (day in 1..yearMonth.lengthOfMonth()) {
        val date = yearMonth.atDay(day)
        val dv = valueMap[date]
        if (dv != null) {
            // 完成度：已完成 1.0，未完成但有记录按值比例（简化为 0.5）
            result[date] = if (dv.completed) 1f else 0.5f
        }
    }
    return result
}

/**
 * 构建月柱状图项：按周聚合
 */
private fun buildMonthBarItems(
    dailyValues: List<DailyValue>,
    yearMonth: YearMonth
): List<BarItem> {
    val valueMap = dailyValues.associate {
        LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE) to it.value
    }
    val firstDay = yearMonth.atDay(1)
    val firstDayOffset = firstDay.dayOfWeek.value - 1  // 周一为 0
    val totalDays = yearMonth.lengthOfMonth()
    val weekCount = ((firstDayOffset + totalDays + 6) / 7)

    return (0 until weekCount).map { weekIndex ->
        val weekValue = (0 until 7).sumOf { dayInWeek ->
            val dayOfYear = weekIndex * 7 + dayInWeek - firstDayOffset + 1
            if (dayOfYear in 1..totalDays) {
                valueMap[yearMonth.atDay(dayOfYear)] ?: 0.0
            } else 0.0
        }
        BarItem(
            label = "第${weekIndex + 1}周",
            value = weekValue.toFloat()
        )
    }
}

/**
 * 月度统计卡片
 */
@Composable
private fun MonthStatsCard(
    stats: RangeStats,
    yearMonth: YearMonth
) {
    GlassCardLevelLight {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${yearMonth.year}年${yearMonth.monthValue}月 统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatRow(label = "总打卡次数", value = "${stats.totalCount} 次")
            StatRow(label = "总数值", value = formatVal(stats.totalValue))
            StatRow(label = "日均", value = formatVal(stats.avgValue))
            StatRow(label = "完成率", value = "${(stats.completionRate * 100).toInt()}%")
            StatRow(label = "完成天数", value = "${stats.completedDays} / ${stats.totalDays} 天")
            StatRow(label = "最高单日", value = formatVal(stats.maxValue))
            if (stats.minValue > 0) {
                StatRow(label = "最低单日", value = formatVal(stats.minValue))
            }
        }
    }
}

// GlassCardLevelLight 已在 WeekView.kt 中定义为 internal 共享函数
