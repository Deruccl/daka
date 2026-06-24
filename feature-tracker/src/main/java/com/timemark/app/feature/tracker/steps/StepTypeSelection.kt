package com.timemark.app.feature.tracker.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.theme.Primary
import com.timemark.app.core.utils.ColorUtils
import com.timemark.app.domain.model.TrackerTemplate
import com.timemark.app.domain.model.TrackerTemplates
import com.timemark.app.domain.model.TrackerType
import com.timemark.app.feature.tracker.TrackerDraft
import com.timemark.app.feature.tracker.TrackerFormActions

/** 打卡类型与说明 */
private data class TypeOption(
    val type: TrackerType,
    val label: String,
    val icon: String,
    val desc: String
)

/** 类型选项列表 */
private val TYPE_OPTIONS: List<TypeOption> = listOf(
    TypeOption(TrackerType.COUNT, "计数型", "🔢", "记录次数，如喝水杯数"),
    TypeOption(TrackerType.DURATION, "时长型", "⏱️", "记录时长，如运动分钟数"),
    TypeOption(TrackerType.VALUE, "数值型", "📊", "记录数值，如体重 kg"),
    TypeOption(TrackerType.CHECK, "勾选型", "✅", "完成/未完成，如早起打卡"),
    TypeOption(TrackerType.IMAGE_TEXT, "图文型", "📸", "图文记录，如饮食日记"),
    TypeOption(TrackerType.TIMER, "计时型", "⏲️", "开始/结束自动计时")
)

/**
 * 步骤 0：类型选择 / 模板
 *
 * 顶部展示推荐模板（横向滚动），下方展示打卡类型选择（卡片网格）。
 * 点击模板会自动填充表单，点击类型仅设置类型字段。
 *
 * @param actions 表单操作回调
 * @param draft 当前草稿
 */
@Composable
fun StepTypeSelection(
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
        // 推荐模板
        Text(
            text = "从模板开始",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(TrackerTemplates.all) { template ->
                TemplateCard(
                    template = template,
                    isSelected = draft.name == template.name && draft.icon == template.icon,
                    onClick = { actions.selectTemplate(template) }
                )
            }
        }

        // 类型选择
        Text(
            text = "或选择类型",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        // 类型网格（2 列）
        val rows = TYPE_OPTIONS.chunked(2)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { option ->
                    TypeCard(
                        option = option,
                        isSelected = draft.type == option.type,
                        onClick = { actions.selectType(option.type) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // 奇数个时补齐占位
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** 模板卡片 */
@Composable
private fun TemplateCard(
    template: TrackerTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = runCatching { Color(ColorUtils.parseColor(template.color)) }
        .getOrDefault(Primary)

    GlassCard(
        level = if (isSelected) GlassLevel.STANDARD else GlassLevel.LIGHT,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(120.dp)
            .then(
                if (isSelected) Modifier.border(
                    width = 2.dp,
                    color = Primary,
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(text = template.icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = template.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = template.category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 类型卡片 */
@Composable
private fun TypeCard(
    option: TypeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        level = if (isSelected) GlassLevel.STANDARD else GlassLevel.LIGHT,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        modifier = modifier
            .height(100.dp)
            .then(
                if (isSelected) Modifier.border(
                    width = 2.dp,
                    color = Primary,
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = option.icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = option.desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
