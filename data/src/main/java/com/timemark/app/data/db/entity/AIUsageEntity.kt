package com.timemark.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * AI 使用记录实体表。
 * 用于统计 Token 消耗、费用及调用成功率。
 */
@Entity(
    tableName = "ai_usage",
    indices = [
        Index("config_id"),
        Index("timestamp"),
        Index("feature")
    ]
)
data class AIUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "config_id") val configId: Long,
    val feature: String,        // AIFeature.name
    val tokensInput: Int,
    val tokensOutput: Int,
    val cost: Double,
    val timestamp: Long,
    val success: Boolean,
    val errorMessage: String?,
    val responseTimeMs: Long
)
