package com.timemark.app.feature.stats

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.domain.model.TimeViewLevel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 日期导航
 *
 * 左右箭头切换时间段，中间显示当前时间段文本。
 * 非今天时显示"今天"快捷按钮。
 *
 * @param selectedDate 当前选中的日期
 * @param viewLevel 当前视图级别
 * @param onPrevious 上一段
 * @param onNext 下一段
 * @param onToday 回到今天
 */
@Composable
fun DateNavigator(
    selectedDate: LocalDate,
    viewLevel: TimeViewLevel,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val today = LocalDate.now()
    val isToday = isDateToday(selectedDate, viewLevel, today)
    val dateText = formatPeriodLabel(selectedDate, viewLevel)

    GlassCard(
        level = GlassLevel.LIGHT,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 上一段
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "上一段"
                )
            }
            // 当前时间段文本 + 今天按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!isToday) {
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = onToday,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp,
                            vertical = 0.dp
                        )
                    ) {
                        Text("今天", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            // 下一段
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "下一段"
                )
            }
        }
    }
}

/**
 * 根据视图级别判断当前时间段是否包含今天
 */
private fun isDateToday(date: LocalDate, level: TimeViewLevel, today: LocalDate): Boolean {
    return when (level) {
        TimeViewLevel.MINUTE, TimeViewLevel.HOUR, TimeViewLevel.DAY -> date == today
        TimeViewLevel.WEEK -> {
            val start = date.with(java.time.DayOfWeek.MONDAY)
            val end = start.plusDays(6)
            !today.isBefore(start) && !today.isAfter(end)
        }
        TimeViewLevel.MONTH -> date.year == today.year && date.month == today.month
        TimeViewLevel.YEAR -> date.year == today.year
    }
}

/**
 * 根据视图级别格式化时间段显示文字
 */
private fun formatPeriodLabel(date: LocalDate, level: TimeViewLevel): String {
    val zhLocale = Locale.CHINA
    return when (level) {
        TimeViewLevel.MINUTE, TimeViewLevel.HOUR, TimeViewLevel.DAY ->
            date.format(DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", zhLocale))
        TimeViewLevel.WEEK -> {
            val start = date.with(java.time.DayOfWeek.MONDAY)
            val end = start.plusDays(6)
            "${start.year}年 第${weekOfYear(start)}周 (${start.monthValue}.${start.dayOfMonth} - ${end.monthValue}.${end.dayOfMonth})"
        }
        TimeViewLevel.MONTH ->
            YearMonth.from(date).format(DateTimeFormatter.ofPattern("yyyy年M月", zhLocale))
        TimeViewLevel.YEAR ->
            "${date.year}年"
    }
}

/** 计算某日期是当年的第几周（以周一为一周开始） */
private fun weekOfYear(date: LocalDate): Int {
    val firstDay = date.withDayOfYear(1)
    val firstMonday = if (firstDay.dayOfWeek == java.time.DayOfWeek.MONDAY) {
        firstDay
    } else {
        firstDay.with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY))
    }
    val days = (date.toEpochDay() - firstMonday.toEpochDay()).toInt()
    return if (days < 0) 1 else days / 7 + 1
}
