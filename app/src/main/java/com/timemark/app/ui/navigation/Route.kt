package com.timemark.app.ui.navigation

/**
 * 应用路由定义
 *
 * 集中管理所有页面的路由路径，避免硬编码字符串带来的拼写错误。
 *
 * - 主 Tab 页面：Home / Stats / AI / Settings
 * - Tracker 相关：CreateTracker / EditTracker / TrackerDetail
 * - AI 相关：AIConfig / AIConfigAdd / AIConfigEdit / AIChat
 * - 设置相关：BackupRestore / About
 */
sealed class Route(val route: String) {

    /** 首页（主 Tab） */
    object Home : Route("home")

    /** 统计页（主 Tab） */
    object Stats : Route("stats")

    /** AI 页（主 Tab） */
    object AI : Route("ai")

    /** 我的 / 设置页（主 Tab） */
    object Settings : Route("settings")

    /** 创建打卡项 */
    object CreateTracker : Route("create_tracker")

    /** 编辑打卡项 */
    object EditTracker : Route("edit_tracker/{trackerId}") {
        fun createRoute(trackerId: Long) = "edit_tracker/$trackerId"
    }

    /** 打卡项详情 */
    object TrackerDetail : Route("tracker_detail/{trackerId}") {
        fun createRoute(trackerId: Long) = "tracker_detail/$trackerId"
    }

    /** AI 配置列表 */
    object AIConfig : Route("ai_config")

    /** 新增 AI 配置 */
    object AIConfigAdd : Route("ai_config_add")

    /** 编辑 AI 配置 */
    object AIConfigEdit : Route("ai_config_edit/{configId}") {
        fun createRoute(configId: Long) = "ai_config_edit/$configId"
    }

    /** AI 对话 */
    object AIChat : Route("ai_chat")

    /** 食物识别 */
    object FoodRecognition : Route("food_recognition")

    /** Token 用量统计 */
    object TokenUsage : Route("token_usage")

    /** Task 36.3: 协同效果对比 */
    object CollaborativeStats : Route("collaborative_stats")

    /** Task 36.4: API 性能监控 */
    object PerformanceMonitor : Route("performance_monitor")

    /** 备份与恢复 */
    object BackupRestore : Route("backup_restore")

    /** 应用锁设置（Task 32.1） */
    object AppLock : Route("app_lock")

    /** 网络请求日志（Task 32.6） */
    object NetworkLog : Route("network_log")

    /** AI 功能设置（Task 33.3） */
    object AIFeatureSettings : Route("ai_feature_settings")

    /** AI 分析列表（Task 33.1） */
    object AnalysisList : Route("analysis_list")

    /** AI 分析结果（Task 33.1） */
    object AnalysisResult : Route("analysis_result/{featureName}") {
        fun createRoute(featureName: String) = "analysis_result/$featureName"
    }

    /** 关于 */
    object About : Route("about")

    /** Task 38.3: 日志管理 */
    object LogSettings : Route("log_settings")

    /** Task 38.4: 崩溃日志 */
    object CrashLog : Route("crash_log")
}
