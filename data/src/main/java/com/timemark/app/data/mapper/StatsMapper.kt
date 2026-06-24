package com.timemark.app.data.mapper

import com.timemark.app.data.db.entity.DailyStatsEntity
import com.timemark.app.data.util.JsonUtils
import com.timemark.app.domain.model.DailyStats

/**
 * DailyStats 实体与 Domain 模型互转。
 * extra 字段以 JSON 对象字符串存储 Map<String, String>。
 */
fun DailyStatsEntity.toDomain(): DailyStats = DailyStats(
    id = id,
    date = date,
    trackerId = trackerId,
    totalValue = totalValue,
    count = count,
    completed = completed,
    extra = JsonUtils.decodeStringMap(extra)
)

fun DailyStats.toEntity(): DailyStatsEntity = DailyStatsEntity(
    id = id,
    trackerId = trackerId,
    date = date,
    totalValue = totalValue,
    count = count,
    completed = completed,
    extra = JsonUtils.encodeStringMap(extra)
)
