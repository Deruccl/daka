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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.EmptyState
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.feature.ai.config.AIConfigViewModel

/**
 * AI 配置列表页面
 *
 * 功能：
 * - 全局 AI 开关
 * - 协同模式开关
 * - 模型列表（名称/厂商/类型/状态）
 * - 上下移动调整优先级
 * - 跳转新增/编辑/Token 统计
 */
@Composable
fun AIConfigScreen(navController: NavController) {
    val viewModel: AIConfigViewModel = hiltViewModel()
    val configs by viewModel.configs.collectAsStateWithLifecycle()
    val aiGlobalEnabled by viewModel.aiGlobalEnabled.collectAsStateWithLifecycle()
    val collaborativeMode by viewModel.collaborativeMode.collectAsStateWithLifecycle()
    val todayTokenTotal by viewModel.todayTokenTotal.collectAsStateWithLifecycle()
    val todayCost by viewModel.todayCost.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "AI 配置",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate("token_usage") }) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Token 统计"
                        )
                    }
                    // Task 36.3: 协同效果入口
                    IconButton(onClick = { navController.navigate("collaborative_stats") }) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "协同效果"
                        )
                    }
                    // Task 36.4: 性能监控入口
                    IconButton(onClick = { navController.navigate("performance_monitor") }) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "性能监控"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("ai_config_add") },
                icon = { Icon(Icons.Default.Add, contentDescription = "添加") },
                text = { Text("新增模型") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
            // 全局开关
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
                                text = "关闭后所有 AI 功能将不可用",
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

            // 协同模式开关
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
                                text = "协同模式",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "多模态识别 + 文本模型分析",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = collaborativeMode,
                            onCheckedChange = { viewModel.setCollaborativeMode(it) }
                        )
                    }
                }
            }

            // 今日 Token 概览
            item {
                GlassCard(
                    level = GlassLevel.LIGHT,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "今日 Token",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$todayTokenTotal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "今日费用",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "¥${"%.4f".format(todayCost)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // 配置列表标题
            item {
                Text(
                    text = "已配置模型 (${configs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // 空状态
            if (configs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Default.SmartToy,
                            title = "尚未配置 AI 模型",
                            description = "点击右下角按钮添加你的第一个 AI 模型配置",
                            actionText = "新增模型",
                            onActionClick = { navController.navigate("ai_config_add") }
                        )
                    }
                }
            } else {
                items(configs, key = { it.id }) { config ->
                    ConfigItem(
                        config = config,
                        viewModel = viewModel,
                        onEdit = { navController.navigate("ai_config_edit/${config.id}") }
                    )
                }
            }
        }
    }
}

/** 单个配置项卡片 */
@Composable
private fun ConfigItem(
    config: com.timemark.app.domain.model.AIConfig,
    viewModel: AIConfigViewModel,
    onEdit: () -> Unit
) {
    GlassCard(
        level = GlassLevel.STANDARD,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${viewModel.providerDisplayName(config.provider)} · ${viewModel.modelTypeDisplayName(config.modelType)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = config.model,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { viewModel.toggleEnabled(config) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 功能标签
            if (config.applicableFeatures.isNotEmpty()) {
                Text(
                    text = "适用: ${config.applicableFeatures.joinToString("、") { viewModel.featureDisplayName(it) }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.moveUp(config) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "上移",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { viewModel.moveDown(config) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "下移",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { viewModel.testConnection(config) }) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "测试",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { viewModel.deleteConfig(config.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
