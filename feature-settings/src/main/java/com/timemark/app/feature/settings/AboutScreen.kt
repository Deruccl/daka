package com.timemark.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.feature.settings.components.NavigateSettingItem
import com.timemark.app.feature.settings.components.SettingsSection

/**
 * 关于页面
 *
 * 展示应用信息：
 * - 应用图标和名称
 * - 版本号
 * - 简介
 * - 开源许可
 * - 隐私政策说明
 */
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            GlassTopBar(
                title = "关于",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 应用图标和名称
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.size(96.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(24.dp)
                        )
                    }
                    Text(
                        text = "时光印记",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "版本 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 应用简介
            item {
                SettingsSection("简介") {
                    Text(
                        text = "时光印记是一款专注于习惯养成的打卡应用。" +
                            "支持多种打卡类型、智能提醒、数据统计与 AI 分析，" +
                            "帮助你记录每一天的坚持与成长。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 主要功能
            item {
                SettingsSection("主要功能") {
                    Column(modifier = Modifier.padding(16.dp)) {
                        listOf(
                            "• 多类型打卡：计数、时长、数值、勾选、图文、计时",
                            "• 智能提醒：每日、每周、间隔、智能提醒",
                            "• 数据统计：多维度可视化分析",
                            "• AI 分析：食物识别、营养分析、习惯建议",
                            "• 数据备份：完整备份与恢复，支持 CSV / JSON 导出",
                            "• 隐私保护：应用锁、数据库加密"
                        ).forEach { feature ->
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 开源许可
            item {
                SettingsSection("开源许可") {
                    NavigateSettingItem(
                        title = "开源许可",
                        onClick = { /* TODO: 打开开源许可页面 */ },
                        description = "查看第三方库许可信息"
                    )
                }
            }

            // 隐私政策
            item {
                SettingsSection("隐私政策") {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "我们重视您的隐私。时光印记所有数据均存储在本地设备上，" +
                                "不会上传至任何服务器。AI 分析功能需要将数据发送至您配置的 " +
                                "AI 服务商，请参阅对应服务商的隐私政策。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "• 打卡数据：仅存储在本地数据库\n" +
                                "• AI 数据：仅在启用 AI 功能时发送至您选择的 AI 服务商\n" +
                                "• 备份数据：保存在设备本地存储\n" +
                                "• 无后台追踪，无广告 SDK",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 版权信息
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "© 2024 时光印记",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Made with ❤️ for habit tracking",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
