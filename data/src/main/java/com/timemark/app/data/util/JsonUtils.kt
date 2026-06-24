package com.timemark.app.data.util

import kotlinx.serialization.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

/**
 * JSON 序列化工具。
 * 统一管理 Json 实例，提供常用类型的编解码方法。
 */
object JsonUtils {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** 编码任意可序列化对象为 JSON 字符串。 */
    inline fun <reified T> encode(value: T): String = json.encodeToString(value)

    /** 从 JSON 字符串解码为指定类型。 */
    inline fun <reified T> decode(text: String): T = json.decodeFromString(text)

    /** 将字符串列表编码为 JSON 数组字符串。 */
    fun encodeStringList(list: List<String>): String =
        json.encodeToString(ListSerializer(String.serializer()), list)

    /** 将 JSON 数组字符串解码为字符串列表。空字符串返回空列表。 */
    fun decodeStringList(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), text)
        }.getOrDefault(emptyList())
    }

    /** 将整数列表编码为 JSON 数组字符串。 */
    fun encodeIntList(list: List<Int>): String =
        json.encodeToString(ListSerializer(Int.serializer()), list)

    /** 将 JSON 数组字符串解码为整数列表。空字符串返回空列表。 */
    fun decodeIntList(text: String?): List<Int> {
        if (text.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(Int.serializer()), text)
        }.getOrDefault(emptyList())
    }

    /** 将字符串 Map 编码为 JSON 对象字符串。 */
    fun encodeStringMap(map: Map<String, String>): String =
        json.encodeToString(MapSerializer(String.serializer(), String.serializer()), map)

    /** 将 JSON 对象字符串解码为字符串 Map。空字符串返回空 Map。 */
    fun decodeStringMap(text: String?): Map<String, String> {
        if (text.isNullOrBlank()) return emptyMap()
        return runCatching {
            json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), text)
        }.getOrDefault(emptyMap())
    }
}
