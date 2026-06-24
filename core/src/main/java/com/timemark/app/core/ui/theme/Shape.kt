package com.timemark.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 形状系统
 *
 * 定义 5 级圆角，对应不同尺寸的组件：
 * - extraSmall：小圆角（标签、Chip）
 * - small：中圆角（小卡片、输入框）
 * - medium：大圆角（卡片、对话框）
 * - large：超大圆角（底部弹窗、大卡片）
 * - extraLarge：完全圆角（药丸形按钮、FAB）
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // 小圆角
    small = RoundedCornerShape(16.dp),        // 中圆角
    medium = RoundedCornerShape(24.dp),       // 大圆角
    large = RoundedCornerShape(32.dp),        // 超大圆角
    extraLarge = RoundedCornerShape(9999.dp)  // 完全圆角（药丸）
)
