package com.timemark.app.domain.model

import kotlinx.serialization.Serializable

/**
 * 饮水习惯分析结果
 *
 * 用于展示用户一段时间内的饮水习惯分析，包含：
 * - 每日平均饮水量
 * - 饮水时间分布（早/中/晚）
 * - 规律性评分（0-100）
 * - 个性化建议
 */
@Serializable
data class WaterIntakeAnalysis(
    val dailyAverageMl: Double,            // 每日平均饮水量（毫升）
    val targetMl: Double = 2000.0,         // 每日目标饮水量
    val timeDistribution: WaterTimeDistribution = WaterTimeDistribution(),
    val regularityScore: Int = 0,          // 规律性评分 0-100
    val suggestions: List<String> = emptyList()
)

/** 饮水时间分布（按时间段统计占比） */
@Serializable
data class WaterTimeDistribution(
    val morningRatio: Double = 0.0,   // 早上（5-12）占比
    val afternoonRatio: Double = 0.0, // 下午（12-18）占比
    val eveningRatio: Double = 0.0    // 晚上（18-24）占比
)

/**
 * 运动数据分析结果
 *
 * 用于展示用户运动习惯与效果：
 * - 运动效果评价
 * - 趋势分析（上升/平稳/下降）
 * - 计划建议
 */
@Serializable
data class ExerciseAnalysis(
    val totalDurationMinutes: Int = 0,    // 总运动时长（分钟）
    val totalCalories: Double = 0.0,      // 总消耗热量
    val averageDurationPerDay: Double = 0.0, // 日均运动时长
    val effect: String = "",              // 运动效果描述
    val trend: ExerciseTrend = ExerciseTrend.STABLE,
    val planSuggestion: String = ""       // 运动计划建议
)

/** 运动趋势 */
enum class ExerciseTrend {
    INCREASING,  // 上升
    STABLE,      // 平稳
    DECREASING   // 下降
}

/**
 * 睡眠质量分析结果
 *
 * 用于展示用户睡眠情况：
 * - 平均睡眠时长
 * - 睡眠质量评分
 * - 规律性
 * - 改善建议
 */
@Serializable
data class SleepAnalysis(
    val averageDurationMinutes: Int = 0,  // 平均睡眠时长（分钟）
    val qualityScore: Int = 0,            // 睡眠质量评分 0-100
    val regularityScore: Int = 0,         // 规律性评分 0-100
    val bedtimePattern: String = "",     // 入睡时间模式描述
    val suggestions: List<String> = emptyList()
)

/**
 * 习惯养成分析结果
 *
 * 用于展示用户习惯坚持情况：
 * - 坚持度（0-100）
 * - 连续天数
 * - 成功率
 * - 建议
 * - 鼓励话语
 */
@Serializable
data class HabitAnalysis(
    val consistencyScore: Int = 0,        // 坚持度 0-100
    val currentStreakDays: Int = 0,       // 当前连续天数
    val longestStreakDays: Int = 0,       // 最长连续天数
    val successRate: Double = 0.0,        // 成功率 0-1
    val suggestions: List<String> = emptyList(),
    val encouragement: String = ""        // 鼓励话语
)
