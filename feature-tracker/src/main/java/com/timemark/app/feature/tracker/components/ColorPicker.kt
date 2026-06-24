package com.timemark.app.feature.tracker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.utils.ColorUtils

/** 预设颜色列表（十六进制） */
private val COLOR_LIST: List<String> = listOf(
    "#6366F1", // 靛蓝（主色）
    "#EC4899", // 粉红
    "#10B981", // 翠绿
    "#F44336", // 红色
    "#FF9800", // 橙色
    "#FFC107", // 琥珀
    "#2196F3", // 蓝色
    "#00BCD4", // 青色
    "#9C27B0", // 紫色
    "#673AB7", // 深紫
    "#4CAF50", // 绿色
    "#795548", // 棕色
    "#607D8B", // 蓝灰
    "#E91E63"  // 玫红
)

/**
 * 颜色选择器
 *
 * 以网格形式展示预设颜色，点击选中。
 * 当前选中的颜色有白色边框高亮。
 *
 * @param selected 当前选中的颜色（十六进制 #RRGGBB）
 * @param onSelect 选中回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "颜色",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassCard(
            level = GlassLevel.LIGHT,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                COLOR_LIST.forEach { hex ->
                    val isSelected = hex.equals(selected, ignoreCase = true)
                    // 解析颜色，失败时回退到主色
                    val color = runCatching { Color(ColorUtils.parseColor(hex)) }
                        .getOrDefault(Color(0xFF6366F1))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onSelect(hex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = Color.Black.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
