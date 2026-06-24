package com.timemark.app.domain.model

import kotlinx.serialization.Serializable

/** 范围统计 */
@Serializable
data class RangeStats(
    val trackerId: Long,
    val startDate: String,
    val endDate: String,
    val totalValue: Double,
    val totalCount: Int,
    val avgValue: Double,
    val maxValue: Double,
    val minValue: Double,
    val completedDays: Int,
    val totalDays: Int,
    val completionRate: Float,      // 完成率 0-1
    val streak: Int,                // 连续天数
    val dailyValues: List<DailyValue>  // 每日数值
)

/** 每日数值 */
@Serializable
data class DailyValue(
    val date: String,
    val value: Double,
    val count: Int,
    val completed: Boolean
)

/** 时间视图级别 */
enum class TimeViewLevel {
    MINUTE,  // 分钟
    HOUR,    // 小时
    DAY,     // 日
    WEEK,    // 周
    MONTH,   // 月
    YEAR;    // 年

    /**
     * 返回更细一级的视图级别（粒度更细）。
     * - YEAR → MONTH → WEEK → DAY → HOUR → MINUTE
     * - 已是最细粒度 MINUTE 时返回 null
     */
    fun finer(): TimeViewLevel? = when (this) {
        YEAR -> MONTH
        MONTH -> WEEK
        WEEK -> DAY
        DAY -> HOUR
        HOUR -> MINUTE
        MINUTE -> null
    }

    /**
     * 返回更粗一级的视图级别（粒度更粗）。
     * - MINUTE → HOUR → DAY → WEEK → MONTH → YEAR
     * - 已是最粗粒度 YEAR 时返回 null
     */
    fun coarser(): TimeViewLevel? = when (this) {
        MINUTE -> HOUR
        HOUR -> DAY
        DAY -> WEEK
        WEEK -> MONTH
        MONTH -> YEAR
        YEAR -> null
    }
}

/** 打卡模板 */
@Serializable
data class TrackerTemplate(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: TrackerType,
    val unit: String,
    val targetValue: Double,
    val description: String,
    val category: String  // 健康/学习/生活/工作
)
