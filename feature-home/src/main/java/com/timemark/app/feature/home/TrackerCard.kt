package com.timemark.app.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timemark.app.core.ui.animation.hoverElevation
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.utils.ColorUtils
import com.timemark.app.core.utils.HapticLevel
import com.timemark.app.core.utils.rememberHapticFeedback

/**
 * 打卡卡片
 *
 * 展示单个打卡项目的信息：
 * - 图标、名称、今日进度
 * - 快捷打卡按钮（+1）或完成标记
 * - 点击进入详情，长按进入编辑
 *
 * 已完成的卡片使用 LIGHT 玻璃层级，未完成使用 STANDARD。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackerCard(
    trackerWithStats: TrackerWithStats,
    onClick: () -> Unit,
    onQuickCheckIn: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tracker = trackerWithStats.tracker
    val isCompleted = trackerWithStats.isCompleted

    // 解析图标背景色，解析失败时回退到主色
    val primaryColor = MaterialTheme.colorScheme.primary
    val iconBackgroundColor = remember(tracker.color, primaryColor) {
        runCatching { Color(ColorUtils.parseColor(tracker.color)) }
            .getOrDefault(primaryColor)
    }

    // 触觉反馈控制器（卡片点击 MEDIUM，长按 STRONG）
    val haptic = rememberHapticFeedback()

    GlassCard(
        level = if (isCompleted) GlassLevel.LIGHT else GlassLevel.STANDARD,
        shape = RoundedCornerShape(16.dp),
        onClick = null,
        modifier = modifier
            .fillMaxWidth()
            // 卡片悬停效果：上浮 3dp + 缩放 1.02f，200ms 动画
            .hoverElevation()
            .combinedClickable(
                onClick = {
                    // 卡片点击：中等触觉反馈
                    haptic.performHaptic(HapticLevel.MEDIUM)
                    onClick()
                },
                onLongClick = {
                    // 长按：强烈触觉反馈
                    haptic.performHaptic(HapticLevel.STRONG)
                    onLongClick()
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tracker.icon,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 名称和进度
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tracker.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (tracker.hasTarget) {
                    Text(
                        text = "${trackerWithStats.currentValue}/${tracker.targetValue} ${tracker.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCompleted) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // 进度条
                    LinearProgressIndicator(
                        progress = { trackerWithStats.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = if (isCompleted) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    Text(
                        text = "今日 ${trackerWithStats.currentValue} ${tracker.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 快捷打卡按钮或完成标记
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已完成",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                GlassButton(
                    text = "+1",
                    onClick = onQuickCheckIn,
                    type = GlassButtonType.SMALL
                )
            }
        }
    }
}
