package com.timemark.app.feature.tracker.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timemark.app.core.ui.components.ProgressBar
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.utils.ColorUtils
import com.timemark.app.domain.model.DailyStats
import com.timemark.app.domain.model.Tracker
import java.time.LocalDate

/**
 * 今日状态卡片
 *
 * 展示选中日期的核心信息：
 * - 大字号显示当日数值与目标
 * - 目标进度条
 * - 连续打卡天数
 * - 快速操作按钮（+1、添加记录）
 *
 * @param tracker 当前打卡项
 * @param stats 当日统计
 * @param streak 连续打卡天数
 * @param selectedDate 选中的日期
 * @param onQuickAdd 快速 +1 回调
 * @param onAddRecord 添加记录回调
 */
@Composable
fun TodayStatusCard(
    tracker: Tracker,
    stats: DailyStats?,
    streak: Int,
    selectedDate: LocalDate,
    onQuickAdd: () -> Unit,
    onAddRecord: () -> Unit
) {
    val currentValue = stats?.totalValue ?: 0.0
    val targetValue = tracker.targetValue
    val hasTarget = tracker.hasTarget
    val progress = if (hasTarget) {
        (currentValue / targetValue).toFloat().coerceIn(0f, 1f)
    } else 0f
    val isCompleted = stats?.completed == true

    // 解析打卡项颜色，失败回退到主色
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackerColor = remember(tracker.color, primaryColor) {
        runCatching { Color(ColorUtils.parseColor(tracker.color)) }
            .getOrDefault(primaryColor)
    }

    GlassCard(
        level = GlassLevel.STANDARD,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 顶部：图标 + 名称 + 连续天数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 图标
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(trackerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tracker.icon,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tracker.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (tracker.unit.isNotEmpty()) {
                            Text(
                                text = "单位：${tracker.unit}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                // 连续打卡天数
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "连续天数",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streak 天",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }

            // 中部：大字号显示当日数值
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = formatValue(currentValue),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isCompleted) MaterialTheme.colorScheme.tertiary else trackerColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (tracker.unit.isNotEmpty()) {
                    Text(
                        text = tracker.unit,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (hasTarget) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "/ ${formatValue(targetValue)} ${tracker.unit}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // 目标进度条
            if (hasTarget) {
                ProgressBar(
                    progress = progress,
                    height = 12.dp,
                    color = trackerColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isCompleted) "已达成目标 🎉" else "还差 ${formatValue((targetValue - currentValue).coerceAtLeast(0.0))} ${tracker.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCompleted) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isCompleted) MaterialTheme.colorScheme.tertiary
                        else trackerColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 底部：快速操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassButton(
                    text = "+1",
                    onClick = onQuickAdd,
                    type = GlassButtonType.PRIMARY,
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                GlassButton(
                    text = "添加记录",
                    onClick = onAddRecord,
                    type = GlassButtonType.SECONDARY,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** 格式化数值显示：整数去小数，非整数保留 1 位 */
private fun formatValue(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format("%.1f", value)
}
