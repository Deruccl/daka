package com.timemark.app.feature.ai.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.AIFeature

/**
 * 分析类型选择页面
 *
 * 展示 4 种分析类型卡片：
 * - 饮水分析
 * - 运动分析
 * - 睡眠分析
 * - 习惯分析
 *
 * 点击进入对应分析结果页。
 */
@Composable
fun AnalysisListScreen(navController: NavController) {
    val analysisTypes = listOf(
        AnalysisType(AIFeature.WATER_ANALYSIS, "饮水分析", "分析饮水量、时间分布与规律性", Icons.Default.LocalDrink),
        AnalysisType(AIFeature.EXERCISE_ANALYSIS, "运动分析", "分析运动效果、趋势与计划建议", Icons.Default.DirectionsRun),
        AnalysisType(AIFeature.SLEEP_ANALYSIS, "睡眠分析", "分析睡眠时长、质量与改善建议", Icons.Default.Bedtime),
        AnalysisType(AIFeature.HABIT_ANALYSIS, "习惯分析", "分析坚持度、成功率与鼓励话语", Icons.Default.Psychology)
    )

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "AI 分析",
                onBackClick = { navController.popBackStack() }
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
            item {
                GlassCard(
                    level = GlassLevel.LIGHT,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "智能数据分析",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "选择分析类型，AI 将基于最近 7 天的打卡记录生成个性化建议",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(analysisTypes) { type ->
                AnalysisTypeCard(type = type) {
                    navController.navigate("analysis_result/${type.feature.name}")
                }
            }
        }
    }
}

/** 分析类型卡片 */
@Composable
private fun AnalysisTypeCard(type: AnalysisType, onClick: () -> Unit) {
    GlassCard(
        level = GlassLevel.STANDARD,
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
                    imageVector = type.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 分析类型数据 */
private data class AnalysisType(
    val feature: AIFeature,
    val title: String,
    val description: String,
    val icon: ImageVector
)
