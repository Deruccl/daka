package com.timemark.app.feature.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.feature.settings.components.SettingsSection
import com.timemark.app.feature.settings.components.SliderSettingItem
import com.timemark.app.feature.settings.components.SwitchSettingItem

/**
 * AI 功能设置页面（Task 33.3）
 *
 * 提供：
 * - 各 AI 功能独立开关（食物识别/营养分析/聊天/饮水分析/运动分析/睡眠分析/习惯分析）
 * - 仅 WiFi 模式开关
 * - Token 优化设置（缓存开关、图片压缩质量）（Task 33.4）
 */
@Composable
fun AIFeatureSettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = hiltViewModel()

    val aiFoodRecognitionEnabled by viewModel.aiFoodRecognitionEnabled.collectAsStateWithLifecycle()
    val aiNutritionAnalysisEnabled by viewModel.aiNutritionAnalysisEnabled.collectAsStateWithLifecycle()
    val aiChatEnabled by viewModel.aiChatEnabled.collectAsStateWithLifecycle()
    val aiWaterAnalysisEnabled by viewModel.aiWaterAnalysisEnabled.collectAsStateWithLifecycle()
    val aiExerciseAnalysisEnabled by viewModel.aiExerciseAnalysisEnabled.collectAsStateWithLifecycle()
    val aiSleepAnalysisEnabled by viewModel.aiSleepAnalysisEnabled.collectAsStateWithLifecycle()
    val aiHabitAnalysisEnabled by viewModel.aiHabitAnalysisEnabled.collectAsStateWithLifecycle()
    val aiWifiOnly by viewModel.aiWifiOnly.collectAsStateWithLifecycle()
    val aiCacheEnabled by viewModel.aiCacheEnabled.collectAsStateWithLifecycle()
    val aiImageQuality by viewModel.aiImageQuality.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "AI 功能设置",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
        ) {
            // AI 功能独立开关
            item {
                SettingsSection("AI 功能开关") {
                    SwitchSettingItem(
                        title = "食物识别",
                        checked = aiFoodRecognitionEnabled,
                        onCheckedChange = viewModel::setAIFoodRecognitionEnabled,
                        description = "AI 识别图片中的食物"
                    )
                    SwitchSettingItem(
                        title = "营养分析",
                        checked = aiNutritionAnalysisEnabled,
                        onCheckedChange = viewModel::setAINutritionAnalysisEnabled,
                        description = "AI 分析营养摄入情况"
                    )
                    SwitchSettingItem(
                        title = "AI 聊天",
                        checked = aiChatEnabled,
                        onCheckedChange = viewModel::setAIChatEnabled,
                        description = "AI 对话助手"
                    )
                    SwitchSettingItem(
                        title = "饮水分析",
                        checked = aiWaterAnalysisEnabled,
                        onCheckedChange = viewModel::setAIWaterAnalysisEnabled,
                        description = "AI 分析饮水习惯"
                    )
                    SwitchSettingItem(
                        title = "运动分析",
                        checked = aiExerciseAnalysisEnabled,
                        onCheckedChange = viewModel::setAIExerciseAnalysisEnabled,
                        description = "AI 分析运动数据"
                    )
                    SwitchSettingItem(
                        title = "睡眠分析",
                        checked = aiSleepAnalysisEnabled,
                        onCheckedChange = viewModel::setAISleepAnalysisEnabled,
                        description = "AI 分析睡眠质量"
                    )
                    SwitchSettingItem(
                        title = "习惯分析",
                        checked = aiHabitAnalysisEnabled,
                        onCheckedChange = viewModel::setAIHabitAnalysisEnabled,
                        description = "AI 分析习惯养成情况"
                    )
                }
            }

            // 网络限制
            item {
                SettingsSection("网络限制") {
                    SwitchSettingItem(
                        title = "仅 WiFi 下使用",
                        checked = aiWifiOnly,
                        onCheckedChange = viewModel::setAIWifiOnly,
                        description = "开启后仅在 WiFi 网络下调用 AI，避免消耗移动流量"
                    )
                }
            }

            // Task 33.4: Token 优化
            item {
                SettingsSection("Token 优化") {
                    SwitchSettingItem(
                        title = "响应缓存",
                        checked = aiCacheEnabled,
                        onCheckedChange = viewModel::setAICacheEnabled,
                        description = "缓存 AI 响应，相同请求直接返回不消耗 Token"
                    )
                    SliderSettingItem(
                        title = "图片压缩质量",
                        value = aiImageQuality.toFloat(),
                        range = 50f..100f,
                        steps = 9,
                        valueLabel = "$aiImageQuality",
                        onValueChange = { viewModel.setAIImageQuality(it.toInt()) }
                    )
                }
            }
        }
    }
}
