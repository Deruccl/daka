package com.timemark.app.domain.model

import kotlinx.serialization.Serializable

/** 每日统计 */
@Serializable
data class DailyStats(
    val id: Long = 0,
    val date: String,               // "yyyy-MM-dd"
    val trackerId: Long,
    val totalValue: Double = 0.0,   // 当日总值
    val count: Int = 0,             // 记录次数
    val completed: Boolean = false, // 是否完成目标
    val extra: Map<String, String> = emptyMap()  // 额外统计
)
