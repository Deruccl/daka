package com.timemark.app.data.db.converters

import androidx.room.TypeConverter
import com.timemark.app.data.util.JsonUtils

/**
 * Room 类型转换器。
 * 提供 List<String> <-> String(JSON) 的转换，供需要直接存储列表的实体使用。
 * 当前实体均以 String 存储 JSON，本转换器作为可选支持保留。
 */
class TypeConverters {
    @TypeConverter
    fun listToJson(value: List<String>?): String =
        if (value.isNullOrEmpty()) "[]" else JsonUtils.encodeStringList(value)

    @TypeConverter
    fun jsonToList(value: String?): List<String> = JsonUtils.decodeStringList(value)
}
