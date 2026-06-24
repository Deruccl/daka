package com.timemark.app.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.timemark.app.domain.model.TimeViewLevel

/**
 * 时间视图级别选择器
 *
 * 提供分钟/小时/日/周/月/年 六种视图级别切换。
 * 选中项使用主色背景，未选中项使用半透明背景。
 *
 * @param current 当前视图级别
 * @param onSelect 选择回调
 */
@Composable
fun ViewLevelSelector(
    current: TimeViewLevel,
    onSelect: (TimeViewLevel) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.3f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        viewLevels.forEach { (label, level) ->
            ViewLevelChip(
                label = label,
                level = level,
                current = current,
                selectedColor = primaryColor,
                onSelect = onSelect,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** 视图级别与显示文字的映射 */
private val viewLevels = listOf(
    "分" to TimeViewLevel.MINUTE,
    "时" to TimeViewLevel.HOUR,
    "日" to TimeViewLevel.DAY,
    "周" to TimeViewLevel.WEEK,
    "月" to TimeViewLevel.MONTH,
    "年" to TimeViewLevel.YEAR
)

/**
 * 单个级别芯片
 */
@Composable
private fun ViewLevelChip(
    label: String,
    level: TimeViewLevel,
    current: TimeViewLevel,
    selectedColor: Color,
    onSelect: (TimeViewLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = level == current
    val backgroundColor = if (isSelected) selectedColor else Color.Transparent
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        color = textColor,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onSelect(level) }
            .padding(vertical = 8.dp)
    )
}
