package com.timemark.app.core.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * 可访问图标组件
 *
 * 包装 [Icon]，强制要求 [contentDescription]，确保所有图标对 TalkBack 屏幕阅读器可读。
 * 适用于纯展示性图标（无点击行为）。若图标本身是按钮的一部分，应使用 IconButton 并设置描述。
 *
 * @param imageVector 图标矢量
 * @param contentDescription 强制要求的内容描述（不可为空），用于无障碍播报
 * @param modifier 修饰符
 * @param tint 图标着色，默认跟随主题 onSurface
 */
@Composable
fun AccessibleIcon(
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        // 设置 Role.Image，让 TalkBack 将其识别为图像元素
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
            role = Role.Image
        }
    )
}
