package com.timemark.app.core.ui.components.glass

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航项数据类
 *
 * 用于 [GlassBottomBar] 的导航项定义。在 core 模块中定义，
 * 以便 app 模块与各 feature 模块复用，避免循环依赖。
 *
 * @param route 路由标识
 * @param icon 默认图标
 * @param label 显示文字
 * @param selectedIcon 选中状态图标，为 null 时使用 [icon]
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val selectedIcon: ImageVector? = null
)
