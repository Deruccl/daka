package com.timemark.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.timemark.app.core.ui.components.glass.GlassBottomBar
import com.timemark.app.core.utils.WindowSizeClass
import com.timemark.app.core.utils.windowSizeClass
import com.timemark.app.ui.navigation.Route
import com.timemark.app.ui.navigation.TimeMarkNavHost
import com.timemark.app.ui.navigation.bottomNavItems

/**
 * 主页面 Scaffold
 *
 * 职责：
 * - 根据当前路由动态显示/隐藏底部导航栏（仅主 Tab 显示，其他页面隐藏）
 * - 底部导航栏切换使用 saveState/restoreState 保留各 Tab 状态
 * - 承载 [TimeMarkNavHost] 并将 paddingValues 透传
 *
 * Task 37.3: 底部导航支持键盘切换（左右方向键 + Enter 激活）
 * Task 37.4: EXPANDED 尺寸时使用 NavigationRail 替代底部导航
 *
 * @param navController 应用级导航控制器
 */
@Composable
fun ScaffoldMain(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val sizeClass = windowSizeClass()

    // 仅在四个主 Tab 页面显示导航栏
    val showNav = currentRoute in listOf(
        Route.Home.route,
        Route.Stats.route,
        Route.AI.route,
        Route.Settings.route
    )

    // Task 37.4: EXPANDED 尺寸使用 NavigationRail（左侧导航栏）
    if (sizeClass == WindowSizeClass.EXPANDED && showNav) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 左侧导航栏
            TimeMarkNavigationRail(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            // 内容区
            TimeMarkNavHost(
                navController = navController,
                modifier = Modifier.fillMaxHeight()
            )
        }
    } else {
        // 手机模式：底部导航栏
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = showNav,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    GlassBottomBar(
                        items = bottomNavItems,
                        currentRoute = currentRoute,
                        onItemClick = { route ->
                            navController.navigate(route) {
                                // 弹出到起始目的地，保留状态
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // 避免重复创建同一目的地
                                launchSingleTop = true
                                // 恢复之前保存的状态
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            TimeMarkNavHost(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * Task 37.4: 平板模式左侧导航栏
 *
 * 使用 Material 3 NavigationRail，支持键盘导航与无障碍语义。
 */
@Composable
private fun TimeMarkNavigationRail(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    NavigationRail {
        bottomNavItems.forEach { item ->
            val isSelected = item.route == currentRoute
            NavigationRailItem(
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) (item.selectedIcon ?: item.icon) else item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                // Task 37.1: 无障碍语义
                modifier = Modifier.semantics {
                    role = Role.Tab
                    contentDescription = item.label
                    selected = isSelected
                }
            )
        }
    }
}
