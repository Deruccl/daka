package com.timemark.app.feature.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.feature.ai.config.AIConfigViewModel

/**
 * AI 功能主页面
 *
 * 展示：
 * - AI 功能全局开关
 * - 功能列表（食物识别、营养分析、饮水分析、运动分析、睡眠分析、习惯分析、AI 聊天）
 * - 每个功能卡片显示状态（可用/未配置）
 * - 点击进入对应功能
 */
@Composable
fun AIScreen(navController: NavController) {
    val viewModel: AIConfigViewModel = hiltViewModel()
    val configs by viewModel.configs.collectAsStateWithLifecycle()
    val aiGlobalEnabled by viewModel.aiGlobalEnabled.collectAsStateWithLifecycle()

    // 各功能是否可用（有对应配置且启用）
    val enabledConfigs = configs.filter { it.enabled }
    val featureAvailability: Map<AIFeature, Boolean> = AIFeature.values().associateWith { feature ->
        enabledConfigs.any { c -> feature in c.applicableFeatures }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "AI 助手",
                actions = {
                    androidx.compose.material3.IconButton(onClick = { navController.navigate("ai_config") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "AI 配置"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 全局开关卡片
            item {
                GlassCard(
                    level = GlassLevel.STANDARD,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI 功能",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (aiGlobalEnabled) "已开启" else "已关闭",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = aiGlobalEnabled,
                            onCheckedChange = { viewModel.setAIGlobalEnabled(it) }
                        )
                    }
                }
            }

            // 功能列表标题
            item {
                Text(
                    text = "AI 功能",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 功能卡片
            val features = listOf(
                AIFeature.FOOD_RECOGNITION to "食物识别" to Icons.Default.Restaurant,
                AIFeature.NUTRITION_ANALYSIS to "营养分析" to Icons.Default.Insights,
                AIFeature.WATER_ANALYSIS to "饮水分析" to Icons.Default.LocalDrink,
                AIFeature.EXERCISE_ANALYSIS to "运动分析" to Icons.Default.DirectionsRun,
                AIFeature.SLEEP_ANALYSIS to "睡眠分析" to Icons.Default.Bedtime,
                AIFeature.HABIT_ANALYSIS to "习惯分析" to Icons.Default.Psychology,
                AIFeature.CHAT to "AI 聊天" to Icons.Default.Chat
            )

            items(features) { (pair, icon) ->
                val (feature, label) = pair
                val available = featureAvailability[feature] == true && aiGlobalEnabled
                FeatureCard(
                    icon = icon,
                    title = label,
                    description = if (available) "可用" else "未配置",
                    available = available,
                    onClick = {
                        when (feature) {
                            AIFeature.FOOD_RECOGNITION -> navController.navigate("food_recognition")
                            AIFeature.CHAT -> navController.navigate("ai_chat")
                            AIFeature.WATER_ANALYSIS,
                            AIFeature.EXERCISE_ANALYSIS,
                            AIFeature.SLEEP_ANALYSIS,
                            AIFeature.HABIT_ANALYSIS -> {
                                // Task 33.1: 跳转到分析列表页
                                navController.navigate("analysis_list")
                            }
                            else -> {
                                // 其他功能跳转到聊天
                                navController.navigate("ai_chat")
                            }
                        }
                    }
                )
            }
        }
    }
}

/** 功能卡片 */
@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    available: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        level = if (available) GlassLevel.STANDARD else GlassLevel.LIGHT,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (available) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (available) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
