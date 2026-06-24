package com.timemark.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.timemark.app.core.ui.components.glass.GlassBottomBar
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
 * @param navController 应用级导航控制器
 */
@Composable
fun ScaffoldMain(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 仅在四个主 Tab 页面显示底部导航栏
    val showBottomBar = currentRoute in listOf(
        Route.Home.route,
        Route.Stats.route,
        Route.AI.route,
        Route.Settings.route
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
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
