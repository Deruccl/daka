package com.timemark.app.feature.tracker.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.components.glass.GlassTextField
import com.timemark.app.feature.tracker.TrackerDraft
import com.timemark.app.feature.tracker.TrackerFormActions
import com.timemark.app.feature.tracker.components.ColorPicker
import com.timemark.app.feature.tracker.components.IconPicker

/**
 * 步骤 1：基础设置
 *
 * 包含打卡名称、图标、颜色、单位、目标值、简短描述等基础字段。
 *
 * @param actions 表单操作回调
 * @param draft 当前草稿
 */
@Composable
fun StepBasicSettings(
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
        // 打卡名称
        Column {
            Text(
                text = "打卡名称",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            GlassTextField(
                value = draft.name,
                onValueChange = { actions.updateName(it) },
                placeholder = "如：每日饮水、阅读打卡..."
            )
            if (draft.name.isBlank()) {
                Text(
                    text = "名称不能为空",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }

        // 图标选择
        IconPicker(
            selected = draft.icon,
            onSelect = { actions.updateIcon(it) }
        )

        // 颜色选择
        ColorPicker(
            selected = draft.color,
            onSelect = { actions.updateColor(it) }
        )

        // 打卡单位
        Column {
            Text(
                text = "打卡单位（可选）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            GlassTextField(
                value = draft.unit,
                onValueChange = { actions.updateUnit(it) },
                placeholder = "如：杯、分钟、次、kg、ml..."
            )
        }

        // 每日目标值
        Column {
            Text(
                text = "每日目标值（可选，0 表示无目标）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            GlassTextField(
                value = if (draft.targetValue == 0.0) "" else draft.targetValue.toString(),
                onValueChange = { str ->
                    val v = str.toDoubleOrNull() ?: 0.0
                    actions.updateTargetValue(v)
                },
                placeholder = "如：8、30、100..."
            )
        }

        // 简短描述
        Column {
            Text(
                text = "简短描述（可选）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            GlassTextField(
                value = draft.description,
                onValueChange = { actions.updateDescription(it) },
                placeholder = "描述这个打卡项的目的...",
                singleLine = false
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
