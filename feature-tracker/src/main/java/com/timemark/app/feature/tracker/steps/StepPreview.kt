package com.timemark.app.feature.tracker.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.utils.ColorUtils
import com.timemark.app.domain.model.ReminderFrequency
import com.timemark.app.domain.model.TimePeriod
import com.timemark.app.domain.model.TrackerType
import com.timemark.app.feature.tracker.TrackerDraft

/** 类型显示名 */
private fun TrackerType.displayName(): String = when (this) {
    TrackerType.COUNT -> "计数型"
    TrackerType.DURATION -> "时长型"
    TrackerType.VALUE -> "数值型"
    TrackerType.CHECK -> "勾选型"
    TrackerType.IMAGE_TEXT -> "图文型"
    TrackerType.TIMER -> "计时型"
}

/** 时间段显示名 */
private fun TimePeriod.displayName(): String = when (this) {
    TimePeriod.ALL_DAY -> "全天"
    TimePeriod.MORNING -> "早上"
    TimePeriod.AFTERNOON -> "下午"
    TimePeriod.EVENING -> "晚上"
    TimePeriod.CUSTOM -> "自定义"
}

/** 提醒频率显示名 */
private fun ReminderFrequency.displayName(): String = when (this) {
    ReminderFrequency.DAILY -> "每天"
    ReminderFrequency.WEEKLY -> "每周"
    ReminderFrequency.INTERVAL -> "间隔提醒"
    ReminderFrequency.SMART -> "智能"
}

/**
 * 步骤 3：预览确认
 *
 * 展示打卡卡片预览效果及所有配置摘要，可返回修改。
 *
 * @param draft 当前草稿
 */
@Composable
fun StepPreview(
    draft: TrackerDraft,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "预览效果",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // 打卡卡片预览
        TrackerPreviewCard(draft)

        // 配置摘要
        Text(
            text = "配置摘要",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        GlassCard(
            level = GlassLevel.LIGHT,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow(label = "名称", value = draft.name.ifBlank { "（未设置）" })
                SummaryRow(label = "类型", value = draft.type.displayName())
                SummaryRow(label = "图标", value = draft.icon)
                SummaryRow(label = "颜色", value = draft.color)
                if (draft.unit.isNotBlank()) {
                    SummaryRow(label = "单位", value = draft.unit)
                }
                if (draft.targetValue > 0) {
                    SummaryRow(label = "每日目标", value = "${draft.targetValue} ${draft.unit}")
                }
                if (draft.description.isNotBlank()) {
                    SummaryRow(label = "描述", value = draft.description)
                }
                SummaryRow(label = "时间段", value = draft.timePeriod.displayName())
                SummaryRow(
                    label = "提醒",
                    value = if (draft.reminderEnabled) {
                        buildString {
                            append(draft.reminderTime ?: "未设置时间")
                            append("（")
                            append(draft.reminderFrequency.displayName())
                            append("）")
                        }
                    } else "未启用"
                )
                SummaryRow(label = "首页可见", value = if (draft.isVisible) "是" else "否")
                SummaryRow(label = "AI 分析", value = if (draft.aiEnabled) "启用" else "未启用")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** 打卡卡片预览（模拟首页打卡卡片样式） */
@Composable
private fun TrackerPreviewCard(draft: TrackerDraft) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val iconBackgroundColor = remember(draft.color, primaryColor) {
        runCatching { Color(ColorUtils.parseColor(draft.color)) }
            .getOrDefault(primaryColor)
    }

    GlassCard(
        level = GlassLevel.STANDARD,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = draft.icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 名称和进度
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.name.ifBlank { "打卡名称" },
                    style = MaterialTheme.typography.titleMedium
                )
                if (draft.targetValue > 0) {
                    Text(
                        text = "0/${draft.targetValue} ${draft.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    Text(
                        text = "今日 0 ${draft.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** 摘要行 */
@Composable
private fun SummaryRow(label: String, value: String) {
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
