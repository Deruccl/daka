package com.timemark.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.timemark.app.core.ui.animation.enterTransitionForRoute
import com.timemark.app.core.ui.animation.exitTransitionForRoute
import com.timemark.app.core.ui.animation.popEnterTransitionForRoute
import com.timemark.app.core.ui.animation.popExitTransitionForRoute
import com.timemark.app.feature.ai.AIChatScreen
import com.timemark.app.feature.ai.AIConfigAddScreen
import com.timemark.app.feature.ai.AIConfigEditScreen
import com.timemark.app.feature.ai.AIConfigScreen
import com.timemark.app.feature.ai.AIScreen
import com.timemark.app.feature.ai.CollaborativeStatsScreen
import com.timemark.app.feature.ai.FoodRecognitionScreen
import com.timemark.app.feature.ai.PerformanceMonitorScreen
import com.timemark.app.feature.ai.TokenUsageScreen
import com.timemark.app.feature.ai.analysis.AnalysisListScreen
import com.timemark.app.feature.ai.analysis.AnalysisResultScreen
import com.timemark.app.feature.home.HomeScreen
import com.timemark.app.feature.settings.AboutScreen
import com.timemark.app.feature.settings.AIFeatureSettingsScreen
import com.timemark.app.feature.settings.BackupRestoreScreen
import com.timemark.app.feature.settings.CrashLogScreen
import com.timemark.app.feature.settings.LogSettingsScreen
import com.timemark.app.feature.settings.SettingsScreen
import com.timemark.app.feature.settings.lock.AppLockScreen
import com.timemark.app.feature.settings.NetworkLogScreen
import com.timemark.app.feature.stats.StatsScreen
import com.timemark.app.feature.tracker.CreateTrackerScreen
import com.timemark.app.feature.tracker.EditTrackerScreen
import com.timemark.app.feature.tracker.TrackerDetailScreen

/**
 * 应用导航图
 *
 * 集中注册所有页面的路由与对应的 Composable。
 *
 * 页面切换动画按路由类型分类：
 * - 底部导航（Home/Stats/AI/Settings）：淡入淡出 + 轻微位移，250ms
 * - 详情页（TrackerDetail/EditTracker 等）：从右侧滑入，350ms EaseOutCubic
 * - 全屏弹窗（AIChat/FoodRecognition）：从底部滑入 + 缩放，300ms EaseOutBack
 *
 * @param navController 导航控制器
 * @param modifier 修饰符，由 [com.timemark.app.ui.ScaffoldMain] 传入 paddingValues
 */
@Composable
fun TimeMarkNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
        modifier = modifier,
        // 根据目标路由类型选择对应的进入/退出动画
        enterTransition = { enterTransitionForRoute(this) },
        exitTransition = { exitTransitionForRoute(this) },
        popEnterTransition = { popEnterTransitionForRoute(this) },
        popExitTransition = { popExitTransitionForRoute(this) }
    ) {
        // 主 Tab 页面
        composable(Route.Home.route) { HomeScreen(navController) }
        composable(Route.Stats.route) { StatsScreen(navController) }
        composable(Route.AI.route) { AIScreen(navController) }
        composable(Route.Settings.route) { SettingsScreen(navController) }

        // Tracker 相关
        composable(Route.CreateTracker.route) { CreateTrackerScreen(navController) }
        composable(
            route = Route.EditTracker.route,
            arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: 0L
            EditTrackerScreen(navController = navController, trackerId = trackerId)
        }
        composable(
            route = Route.TrackerDetail.route,
            arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: 0L
            TrackerDetailScreen(navController = navController, trackerId = trackerId)
        }

        // AI 相关
        composable(Route.AIConfig.route) { AIConfigScreen(navController) }
        composable(Route.AIConfigAdd.route) { AIConfigAddScreen(navController) }
        composable(
            route = Route.AIConfigEdit.route,
            arguments = listOf(navArgument("configId") { type = NavType.LongType })
        ) { backStackEntry ->
            val configId = backStackEntry.arguments?.getLong("configId") ?: 0L
            AIConfigEditScreen(navController = navController, configId = configId)
        }
        composable(Route.AIChat.route) { AIChatScreen(navController) }
        composable(Route.FoodRecognition.route) { FoodRecognitionScreen(navController) }
        composable(Route.TokenUsage.route) { TokenUsageScreen(navController) }
        // Task 36.3: 协同效果对比
        composable(Route.CollaborativeStats.route) { CollaborativeStatsScreen(navController) }
        // Task 36.4: API 性能监控
        composable(Route.PerformanceMonitor.route) { PerformanceMonitorScreen(navController) }
        // Task 33.1: AI 分析页面
        composable(Route.AnalysisList.route) { AnalysisListScreen(navController) }
        composable(
            route = Route.AnalysisResult.route,
            arguments = listOf(navArgument("featureName") { type = NavType.StringType })
        ) { backStackEntry ->
            val featureName = backStackEntry.arguments?.getString("featureName") ?: ""
            AnalysisResultScreen(navController = navController, featureName = featureName)
        }

        // 设置相关
        composable(Route.BackupRestore.route) { BackupRestoreScreen(navController) }
        composable(Route.AppLock.route) { AppLockScreen(navController) }
        composable(Route.NetworkLog.route) { NetworkLogScreen(navController) }
        // Task 33.3: AI 功能设置
        composable(Route.AIFeatureSettings.route) { AIFeatureSettingsScreen(navController) }
        composable(Route.About.route) { AboutScreen(navController) }
        // Task 38.3: 日志管理
        composable(Route.LogSettings.route) { LogSettingsScreen(navController) }
        // Task 38.4: 崩溃日志
        composable(Route.CrashLog.route) { CrashLogScreen(navController) }
    }
}
