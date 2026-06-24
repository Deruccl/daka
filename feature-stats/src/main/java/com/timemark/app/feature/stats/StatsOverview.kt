package com.timemark.app.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.components.ProgressRing
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.domain.model.RangeStats
import com.timemark.app.domain.model.Tracker

/**
 * 统计概览卡片
 *
 * 展示选中项目在当前时间段的关键指标：
 * - 完成率（环形进度）
 * - 已完成天数 / 总天数
 * - 连续天数
 * - 总值 / 平均 / 最大 / 最小
 *
 * @param stats 范围统计数据
 * @param tracker 选中的打卡项目（用于单位与目标值），null 表示全部
 */
@Composable
fun StatsOverview(
    stats: RangeStats,
    tracker: Tracker?
) {
    val unit = tracker?.unit ?: "次"
    val hasTarget = tracker?.hasTarget == true

    GlassCard(
        level = GlassLevel.STANDARD,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：完成率环形进度
            ProgressRing(
                progress = stats.completionRate,
                size = 80.dp,
                text = "${(stats.completionRate * 100).toInt()}%"
            )
            Spacer(modifier = Modifier.width(16.dp))
            // 右侧：关键指标
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "完成 ${stats.completedDays} / ${stats.totalDays} 天",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (stats.streak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "连续 ${stats.streak} 天",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "总计 ${formatValue(stats.totalValue)} $unit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (stats.totalDays > 0) {
                    Text(
                        text = "日均 ${formatValue(stats.avgValue)} $unit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        // 第二行：最大/最小值（仅有数据时显示）
        if (hasTarget && stats.totalDays > 0) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatPill(label = "最高", value = "${formatValue(stats.maxValue)} $unit")
                StatPill(label = "最低", value = "${formatValue(stats.minValue)} $unit")
                StatPill(label = "记录次数", value = "${stats.totalCount}")
            }
        }
    }
}

/**
 * 小型指标胶囊
 */
@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** 格式化数值：整数显示整数，小数保留 1 位 */
private fun formatValue(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format("%.1f", value)
}
