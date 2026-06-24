package com.timemark.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 打卡项目实体表。
 * 对应 domain 层的 Tracker 模型，枚举与列表以字符串形式存储。
 */
@Entity(tableName = "trackers", indices = [Index("sort_order")])
data class TrackerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val color: String,
    val type: String,           // TrackerType.name
    val unit: String,
    val targetValue: Double,
    val description: String,
    val timePeriod: String,     // TimePeriod.name
    val customStartTime: String?,
    val customEndTime: String?,
    val isVisible: Boolean,
    val sortOrder: Int,
    val aiEnabled: Boolean,
    val reminderEnabled: Boolean,
    val reminderTime: String?,
    val reminderFrequency: String,  // ReminderFrequency.name
    val reminderIntervalHours: Int,
    val reminderDays: String,       // JSON 数组 [1,3,5]
    val createdAt: Long,
    val updatedAt: Long
)
