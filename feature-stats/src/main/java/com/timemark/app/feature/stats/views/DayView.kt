package com.timemark.app.feature.stats.views

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timemark.app.core.ui.components.EmptyState
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.utils.ColorUtils
import com.timemark.app.domain.model.DailyValue
import com.timemark.app.feature.stats.StatsViewModel
import com.timemark.app.feature.stats.components.CurrentTimeMarkerColor
import com.timemark.app.feature.stats.components.drawCurrentTimeHorizontalMarker
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 日视图
 *
 * 展示选中日期的打卡详情：
 * - 当日数值卡片
 * - 时间轴（早/中/晚三段）
 * - 当日亮点
 *
 * 未选择项目时展示全部项目的当日完成情况列表。
 * 当选中日期为今天时，在时间轴当前时间位置绘制红色横线标记。
 */
@Composable
fun DayView(viewModel: StatsViewModel) {
    val rangeStats by viewModel.rangeStats.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val trackers by viewModel.trackers.collectAsStateWithLifecycle()
    val selectedTrackerId by viewModel.selectedTrackerId.collectAsStateWithLifecycle()

    val dateStr = selectedDate.format(DateTimeFormatter.ISO_DATE)
    val primaryColor = MaterialTheme.colorScheme.primary
    // 当前时间（仅当选中日期为今天时才显示标记）
    val currentTime: LocalTime? = if (selectedDate == LocalDate.now()) LocalTime.now() else null

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 当日数值卡片
        item {
            DayValueCard(
                date = selectedDate,
                dailyValue = rangeStats?.dailyValues?.firstOrNull { it.date == dateStr },
                trackerName = trackers.firstOrNull { it.id == selectedTrackerId }?.name,
                trackerUnit = trackers.firstOrNull { it.id == selectedTrackerId }?.unit ?: "次"
            )
        }

        // 时间轴
        item {
            TimelineCard(
                dailyValue = rangeStats?.dailyValues?.firstOrNull { it.date == dateStr },
                color = primaryColor,
                currentTime = currentTime
            )
        }

        // 全部项目当日完成情况
        if (selectedTrackerId == null && trackers.isNotEmpty()) {
            item {
                Text(
                    text = "今日打卡项目",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(trackers, key = { it.id }) { tracker ->
                val trackerColor = remember(tracker.color, primaryColor) {
                    runCatching { Color(ColorUtils.parseColor(tracker.color)) }
                        .getOrDefault(primaryColor)
                }
                TrackerDayRow(
                    name = tracker.name,
                    icon = tracker.icon,
                    color = trackerColor,
                    targetValue = tracker.targetValue,
                    unit = tracker.unit,
                    hasTarget = tracker.hasTarget
                )
            }
        }

        // 空状态
        if (rangeStats == null && trackers.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Inbox,
                    title = "暂无数据",
                    description = "请先创建打卡项目并完成打卡"
                )
            }
        }
    }
}

/**
 * 当日数值卡片
 */
@Composable
private fun DayValueCard(
    date: LocalDate,
    dailyValue: DailyValue?,
    trackerName: String?,
    trackerUnit: String
) {
    GlassCard(
        level = GlassLevel.STANDARD,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (dailyValue != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "当日总值",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatVal(dailyValue.value)} $trackerUnit",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "打卡次数",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${dailyValue.count} 次",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (dailyValue.completed) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "已完成",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = if (trackerName == null) "今日暂无打卡记录"
                    else "$trackerName 今日暂无打卡记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 时间轴卡片：早/中/晚三段
 *
 * @param dailyValue 当日数值
 * @param color 主色
 * @param currentTime 当前时间（仅今天显示），非 null 时绘制红色横线标记
 */
@Composable
private fun TimelineCard(
    dailyValue: DailyValue?,
    color: Color,
    currentTime: LocalTime? = null
) {
    // 脉冲动画：透明度 0.6 ~ 1.0 循环，1 秒周期
    val infiniteTransition = rememberInfiniteTransition(label = "dayPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dayPulseAlpha"
    )

    // 时间轴覆盖 05:00 - 24:00（19 小时），计算当前时间在其中的比例
    val timelineStartMin = 5 * 60
    val timelineEndMin = 24 * 60
    val currentProgress: Float? = currentTime?.let {
        val nowMin = it.hour * 60 + it.minute
        ((nowMin - timelineStartMin).toFloat() / (timelineEndMin - timelineStartMin))
            .coerceIn(0f, 1f)
    }
    val currentTimeText = currentTime?.let {
        String.format("%02d:%02d", it.hour, it.minute)
    }

    GlassCard(
        level = GlassLevel.LIGHT,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "时间轴",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                // 当前时间文字（标记右侧）
                if (currentTimeText != null) {
                    Text(
                        text = "现在 $currentTimeText",
                        color = CurrentTimeMarkerColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // 时间轴内容 + 当前时间横线叠加
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    TimelineItem(
                        period = "早上",
                        time = "05:00 - 12:00",
                        color = color,
                        count = if (dailyValue != null) dailyValue.count / 3 else 0
                    )
                    TimelineItem(
                        period = "下午",
                        time = "12:00 - 18:00",
                        color = color,
                        count = if (dailyValue != null) (dailyValue.count + 1) / 3 else 0
                    )
                    TimelineItem(
                        period = "晚上",
                        time = "18:00 - 24:00",
                        color = color,
                        count = if (dailyValue != null) (dailyValue.count + 2) / 3 else 0,
                        isLast = true
                    )
                }
                // 当前时间红色横线标记（含光晕）
                if (currentProgress != null) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val markerY = size.height * currentProgress
                        drawCurrentTimeHorizontalMarker(
                            centerY = markerY,
                            left = 0f,
                            right = size.width,
                            color = CurrentTimeMarkerColor,
                            alpha = pulseAlpha,
                            lineWidth = 3f,
                            dotRadius = 6f,
                            glowRadius = 18f
                        )
                    }
                }
            }
        }
    }
}

/**
 * 时间轴单项
 */
@Composable
private fun TimelineItem(
    period: String,
    time: String,
    color: Color,
    count: Int,
    isLast: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // 左侧时间轴
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (count > 0) color else color.copy(alpha = 0.3f))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(color.copy(alpha = 0.3f))
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        // 右侧内容
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = period,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (count > 0) {
                Text(
                    text = "$count 次打卡",
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
    }
}

/**
 * 单个打卡项目当日行
 */
@Composable
private fun TrackerDayRow(
    name: String,
    icon: String,
    color: Color,
    targetValue: Double,
    unit: String,
    hasTarget: Boolean
) {
    GlassCard(
        level = GlassLevel.LIGHT,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (hasTarget) {
                    LinearProgressIndicator(
                        progress = { 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = color,
                        trackColor = color.copy(alpha = 0.1f)
                    )
                    Text(
                        text = "目标 ${formatVal(targetValue)} $unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** 格式化数值 */
private fun formatVal(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format("%.1f", value)
}
