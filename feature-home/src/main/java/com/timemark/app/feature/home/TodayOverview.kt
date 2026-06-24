package com.timemark.app.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel

/**
 * 今日概览
 *
 * 可展开/收起的统计概览：
 * - 完成率
 * - 已完成打卡数 / 总打卡数
 * - 最长连续打卡天数
 * - 今日亮点（根据完成率给出激励文案）
 */
@Composable
fun TodayOverview(state: HomeUiState.Loaded) {
    var expanded by remember { mutableStateOf(false) }
    val maxStreak = state.trackers.maxOfOrNull { it.streak } ?: 0

    GlassCard(
        level = GlassLevel.LIGHT,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题行 + 展开/收起按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日概览",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "收起" else "展开"
                    )
                }
            }

            // 展开内容
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 完成率
                    OverviewItem(
                        label = "完成率",
                        value = "${(state.completionRate * 100).toInt()}%",
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                    // 已完成数
                    OverviewItem(
                        label = "已完成",
                        value = "${state.totalCompleted} / ${state.totalCount}"
                    )
                    // 最长连续打卡
                    if (maxStreak > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "最长连续 $maxStreak 天",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // 今日亮点
                    Text(
                        text = highlightMessage(state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 概览单项：标签 + 值
 */
@Composable
private fun OverviewItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 根据完成率生成激励文案
 */
private fun highlightMessage(state: HomeUiState.Loaded): String = when {
    state.totalCount == 0 -> "今天还没有打卡项目"
    state.completionRate >= 1f -> "太棒了！今日全部完成 🎉"
    state.completionRate >= 0.5f -> "继续保持，已完成过半！"
    state.completionRate > 0f -> "已经开始打卡了，加油！"
    else -> "今天还没开始打卡哦"
}
