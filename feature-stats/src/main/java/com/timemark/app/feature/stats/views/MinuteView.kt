package com.timemark.app.feature.stats.views

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timemark.app.feature.stats.StatsViewModel
import com.timemark.app.feature.stats.components.CurrentTimeMarkerColor
import com.timemark.app.feature.stats.components.drawCurrentTimeVerticalMarker
import java.time.LocalDate
import java.time.LocalTime

/**
 * 分钟视图（简化实现）
 *
 * 展示当前小时的 1 小时时间轴，5 分钟一个刻度。
 * 当选中日期为今天时，在当前分钟位置绘制红色脉冲标记。
 */
@Composable
fun MinuteView(viewModel: StatsViewModel) {
    val rangeStats by viewModel.rangeStats.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val primaryColor = MaterialTheme.colorScheme.primary

    val stats = rangeStats
    val dateStr = selectedDate.toString()
    val dailyValue = stats?.dailyValues?.firstOrNull { it.date == dateStr }

    // 当前分钟（仅当选中日期为今天时才显示标记）
    val currentMinute: Float? = if (isSelectedDateToday(selectedDate)) {
        val now = LocalTime.now()
        (now.minute + now.second / 60f).coerceIn(0f, 60f)
    } else null

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ChartCard(title = "1 小时时间轴（5 分钟刻度）") {
                MinuteTimeline(
                    totalMinutes = 60,
                    intervalMinutes = 5,
                    color = primaryColor,
                    progress = if (dailyValue != null) 0.5f else 0f,
                    currentMinute = currentMinute
                )
            }
        }
        item {
            GlassCardLevelLight {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "分钟详情",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "当日打卡次数", value = "${dailyValue?.count ?: 0} 次")
                    StatRow(label = "刻度间隔", value = "5 分钟")
                    StatRow(label = "总刻度数", value = "12 个")
                }
            }
        }
    }
}

/** 判断选中日期是否为今天 */
private fun isSelectedDateToday(date: LocalDate): Boolean = date == LocalDate.now()

/**
 * 分钟时间轴：水平时间轴，5 分钟一个刻度
 *
 * @param totalMinutes 总分钟数
 * @param intervalMinutes 刻度间隔
 * @param color 主色
 * @param progress 当前进度 0..1
 * @param currentMinute 当前分钟位置（0..60），非 null 时绘制红色脉冲标记
 */
@Composable
private fun MinuteTimeline(
    totalMinutes: Int,
    intervalMinutes: Int,
    color: Color,
    progress: Float,
    currentMinute: Float? = null
) {
    val tickCount = totalMinutes / intervalMinutes
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    // 脉冲动画：透明度 0.6 ~ 1.0 循环，1 秒周期
    val infiniteTransition = rememberInfiniteTransition(label = "minutePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "minutePulseAlpha"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val w = size.width
            val h = size.height
            val axisY = h * 0.6f
            val padding = 16f
            val axisWidth = w - padding * 2

            // 主轴线
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(padding, axisY),
                end = Offset(w - padding, axisY),
                strokeWidth = 2f
            )

            // 进度填充
            val progressWidth = axisWidth * progress.coerceIn(0f, 1f)
            if (progressWidth > 0f) {
                drawLine(
                    color = color,
                    start = Offset(padding, axisY),
                    end = Offset(padding + progressWidth, axisY),
                    strokeWidth = 4f
                )
            }

            // 刻度
            for (i in 0..tickCount) {
                val x = padding + axisWidth * i / tickCount
                val isMajor = i % 3 == 0
                val tickH = if (isMajor) 12f else 6f
                drawLine(
                    color = if (isMajor) color else color.copy(alpha = 0.5f),
                    start = Offset(x, axisY - tickH / 2),
                    end = Offset(x, axisY + tickH / 2),
                    strokeWidth = if (isMajor) 2f else 1f
                )
            }

            // 当前位置圆点
            if (progress > 0f) {
                drawCircle(
                    color = color,
                    radius = 6f,
                    center = Offset(padding + progressWidth, axisY)
                )
            }

            // 当前时间红色脉冲标记（竖线 + 光晕）
            if (currentMinute != null) {
                val markerX = padding + axisWidth * (currentMinute / totalMinutes)
                drawCurrentTimeVerticalMarker(
                    centerX = markerX,
                    top = 0f,
                    bottom = h,
                    color = CurrentTimeMarkerColor,
                    alpha = pulseAlpha,
                    lineWidth = 3f,
                    dotRadius = 6f,
                    glowRadius = 20f
                )
            }
        }
        // "现在"标签 + 刻度标签
        if (currentMinute != null) {
            Text(
                text = "现在",
                color = CurrentTimeMarkerColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..tickCount step 3) {
                Text(
                    text = ":${String.format("%02d", i * intervalMinutes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }
        }
    }
}
