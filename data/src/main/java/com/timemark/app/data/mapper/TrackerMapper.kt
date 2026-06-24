package com.timemark.app.data.mapper

import com.timemark.app.data.db.entity.TrackerEntity
import com.timemark.app.data.util.JsonUtils
import com.timemark.app.domain.model.ReminderFrequency
import com.timemark.app.domain.model.TimePeriod
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.model.TrackerType

/**
 * Tracker 实体与 Domain 模型互转。
 * 枚举以 name 字符串存储，reminderDays 以 JSON 数组字符串存储。
 */
fun TrackerEntity.toDomain(): Tracker = Tracker(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = runCatching { TrackerType.valueOf(type) }.getOrDefault(TrackerType.COUNT),
    unit = unit,
    targetValue = targetValue,
    description = description,
    timePeriod = runCatching { TimePeriod.valueOf(timePeriod) }.getOrDefault(TimePeriod.ALL_DAY),
    customStartTime = customStartTime,
    customEndTime = customEndTime,
    isVisible = isVisible,
    sortOrder = sortOrder,
    aiEnabled = aiEnabled,
    reminderEnabled = reminderEnabled,
    reminderTime = reminderTime,
    reminderFrequency = runCatching {
        ReminderFrequency.valueOf(reminderFrequency)
    }.getOrDefault(ReminderFrequency.DAILY),
    reminderIntervalHours = reminderIntervalHours,
    reminderDays = JsonUtils.decodeIntList(reminderDays),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Tracker.toEntity(): TrackerEntity = TrackerEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type.name,
    unit = unit,
    targetValue = targetValue,
    description = description,
    timePeriod = timePeriod.name,
    customStartTime = customStartTime,
    customEndTime = customEndTime,
    isVisible = isVisible,
    sortOrder = sortOrder,
    aiEnabled = aiEnabled,
    reminderEnabled = reminderEnabled,
    reminderTime = reminderTime,
    reminderFrequency = reminderFrequency.name,
    reminderIntervalHours = reminderIntervalHours,
    reminderDays = JsonUtils.encodeIntList(reminderDays),
    createdAt = createdAt,
    updatedAt = updatedAt
)
