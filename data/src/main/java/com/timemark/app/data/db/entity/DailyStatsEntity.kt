package com.timemark.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 每日统计实体表。
 * (tracker_id, date) 唯一约束，确保每个 Tracker 每天仅有一条统计。
 */
@Entity(
    tableName = "daily_stats",
    indices = [
        Index(value = ["tracker_id", "date"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = TrackerEntity::class,
            parentColumns = ["id"],
            childColumns = ["tracker_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyStatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tracker_id") val trackerId: Long,
    val date: String,
    val totalValue: Double,
    val count: Int,
    val completed: Boolean,
    val extra: String           // JSON
)
