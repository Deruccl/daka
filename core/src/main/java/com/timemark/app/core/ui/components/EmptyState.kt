package com.timemark.app.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.TimeMarkTheme

/**
 * 空状态组件
 *
 * 用于列表无数据、无搜索结果等场景，包含插画图标、标题、说明文字与引导按钮。
 *
 * @param icon 插画图标
 * @param title 标题文字
 * @param description 说明文字
 * @param modifier 修饰符
 * @param actionText 引导按钮文字（可选）
 * @param onActionClick 引导按钮点击回调（可选）
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        // 插画图标
        Box(
            modifier = Modifier
                .size(96.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }

        // 标题
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // 说明文字
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // 引导按钮
        if (actionText != null && onActionClick != null) {
            GlassButton(
                text = actionText,
                onClick = onActionClick,
                type = GlassButtonType.PRIMARY
            )
        }
    }
}

@Preview(name = "EmptyState", showBackground = true)
@Composable
private fun EmptyStatePreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            EmptyState(
                icon = Icons.Filled.Inbox,
                title = "暂无打卡项",
                description = "点击下方按钮创建你的第一个打卡项，开始养成好习惯",
                actionText = "创建打卡",
                onActionClick = {}
            )
        }
    }
}

@Preview(name = "EmptyState - 无按钮", showBackground = true)
@Composable
private fun EmptyStateNoActionPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            EmptyState(
                icon = Icons.Filled.Inbox,
                title = "暂无记录",
                description = "完成打卡后这里会显示记录"
            )
        }
    }
}
