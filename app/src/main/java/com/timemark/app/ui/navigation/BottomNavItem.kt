package com.timemark.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import com.timemark.app.core.ui.components.glass.BottomNavItem

/**
 * 底部导航栏项列表
 *
 * 使用 core 模块定义的 [BottomNavItem]，避免 app 与 core 之间产生循环依赖。
 * 顺序与显示顺序一致：首页 / 统计 / AI / 我的。
 */
val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(route = Route.Home.route, icon = Icons.Default.Home, label = "首页"),
    BottomNavItem(route = Route.Stats.route, icon = Icons.Default.BarChart, label = "统计"),
    BottomNavItem(route = Route.AI.route, icon = Icons.Default.SmartToy, label = "AI"),
    BottomNavItem(route = Route.Settings.route, icon = Icons.Default.Settings, label = "我的")
)
