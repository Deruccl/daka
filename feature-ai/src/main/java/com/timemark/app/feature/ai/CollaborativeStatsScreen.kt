package com.timemark.app.feature.ai

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.ai.CollaborativeService
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.AIProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 协同效果统计 ViewModel（Task 36.3）
 *
 * 注入 [CollaborativeService] 获取协同模式统计数据。
 */
@HiltViewModel
class CollaborativeStatsViewModel @Inject constructor(
    private val collaborativeService: CollaborativeService
) : ViewModel() {

    /** 协同效果统计 */
    val stats = collaborativeService.getCollaborativeStats()

    /** 重置统计 */
    fun resetStats() = collaborativeService.resetStats()

    /** 厂商显示名 */
    fun providerDisplayName(provider: AIProvider): String = when (provider) {
        AIProvider.OPENAI -> "OpenAI"
        AIProvider.ANTHROPIC -> "Anthropic"
        AIProvider.GEMINI -> "Gemini"
        AIProvider.BAIDU -> "百度文心"
        AIProvider.ALIBABA -> "阿里通义"
        AIProvider.BYTEDANCE -> "字节豆包"
        AIProvider.ZHIPU -> "智谱 GLM"
        AIProvider.MOONSHOT -> "Kimi"
        AIProvider.OLLAMA -> "Ollama"
        AIProvider.CUSTOM -> "自定义"
    }
}

/**
 * 协同效果对比页面（Task 36.3）
 *
 * 展示：
 * - 协同模式 vs 单模型模式的 Token 消耗对比（柱状图）
 * - 节省 Token 数量与节省比例
 * - 各 Provider 使用次数分布（饼图 + 柱状图）
 */
@Composable
fun CollaborativeStatsScreen(navController: NavController) {
    val viewModel: CollaborativeStatsViewModel = hiltViewModel()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "协同效果对比",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 概览卡片
            GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "协同模式效果",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    StatRow(label = "协同请求次数", value = "${stats.totalRequests}")
                    StatRow(label = "协同模式 Token", value = "${stats.collaborativeTokens}")
                    StatRow(label = "单模型估算 Token", value = "${stats.singleModelTokens}")
                    StatRow(
                        label = "节省 Token",
                        value = "${stats.savedTokens}",
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                    StatRow(
                        label = "节省比例",
                        value = "${"%.1f".format(stats.savedPercentage * 100)}%",
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Token 消耗对比柱状图
            TokenComparisonChart(
                collaborativeTokens = stats.collaborativeTokens,
                singleModelTokens = stats.singleModelTokens
            )

            // 各 Provider 使用次数分布
            ProviderUsageSection(
                providerUsage = stats.providerUsage,
                viewModel = viewModel
            )

            // 空状态提示
            if (stats.totalRequests == 0) {
                GlassCard(level = GlassLevel.LIGHT, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无协同模式请求记录\n开启协同模式并使用图片识别功能后，此处将显示效果对比",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // 重置按钮
            if (stats.totalRequests > 0) {
                GlassButton(
                    text = "重置统计",
                    onClick = { viewModel.resetStats() },
                    type = GlassButtonType.SECONDARY
                )
            }
        }
    }
}

/** 统计行 */
@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * Token 消耗对比柱状图（Task 36.3）
 *
 * 使用 LinearProgressIndicator 展示协同模式 vs 单模型模式的 Token 消耗对比。
 */
@Composable
private fun TokenComparisonChart(
    collaborativeTokens: Int,
    singleModelTokens: Int
) {
    val maxTokens = maxOf(collaborativeTokens, singleModelTokens, 1)

    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Token 消耗对比",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 协同模式
            Text(
                text = "协同模式: $collaborativeTokens Token",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { collaborativeTokens.toFloat() / maxTokens.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 单模型模式
            Text(
                text = "单模型估算: $singleModelTokens Token",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { singleModelTokens.toFloat() / maxTokens.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

/**
 * Provider 使用次数分布（Task 36.3）
 *
 * 使用 Canvas 饼图 + 柱状图展示各 Provider 在协同模式中的使用次数。
 */
@Composable
private fun ProviderUsageSection(
    providerUsage: Map<AIProvider, Int>,
    viewModel: CollaborativeStatsViewModel
) {
    if (providerUsage.isEmpty()) return

    val total = providerUsage.values.sum().coerceAtLeast(1)
    val sortedUsage = providerUsage.toList().sortedByDescending { it.second }

    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Provider 使用次数分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 饼图（Canvas 绘制）
            PieChart(
                data = sortedUsage.map { (provider, count) ->
                    PieChartData(
                        label = viewModel.providerDisplayName(provider),
                        value = count,
                        color = getProviderColor(provider, sortedUsage.indexOfFirst { it.first == provider })
                    )
                },
                total = total
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 柱状图列表
            sortedUsage.forEach { (provider, count) ->
                val ratio = count.toFloat() / total.toFloat()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.providerDisplayName(provider),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "$count 次 (${(ratio * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = getProviderColor(provider, sortedUsage.indexOfFirst { it.first == provider }),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

/** 饼图数据项 */
private data class PieChartData(
    val label: String,
    val value: Int,
    val color: Color
)

/**
 * Canvas 饼图（Task 36.3）
 *
 * 使用 Canvas drawArc 绘制环形饼图，展示各 Provider 使用占比。
 */
@Composable
private fun PieChart(data: List<PieChartData>, total: Int) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFE91E63),
        Color(0xFF00BCD4),
        Color(0xFFFF9800),
        Color(0xFF8BC34A),
        Color(0xFF673AB7),
        Color(0xFF607D8B),
        Color(0xFF795548)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(160.dp)
        ) {
            val canvasSize = size.minDimension
            val center = Offset(canvasSize / 2f, canvasSize / 2f)
            val radius = canvasSize / 2f
            val strokeWidth = canvasSize * 0.15f

            var startAngle = -90f // 从顶部开始

            data.forEachIndexed { index, item ->
                val sweepAngle = (item.value.toFloat() / total.toFloat()) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweepAngle
            }
        }
    }
}

/** 获取 Provider 对应的图表颜色 */
private fun getProviderColor(provider: AIProvider, index: Int): Color {
    val colors = listOf(
        Color(0xFF6750A4), // primary
        Color(0xFF625B71), // secondary
        Color(0xFF7D5260), // tertiary
        Color(0xFFE91E63),
        Color(0xFF00BCD4),
        Color(0xFFFF9800),
        Color(0xFF8BC34A),
        Color(0xFF673AB7),
        Color(0xFF607D8B),
        Color(0xFF795548)
    )
    return colors[index % colors.size]
}
