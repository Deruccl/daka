package com.timemark.app.domain.model

import kotlinx.serialization.Serializable

/** 打卡记录 */
@Serializable
data class Record(
    val id: Long = 0,
    val trackerId: Long,
    val value: Double = 1.0,        // 记录值
    val date: String,               // "yyyy-MM-dd"
    val time: String,               // "HH:mm"
    val timestamp: Long,            // 毫秒时间戳
    val note: String = "",
    val images: List<String> = emptyList(),  // 图片路径列表
    val tags: List<String> = emptyList(),
    val mood: String? = null,       // 心情（饮食打卡用）
    val duration: Long = 0,         // 时长（毫秒，计时型用）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
