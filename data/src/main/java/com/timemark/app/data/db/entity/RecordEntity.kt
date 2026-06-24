package com.timemark.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 打卡记录实体表。
 * 通过外键关联 TrackerEntity，删除 Tracker 时级联删除其所有记录。
 */
@Entity(
    tableName = "records",
    indices = [
        Index("tracker_id"),
        Index("date"),
        Index("timestamp"),
        Index(value = ["tracker_id", "date"])
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
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tracker_id") val trackerId: Long,
    val value: Double,
    val date: String,          // "yyyy-MM-dd"
    val time: String,          // "HH:mm"
    val timestamp: Long,
    val note: String,
    val images: String,        // JSON 数组
    val tags: String,          // JSON 数组
    val mood: String?,
    val duration: Long,
    val createdAt: Long,
    val updatedAt: Long
)
