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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.ai.PerformanceMonitor
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.AIProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 性能监控 ViewModel（Task 36.4）
 *
 * 注入 [PerformanceMonitor] 获取各 Provider 的性能指标。
 */
@HiltViewModel
class PerformanceMonitorViewModel @Inject constructor(
    val performanceMonitor: PerformanceMonitor
) : ViewModel() {

    /** 所有 Provider 的性能指标 */
    val allPerformance = performanceMonitor.getAllPerformance()

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
 * API 性能监控页面（Task 36.4）
 *
 * 展示：
 * - 各 Provider 性能指标卡片（请求数、成功率、平均响应时间、质量评分）
 * - 响应时间趋势图（Canvas 折线图）
 * - 成功率饼图（Canvas 环形图）
 * - 质量评分排行（柱状图）
 */
@Composable
fun PerformanceMonitorScreen(navController: NavController) {
    val viewModel: PerformanceMonitorViewModel = hiltViewModel()
    val performanceList by viewModel.allPerformance.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "API 性能监控",
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
            // 空状态
            if (performanceList.isEmpty()) {
                GlassCard(level = GlassLevel.LIGHT, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无性能数据\n使用 AI 功能后，此处将显示各 Provider 的性能指标",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                // 质量评分排行
                QualityScoreRanking(
                    performanceList = performanceList,
                    viewModel = viewModel
                )

                // 各 Provider 性能卡片
                performanceList.forEach { performance ->
                    ProviderPerformanceCard(
                        performance = performance,
                        viewModel = viewModel
                    )
                }

                // 成功率饼图
                SuccessRatePieChart(
                    performanceList = performanceList,
                    viewModel = viewModel
                )
            }
        }
    }
}

/**
 * 质量评分排行（Task 36.4）
 *
 * 使用柱状图展示各 Provider 的质量评分（0-100）。
 */
@Composable
private fun QualityScoreRanking(
    performanceList: List<PerformanceMonitor.ProviderPerformance>,
    viewModel: PerformanceMonitorViewModel
) {
    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "质量评分排行",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            performanceList.forEach { performance ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.providerDisplayName(performance.provider),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${performance.qualityScore} 分",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = getScoreColor(performance.qualityScore)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { performance.qualityScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = getScoreColor(performance.qualityScore),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Provider 性能指标卡片（Task 36.4）
 */
@Composable
private fun ProviderPerformanceCard(
    performance: PerformanceMonitor.ProviderPerformance,
    viewModel: PerformanceMonitorViewModel
) {
    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.providerDisplayName(performance.provider),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${performance.qualityScore} 分",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getScoreColor(performance.qualityScore)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            MetricRow(label = "总请求", value = "${performance.totalRequests}")
            MetricRow(label = "成功", value = "${performance.successCount}", valueColor = MaterialTheme.colorScheme.primary)
            MetricRow(label = "失败", value = "${performance.failCount}", valueColor = MaterialTheme.colorScheme.error)
            MetricRow(
                label = "成功率",
                value = "${"%.1f".format(performance.successRate * 100)}%"
            )
            MetricRow(
                label = "平均响应时间",
                value = "${performance.avgResponseTime} ms"
            )

            // 响应时间趋势图
            val history = viewModel.performanceMonitor.getResponseTimeHistory(performance.provider, 30)
            if (history.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "响应时间趋势",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ResponseTimeLineChart(history = history)
            }
        }
    }
}

/** 指标行 */
@Composable
private fun MetricRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * 响应时间趋势折线图（Task 36.4）
 *
 * 使用 Canvas Path 绘制折线图，展示最近请求的响应时间变化趋势。
 */
@Composable
private fun ResponseTimeLineChart(history: List<Long>) {
    val chartColor = MaterialTheme.colorScheme.primary
    val maxTime = (history.maxOrNull() ?: 1L).coerceAtLeast(1L)

    GlassCard(level = GlassLevel.LIGHT, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                if (history.size < 2) return@Canvas

                val stepX = width / (history.size - 1).coerceAtLeast(1)

                // 绘制折线
                val path = Path()
                history.forEachIndexed { index, time ->
                    val x = index * stepX
                    val y = height - (time.toFloat() / maxTime.toFloat()) * height
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                drawPath(
                    path = path,
                    color = chartColor,
                    style = Stroke(width = 3f)
                )

                // 绘制数据点
                history.forEachIndexed { index, time ->
                    val x = index * stepX
                    val y = height - (time.toFloat() / maxTime.toFloat()) * height
                    drawCircle(
                        color = chartColor,
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "最近 ${history.size} 次请求",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "最大 ${maxTime}ms",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 成功率饼图（Task 36.4）
 *
 * 使用 Canvas drawArc 绘制环形图，展示各 Provider 的成功请求数占比。
 */
@Composable
private fun SuccessRatePieChart(
    performanceList: List<PerformanceMonitor.ProviderPerformance>,
    viewModel: PerformanceMonitorViewModel
) {
    val successData = performanceList.filter { it.successCount > 0 }
    if (successData.isEmpty()) return

    val totalSuccess = successData.sumOf { it.successCount }.coerceAtLeast(1)
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

    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "成功请求分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 饼图
                Canvas(modifier = Modifier.size(140.dp)) {
                    val canvasSize = size.minDimension
                    val center = Offset(canvasSize / 2f, canvasSize / 2f)
                    val radius = canvasSize / 2f
                    val strokeWidth = canvasSize * 0.15f

                    var startAngle = -90f
                    successData.forEachIndexed { index, performance ->
                        val sweepAngle =
                            (performance.successCount.toFloat() / totalSuccess.toFloat()) * 360f
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

                Spacer(modifier = Modifier.size(16.dp))

                // 图例
                Column {
                    successData.forEachIndexed { index, performance ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Canvas(modifier = Modifier.size(12.dp)) {
                                drawRect(color = colors[index % colors.size])
                            }
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "${viewModel.providerDisplayName(performance.provider)}: ${performance.successCount}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 根据质量评分获取颜色（Task 36.4） */
private fun getScoreColor(score: Int): Color = when {
    score >= 80 -> Color(0xFF4CAF50) // 绿色：优秀
    score >= 60 -> Color(0xFFFF9800) // 橙色：良好
    score >= 40 -> Color(0xFFFFC107) // 黄色：一般
    else -> Color(0xFFF44336)        // 红色：较差
}
