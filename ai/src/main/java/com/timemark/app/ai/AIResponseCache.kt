package com.timemark.app.ai

import android.util.LruCache
import java.security.MessageDigest

/**
 * AI 响应缓存（Task 33.4）
 *
 * 基于 LruCache 实现的内存缓存：
 * - key: 请求提示词的 SHA-256 哈希
 * - value: 缓存的响应内容
 *
 * 缓存命中时直接返回，不消耗 Token。
 * 支持配置缓存开关和大小。
 */
class AIResponseCache(
    /** 最大缓存条数，默认 100 */
    maxSize: Int = DEFAULT_MAX_SIZE
) {

    /** LruCache 实例，按条数计数 */
    private val cache: LruCache<String, String> = LruCache(maxSize)

    /** 是否启用缓存 */
    @Volatile
    var enabled: Boolean = true

    /** 缓存命中次数（用于统计命中率） */
    @Volatile
    private var hitCount: Int = 0

    /** 缓存未命中次数 */
    @Volatile
    private var missCount: Int = 0

    /**
     * 查询缓存
     *
     * @param prompt 请求提示词
     * @return 命中时返回缓存的响应内容，未命中或缓存禁用时返回 null
     */
    fun get(prompt: String): String? {
        if (!enabled) return null
        val key = hashKey(prompt)
        val cached = cache.get(key)
        if (cached != null) {
            hitCount++
        } else {
            missCount++
        }
        return cached
    }

    /**
     * 写入缓存
     *
     * @param prompt 请求提示词
     * @param response AI 响应内容
     */
    fun put(prompt: String, response: String) {
        if (!enabled) return
        val key = hashKey(prompt)
        cache.put(key, response)
    }

    /** 清空缓存 */
    fun clear() {
        cache.evictAll()
        hitCount = 0
        missCount = 0
    }

    /** 当前缓存条数 */
    fun size(): Int = cache.size()

    /** 缓存命中率（0.0 - 1.0） */
    fun hitRate(): Double {
        val total = hitCount + missCount
        if (total == 0) return 0.0
        return hitCount.toDouble() / total
    }

    /** 获取统计信息 */
    fun stats(): CacheStats = CacheStats(
        size = size(),
        maxSize = cache.maxSize(),
        hitCount = hitCount,
        missCount = missCount,
        hitRate = hitRate()
    )

    /**
     * 计算提示词的 SHA-256 哈希作为缓存 key
     */
    private fun hashKey(prompt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(prompt.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /** 缓存统计信息 */
    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val hitCount: Int,
        val missCount: Int,
        val hitRate: Double
    )

    companion object {
        /** 默认最大缓存条数 */
        const val DEFAULT_MAX_SIZE = 100
    }
}
