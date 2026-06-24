package com.timemark.app.domain.model

import kotlinx.serialization.Serializable

/** 打卡项目 */
@Serializable
data class Tracker(
    val id: Long = 0,
    val name: String,
    val icon: String,              // emoji 或图标名
    val color: String,             // 十六进制颜色 #RRGGBB
    val type: TrackerType,
    val unit: String = "",         // 单位（杯/分钟/kg/ml/次）
    val targetValue: Double = 0.0, // 每日目标值，0 表示无目标
    val description: String = "",
    val timePeriod: TimePeriod = TimePeriod.ALL_DAY,
    val customStartTime: String? = null,  // 自定义时间段开始 "HH:mm"
    val customEndTime: String? = null,    // 自定义时间段结束 "HH:mm"
    val isVisible: Boolean = true,        // 是否在首页显示
    val sortOrder: Int = 0,               // 排序位置
    val aiEnabled: Boolean = false,       // 是否启用 AI 分析
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null,     // "HH:mm"
    val reminderFrequency: ReminderFrequency = ReminderFrequency.DAILY,
    val reminderIntervalHours: Int = 2,   // 间隔提醒的小时数
    val reminderDays: List<Int> = emptyList(), // 每周提醒日（1-7，周一=1）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** 是否有目标 */
    val hasTarget: Boolean get() = targetValue > 0
}
