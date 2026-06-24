package com.timemark.app.data.mapper

import com.timemark.app.data.db.entity.RecordEntity
import com.timemark.app.data.util.JsonUtils
import com.timemark.app.domain.model.Record

/**
 * Record 实体与 Domain 模型互转。
 * images 和 tags 以 JSON 数组字符串存储。
 */
fun RecordEntity.toDomain(): Record = Record(
    id = id,
    trackerId = trackerId,
    value = value,
    date = date,
    time = time,
    timestamp = timestamp,
    note = note,
    images = JsonUtils.decodeStringList(images),
    tags = JsonUtils.decodeStringList(tags),
    mood = mood,
    duration = duration,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Record.toEntity(): RecordEntity = RecordEntity(
    id = id,
    trackerId = trackerId,
    value = value,
    date = date,
    time = time,
    timestamp = timestamp,
    note = note,
    images = JsonUtils.encodeStringList(images),
    tags = JsonUtils.encodeStringList(tags),
    mood = mood,
    duration = duration,
    createdAt = createdAt,
    updatedAt = updatedAt
)
