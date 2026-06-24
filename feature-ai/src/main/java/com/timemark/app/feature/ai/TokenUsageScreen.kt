package com.timemark.app.feature.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIUsage
import com.timemark.app.feature.ai.config.AIConfigViewModel

/**
 * Token 用量统计页面
 *
 * 展示：
 * - 今日/本周/本月 Token 消耗
 * - 费用估算
 * - 各功能消耗占比
 * - 简易趋势图（按日聚合）
 */
@Composable
fun TokenUsageScreen(navController: NavController) {
    val viewModel: AIConfigViewModel = hiltViewModel()
    val todayUsage by viewModel.todayUsage.collectAsStateWithLifecycle()
    val weekUsage by viewModel.weekUsage.collectAsStateWithLifecycle()
    val monthUsage by viewModel.monthUsage.collectAsStateWithLifecycle()

    val todayTokens = todayUsage.sumOf { it.tokensInput + it.tokensOutput }
    val todayCost = todayUsage.sumOf { it.cost }
    val weekTokens = weekUsage.sumOf { it.tokensInput + it.tokensOutput }
    val weekCost = weekUsage.sumOf { it.cost }
    val monthTokens = monthUsage.sumOf { it.tokensInput + it.tokensOutput }
    val monthCost = monthUsage.sumOf { it.cost }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Token 用量统计",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 今日概览
            GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "今日",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UsageRow(label = "Token 用量", value = "$todayTokens")
                    UsageRow(label = "费用估算", value = "¥${"%.4f".format(todayCost)}")
                    UsageRow(label = "请求数", value = "${todayUsage.size}")
                }
            }

            // 本周概览
            GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "本周",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UsageRow(label = "Token 用量", value = "$weekTokens")
                    UsageRow(label = "费用估算", value = "¥${"%.4f".format(weekCost)}")
                    UsageRow(label = "请求数", value = "${weekUsage.size}")
                }
            }

            // 本月概览
            GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "本月",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UsageRow(label = "Token 用量", value = "$monthTokens")
                    UsageRow(label = "费用估算", value = "¥${"%.4f".format(monthCost)}")
                    UsageRow(label = "请求数", value = "${monthUsage.size}")
                }
            }

            // 各功能消耗占比
            FeatureBreakdownCard(
                title = "各功能消耗（本月）",
                usages = monthUsage,
                viewModel = viewModel
            )

            // 趋势图（按日聚合，最近 7 天）
            TrendCard(
                title = "近 7 天趋势",
                usages = weekUsage
            )
        }
    }
}

/** 用量行 */
@Composable
private fun UsageRow(label: String, value: String) {
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
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** 功能消耗占比卡片 */
@Composable
private fun FeatureBreakdownCard(
    title: String,
    usages: List<AIUsage>,
    viewModel: AIConfigViewModel
) {
    val featureTokens = usages.groupBy { it.feature }
        .mapValues { (_, list) -> list.sumOf { it.tokensInput + it.tokensOutput } }
        .toList()
        .sortedByDescending { it.second }
    val total = featureTokens.sumOf { it.second }.coerceAtLeast(1)

    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (featureTokens.isEmpty()) {
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                featureTokens.forEach { (feature, tokens) ->
                    val ratio = tokens.toFloat() / total.toFloat()
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
                                text = viewModel.featureDisplayName(feature),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "$tokens (${(ratio * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** 趋势图卡片（按日聚合的简易柱状图） */
@Composable
private fun TrendCard(
    title: String,
    usages: List<AIUsage>
) {
    // 按日聚合
    val dailyTokens = usages
        .groupBy {
            val date = java.time.Instant.ofEpochMilli(it.timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            date.toString()
        }
        .mapValues { (_, list) -> list.sumOf { it.tokensInput + it.tokensOutput } }
        .toSortedMap()

    val maxTokens = dailyTokens.values.maxOrNull() ?: 0

    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (dailyTokens.isEmpty() || maxTokens == 0) {
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                dailyTokens.entries.forEach { (date, tokens) ->
                    val ratio = tokens.toFloat() / maxTokens.toFloat()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date.takeLast(5), // MM-dd
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                        ) {
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                        Text(
                            text = "$tokens",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
