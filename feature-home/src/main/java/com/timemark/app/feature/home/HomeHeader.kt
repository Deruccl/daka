package com.timemark.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.components.ProgressRing
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.utils.TimeUtils
import java.time.LocalDate

/**
 * 首页顶部区域
 *
 * 展示：
 * - 日期（可左右切换，非今天时显示"今天"按钮）
 * - 问候语（根据当前时间变化）
 * - 今日完成进度环
 */
@Composable
fun HomeHeader(
    state: HomeUiState.Loaded,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    GlassCard(
        level = GlassLevel.STANDARD,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 日期切换行
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.previousDay() },
                        // 无障碍：明确描述按钮作用
                        modifier = Modifier.semantics { contentDescription = "上一天" }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "上一天")
                    }
                    Text(
                        text = TimeUtils.formatDate(state.date),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick = { viewModel.nextDay() },
                        modifier = Modifier.semantics { contentDescription = "下一天" }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "下一天")
                    }
                    // 非今天时显示"今天"快捷按钮
                    if (state.date != LocalDate.now()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = { viewModel.goToToday() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.semantics { contentDescription = "回到今天" }
                        ) {
                            Text("今天", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                // 问候语
                Text(
                    text = TimeUtils.greeting(),
                    style = MaterialTheme.typography.headlineMedium
                )
                // 今日完成情况
                if (state.totalCount > 0) {
                    Text(
                        text = "今日已完成 ${state.totalCompleted}/${state.totalCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 进度环：添加无障碍内容描述，播报完成率
            val progressPercent = (state.completionRate * 100).toInt()
            ProgressRing(
                progress = state.completionRate,
                size = 80.dp,
                text = "$progressPercent%",
                modifier = Modifier.semantics {
                    contentDescription = "今日完成率 $progressPercent%，已完成 ${state.totalCompleted} 项，共 ${state.totalCount} 项"
                }
            )
        }
    }
}
