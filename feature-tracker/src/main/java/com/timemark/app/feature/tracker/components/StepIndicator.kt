package com.timemark.app.feature.tracker.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.Primary

/**
 * 步骤指示器
 *
 * 由若干圆点 + 连接线组成，当前步骤圆点放大并高亮主色，
 * 已完成步骤使用主色填充，未到达步骤使用浅灰色。
 *
 * @param currentStep 当前步骤索引（从 0 开始）
 * @param totalSteps 总步骤数
 */
@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until totalSteps) {
            val isCurrent = i == currentStep
            val isCompleted = i < currentStep

            // 当前步骤的缩放动画
            val scale by animateFloatAsState(
                targetValue = if (isCurrent) 1.3f else 1f,
                animationSpec = tween(durationMillis = 250),
                label = "stepScale"
            )

            // 圆点
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrent -> Primary
                            isCompleted -> Primary.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 连接线（最后一个步骤不画线）
            if (i < totalSteps - 1) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (i < currentStep) Primary.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}
