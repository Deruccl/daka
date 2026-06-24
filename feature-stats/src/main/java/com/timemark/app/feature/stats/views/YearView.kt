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
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 年视图
 *
 * 展示选中年的统计：
 * - GitHub 风格年度热力图（12 个月按行排列）
 * - 月度对比柱状图
 * - 年度总结（全年总打卡次数、平均完成率、最长连续、年度关键词）
 */
@Composable
fun YearView(viewModel: StatsViewModel) {
    val rangeStats by viewModel.rangeStats.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val primaryColor = MaterialTheme.colorScheme.primary

    val stats = rangeStats
    val year = selectedDate.year

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
                    description = "请先选择一个打卡项目查看年统计"
                )
            }
            return@LazyColumn
        }

        // 年度热力图
        item {
            ChartCard(title = "${year}年 热力图") {
                val heatData = remember(stats, year) {
                    buildYearHeatData(stats.dailyValues, year)
                }
                HeatMapView(
                    data = heatData,
                    granularity = HeatMapGranularity.YEAR,
                    // 点击某一天：进入月视图
                    onDayClick = { date ->
                        viewModel.enterFinerView(date, TimeViewLevel.YEAR)
                    },
                    height = 280.dp,
                    baseColor = primaryColor
                )
            }
        }

        // 月度对比柱状图
        item {
            ChartCard(title = "月度对比") {
                val barItems = remember(stats, year) {
                    buildYearBarItems(stats.dailyValues, year)
                }
                BarChartView(
                    items = barItems,
                    defaultColor = primaryColor,
                    height = 160.dp
                )
            }
        }

        // 年度总结
        item {
            YearSummaryCard(stats = stats, year = year)
        }
    }
}

/**
 * 构建年热力图数据
 */
private fun buildYearHeatData(
    dailyValues: List<DailyValue>,
    year: Int
): Map<LocalDate, Float> {
    val valueMap = dailyValues.associate {
        LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE) to it
    }
    val result = mutableMapOf<LocalDate, Float>()
    val yearObj = Year.of(year)
    for (day in 1..yearObj.length()) {
        val date = yearObj.atDay(day)
        val dv = valueMap[date]
        if (dv != null) {
            result[date] = if (dv.completed) 1f else 0.5f
        }
    }
    return result
}

/**
 * 构建年柱状图项：按月聚合
 */
private fun buildYearBarItems(
    dailyValues: List<DailyValue>,
    year: Int
): List<BarItem> {
    val monthlyTotals = DoubleArray(12)
    dailyValues.forEach { dv ->
        val date = LocalDate.parse(dv.date, DateTimeFormatter.ISO_DATE)
        if (date.year == year) {
            monthlyTotals[date.monthValue - 1] += dv.value
        }
    }
    return (0 until 12).map { i ->
        BarItem(
            label = "${i + 1}月",
            value = monthlyTotals[i].toFloat()
        )
    }
}

/**
 * 年度总结卡片
 */
@Composable
private fun YearSummaryCard(
    stats: RangeStats,
    year: Int
) {
    GlassCardLevelLight {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${year}年 总结",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatRow(label = "全年总打卡次数", value = "${stats.totalCount} 次")
            StatRow(label = "全年总值", value = formatVal(stats.totalValue))
            StatRow(label = "日均", value = formatVal(stats.avgValue))
            StatRow(label = "平均完成率", value = "${(stats.completionRate * 100).toInt()}%")
            StatRow(label = "完成天数", value = "${stats.completedDays} / ${stats.totalDays} 天")
            StatRow(label = "最长连续", value = "${stats.streak} 天")
            StatRow(label = "最高单日", value = formatVal(stats.maxValue))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = yearKeyword(stats),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 根据统计生成年度关键词
 */
private fun yearKeyword(stats: RangeStats): String {
    val rate = stats.completionRate
    return when {
        stats.totalDays == 0 -> "今年还没有打卡记录，开始你的第一个打卡吧"
        rate >= 0.8f -> "年度关键词：自律达人 🏆 这一年你坚持得非常棒！"
        rate >= 0.6f -> "年度关键词：坚持之星 ⭐ 保持节奏，明年继续！"
        rate >= 0.4f -> "年度关键词：稳步前行 🌱 已经养成初步习惯，继续加油！"
        rate > 0f -> "年度关键词：起步之年 🌟 已经开始改变，明年更进一步！"
        else -> "今年还没有完成目标，新的一年重新出发吧"
    }
}

// GlassCardLevelLight 已在 WeekView.kt 中定义为 internal 共享函数
