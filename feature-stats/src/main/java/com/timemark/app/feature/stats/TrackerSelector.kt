package com.timemark.app.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timemark.app.core.utils.ColorUtils
import com.timemark.app.domain.model.Tracker

/**
 * 打卡项目选择器
 *
 * 横向滚动的项目列表，第一项固定为"全部"，
 * 点击单个项目后高亮显示并回调选中事件。
 *
 * @param trackers 全部打卡项目
 * @param selectedTrackerId 当前选中的项目 id，null 表示全部
 * @param onSelect 选中回调
 */
@Composable
fun TrackerSelector(
    trackers: List<Tracker>,
    selectedTrackerId: Long?,
    onSelect: (Long?) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "全部" 选项
        item(key = "all") {
            TrackerChip(
                label = "全部",
                icon = "📊",
                color = primaryColor,
                isSelected = selectedTrackerId == null,
                onClick = { onSelect(null) }
            )
        }
        // 单个打卡项目
        items(trackers, key = { it.id }) { tracker ->
            val chipColor = remember(tracker.color, primaryColor) {
                runCatching { Color(ColorUtils.parseColor(tracker.color)) }
                    .getOrDefault(primaryColor)
            }
            TrackerChip(
                label = tracker.name,
                icon = tracker.icon,
                color = chipColor,
                isSelected = selectedTrackerId == tracker.id,
                onClick = { onSelect(tracker.id) }
            )
        }
    }
}

/**
 * 单个选项芯片
 *
 * 选中时使用项目颜色作为背景与边框，未选中时使用半透明背景。
 */
@Composable
private fun TrackerChip(
    label: String,
    icon: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) color.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.4f)
    val borderColor = if (isSelected) color else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标圆形背景
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
        )
    }
}
