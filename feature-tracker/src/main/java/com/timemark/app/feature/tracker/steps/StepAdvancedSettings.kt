package com.timemark.app.feature.tracker.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTextField
import com.timemark.app.core.ui.theme.Primary
import com.timemark.app.domain.model.ReminderFrequency
import com.timemark.app.domain.model.TimePeriod
import com.timemark.app.feature.tracker.TrackerDraft
import com.timemark.app.feature.tracker.TrackerFormActions

/** 时间段选项 */
private val TIME_PERIODS: List<Pair<TimePeriod, String>> = listOf(
    TimePeriod.ALL_DAY to "全天",
    TimePeriod.MORNING to "早上",
    TimePeriod.AFTERNOON to "下午",
    TimePeriod.EVENING to "晚上"
)

/** 提醒频率选项 */
private val REMINDER_FREQUENCIES: List<Pair<ReminderFrequency, String>> = listOf(
    ReminderFrequency.DAILY to "每天",
    ReminderFrequency.INTERVAL to "间隔",
    ReminderFrequency.WEEKLY to "每周",
    ReminderFrequency.SMART to "智能"
)

/**
 * 步骤 2：高级设置
 *
 * 包含打卡时间段、提醒设置（开关/时间/频率/间隔）、可见性、AI 分析开关。
 *
 * @param actions 表单操作回调
 * @param draft 当前草稿
 */
@Composable
fun StepAdvancedSettings(
    actions: TrackerFormActions,
    draft: TrackerDraft,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 打卡时间段
        SectionTitle(text = "打卡时间段")
        ChipGroup(
            options = TIME_PERIODS,
            selected = draft.timePeriod,
            onSelect = { actions.updateTimePeriod(it) }
        )

        // 提醒设置
        SectionTitle(text = "提醒设置")
        GlassCard(
            level = GlassLevel.LIGHT,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 提醒开关
                SwitchRow(
                    title = "启用提醒",
                    checked = draft.reminderEnabled,
                    onCheckedChange = { actions.updateReminderEnabled(it) }
                )

                if (draft.reminderEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // 提醒时间
                    Column {
                        Text(
                            text = "提醒时间",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        GlassTextField(
                            value = draft.reminderTime ?: "",
                            onValueChange = { actions.updateReminderTime(it) },
                            placeholder = "HH:mm，如 08:00"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 提醒频率
                    Text(
                        text = "提醒频率",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    ChipGroup(
                        options = REMINDER_FREQUENCIES,
                        selected = draft.reminderFrequency,
                        onSelect = { actions.updateReminderFrequency(it) }
                    )

                    // 间隔提醒时显示间隔小时数
                    if (draft.reminderFrequency == ReminderFrequency.INTERVAL) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column {
                            Text(
                                text = "间隔小时数",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            GlassTextField(
                                value = draft.reminderIntervalHours.toString(),
                                onValueChange = { str ->
                                    val h = str.toIntOrNull() ?: 2
                                    actions.updateReminderInterval(h)
                                },
                                placeholder = "如：2"
                            )
                        }
                    }
                }
            }
        }

        // 可见性 & AI 分析
        SectionTitle(text = "其他设置")
        GlassCard(
            level = GlassLevel.LIGHT,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SwitchRow(
                    title = "首页可见",
                    subtitle = "关闭后将不在首页显示",
                    checked = draft.isVisible,
                    onCheckedChange = { actions.updateVisible(it) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SwitchRow(
                    title = "AI 分析",
                    subtitle = "启用后可使用 AI 分析打卡数据",
                    checked = draft.aiEnabled,
                    onCheckedChange = { actions.updateAiEnabled(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** 小节标题 */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

/**
 * 标签选择组
 *
 * @param options 选项列表（值 -> 显示文本）
 * @param selected 当前选中值
 * @param onSelect 选中回调
 */
@Composable
private fun <T> ChipGroup(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) Primary.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSelect(value) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

/** 开关行 */
@Composable
private fun SwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Primary,
                checkedThumbColor = Color.White
            )
        )
    }
}
