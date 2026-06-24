package com.timemark.app.feature.ai.analysis

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.ExerciseAnalysis
import com.timemark.app.domain.model.ExerciseTrend
import com.timemark.app.domain.model.HabitAnalysis
import com.timemark.app.domain.model.SleepAnalysis
import com.timemark.app.domain.model.WaterIntakeAnalysis

/**
 * 分析结果展示页面
 *
 * 接收路由参数 feature，触发对应分析并展示结果：
 * - 数据可视化（进度条/数值卡片）
 * - AI 建议
 */
@Composable
fun AnalysisResultScreen(
    navController: NavController,
    featureName: String,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val feature = runCatching { AIFeature.valueOf(featureName) }.getOrNull()
    val state by viewModel.analysisState.collectAsStateWithLifecycle()

    // 进入页面时自动触发分析
    LaunchedEffect(feature) {
        if (feature != null && state is AnalysisViewModel.AnalysisState.Idle) {
            viewModel.analyze(feature)
        }
    }

    val title = when (feature) {
        AIFeature.WATER_ANALYSIS -> "饮水分析结果"
        AIFeature.EXERCISE_ANALYSIS -> "运动分析结果"
        AIFeature.SLEEP_ANALYSIS -> "睡眠分析结果"
        AIFeature.HABIT_ANALYSIS -> "习惯分析结果"
        else -> "分析结果"
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = title,
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
            when (val s = state) {
                is AnalysisViewModel.AnalysisState.Idle -> {
                    Text("准备分析...")
                }
                is AnalysisViewModel.AnalysisState.Loading -> {
                    LoadingCard()
                }
                is AnalysisViewModel.AnalysisState.Success -> {
                    ResultContent(s.data)
                }
                is AnalysisViewModel.AnalysisState.Error -> {
                    ErrorCard(s.message) {
                        feature?.let { viewModel.analyze(it) }
                    }
                }
            }
        }
    }
}

/** 加载中卡片 */
@Composable
private fun LoadingCard() {
    GlassCard(
        level = GlassLevel.LIGHT,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.size(12.dp))
            Text("AI 正在分析中...")
        }
    }
}

/** 错误卡片 */
@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    GlassCard(
        level = GlassLevel.LIGHT,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚠️ 分析失败",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            GlassButton(
                text = "重试",
                onClick = onRetry,
                type = GlassButtonType.SECONDARY
            )
        }
    }
}

/** 结果内容分发 */
@Composable
private fun ResultContent(data: AnalysisViewModel.AnalysisResultData) {
    when (data.feature) {
        AIFeature.WATER_ANALYSIS -> WaterResultCard(data.result as WaterIntakeAnalysis)
        AIFeature.EXERCISE_ANALYSIS -> ExerciseResultCard(data.result as ExerciseAnalysis)
        AIFeature.SLEEP_ANALYSIS -> SleepResultCard(data.result as SleepAnalysis)
        AIFeature.HABIT_ANALYSIS -> HabitResultCard(data.result as HabitAnalysis)
        else -> Text("不支持的分析类型")
    }
}

/** 饮水分析结果卡片 */
@Composable
private fun WaterResultCard(result: WaterIntakeAnalysis) {
    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("饮水分析", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            // 每日平均饮水量
            MetricRow(
                label = "每日平均",
                value = "${result.dailyAverageMl.toInt()} ml",
                target = "${result.targetMl.toInt()} ml"
            )
            val progress = (result.dailyAverageMl / result.targetMl).toFloat().coerceIn(0f, 1f)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())

            // 时间分布
            Text("时间分布", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            DistributionBar("早上", result.timeDistribution.morningRatio)
            DistributionBar("下午", result.timeDistribution.afternoonRatio)
            DistributionBar("晚上", result.timeDistribution.eveningRatio)

            // 规律性评分
            ScoreRow(label = "规律性评分", score = result.regularityScore)

            // 建议
            if (result.suggestions.isNotEmpty()) {
                SuggestionsSection(result.suggestions)
            }
        }
    }
}

/** 运动分析结果卡片 */
@Composable
private fun ExerciseResultCard(result: ExerciseAnalysis) {
    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("运动分析", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            MetricRow(label = "总运动时长", value = "${result.totalDurationMinutes} 分钟")
            MetricRow(label = "总消耗热量", value = "${result.totalCalories.toInt()} 大卡")
            MetricRow(label = "日均时长", value = "${"%.1f".format(result.averageDurationPerDay)} 分钟")

            // 趋势
            val trendLabel = when (result.trend) {
                ExerciseTrend.INCREASING -> "📈 上升"
                ExerciseTrend.STABLE -> "➡️ 平稳"
                ExerciseTrend.DECREASING -> "📉 下降"
            }
            MetricRow(label = "趋势", value = trendLabel)

            if (result.effect.isNotBlank()) {
                Text("运动效果", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(result.effect, style = MaterialTheme.typography.bodyMedium)
            }

            if (result.planSuggestion.isNotBlank()) {
                Text("计划建议", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(result.planSuggestion, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** 睡眠分析结果卡片 */
@Composable
private fun SleepResultCard(result: SleepAnalysis) {
    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("睡眠分析", style = MaterialTheme.typography.titleMedium, FontWeight.SemiBold)

            val hours = result.averageDurationMinutes / 60.0
            MetricRow(label = "平均睡眠时长", value = "${"%.1f".format(hours)} 小时")
            ScoreRow(label = "睡眠质量评分", score = result.qualityScore)
            ScoreRow(label = "规律性评分", score = result.regularityScore)

            if (result.bedtimePattern.isNotBlank()) {
                Text("入睡模式", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(result.bedtimePattern, style = MaterialTheme.typography.bodyMedium)
            }

            if (result.suggestions.isNotEmpty()) {
                SuggestionsSection(result.suggestions)
            }
        }
    }
}

/** 习惯分析结果卡片 */
@Composable
private fun HabitResultCard(result: HabitAnalysis) {
    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("习惯分析", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            ScoreRow(label = "坚持度", score = result.consistencyScore)
            MetricRow(label = "当前连续天数", value = "${result.currentStreakDays} 天")
            MetricRow(label = "最长连续天数", value = "${result.longestStreakDays} 天")
            MetricRow(label = "成功率", value = "${(result.successRate * 100).toInt()}%")

            if (result.encouragement.isNotBlank()) {
                GlassCard(level = GlassLevel.LIGHT, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "💪 ${result.encouragement}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (result.suggestions.isNotEmpty()) {
                SuggestionsSection(result.suggestions)
            }
        }
    }
}

/** 度量行：左标签右值 */
@Composable
private fun MetricRow(label: String, value: String, target: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (target != null) {
            Text("$value / $target", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        } else {
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** 评分行：标签 + 进度条 + 分数 */
@Composable
private fun ScoreRow(label: String, score: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$score / 100", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** 时间分布条 */
@Composable
private fun DistributionBar(label: String, ratio: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${(ratio * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

/** 建议列表区块 */
@Composable
private fun SuggestionsSection(suggestions: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("AI 建议", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        suggestions.forEachIndexed { index, suggestion ->
            Text(
                text = "${index + 1}. $suggestion",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
