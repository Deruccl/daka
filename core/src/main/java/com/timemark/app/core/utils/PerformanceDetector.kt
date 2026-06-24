package com.timemark.app.core.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build

/**
 * 设备性能等级
 *
 * 用于决定液态玻璃效果的渲染策略：
 * - LOW：低性能设备，使用半透明纯色背景（无模糊），保留高光和边框
 * - MEDIUM：中等性能，降低模糊半径（10dp）
 * - HIGH：高性能，完整效果（完整模糊 + 折射 + 噪点 + 水波纹）
 */
enum class PerformanceLevel {
    LOW,     // 低性能：无模糊，纯色半透明
    MEDIUM,  // 中等性能：降低模糊半径
    HIGH     // 高性能：完整效果
}

/**
 * 性能检测阈值常量
 */
private object PerformanceThresholds {
    /** 低性能：CPU 核数低于此值 */
    const val LOW_CPU_CORES = 4

    /** 中等性能：CPU 核数低于此值 */
    const val MEDIUM_CPU_CORES = 8

    /** 低性能：内存低于此值（MB） */
    const val LOW_MEMORY_MB = 2048L

    /** 中等性能：内存低于此值（MB） */
    const val MEDIUM_MEMORY_MB = 4096L

    /** 低性能：Android 版本低于此值 */
    const val LOW_API_LEVEL = 29

    /** 中等性能：Android 版本低于此值 */
    const val MEDIUM_API_LEVEL = 31
}

/**
 * 设备性能检测器（单例）
 *
 * 根据以下指标综合评估设备性能：
 * - CPU 核数（Runtime.availableProcessors）
 * - 内存大小（ActivityManager.MemoryInfo）
 * - Android 版本（Build.VERSION.SDK_INT）
 * - GPU 信息（通过 ActivityManager 间接判断）
 *
 * 检测结果会被缓存，避免重复计算。
 * 首次调用后结果固定，除非调用 [reset] 重置。
 *
 * 使用方式：
 * ```
 * val level = PerformanceDetector.detectDevicePerformance(context)
 * when (level) {
 *     PerformanceLevel.LOW -> { /* 降级方案 */ }
 *     PerformanceLevel.MEDIUM -> { /* 中等效果 */ }
 *     PerformanceLevel.HIGH -> { /* 完整效果 */ }
 * }
 * ```
 */
object PerformanceDetector {

    @Volatile
    private var cachedLevel: PerformanceLevel? = null

    /**
     * 检测设备性能等级
     *
     * 综合考虑 CPU、内存、Android 版本等指标。
     * 结果会被缓存，后续调用直接返回缓存值。
     *
     * @param context 上下文
     * @return 性能等级
     */
    fun detectDevicePerformance(context: Context): PerformanceLevel {
        cachedLevel?.let { return it }

        val level = calculatePerformanceLevel(context)
        cachedLevel = level
        return level
    }

    /**
     * 计算设备性能等级（实际检测逻辑）
     *
     * 评分策略：取各项指标的最低等级作为最终结果（短板效应）。
     */
    private fun calculatePerformanceLevel(context: Context): PerformanceLevel {
        // 1. CPU 核数
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val cpuLevel = when {
            cpuCores < PerformanceThresholds.LOW_CPU_CORES -> PerformanceLevel.LOW
            cpuCores < PerformanceThresholds.MEDIUM_CPU_CORES -> PerformanceLevel.MEDIUM
            else -> PerformanceLevel.HIGH
        }

        // 2. 内存大小
        val memoryLevel = detectMemoryLevel(context)

        // 3. Android 版本（RenderEffect 需要 API 31+，低版本直接降级）
        val apiLevel = when {
            Build.VERSION.SDK_INT < PerformanceThresholds.LOW_API_LEVEL -> PerformanceLevel.LOW
            Build.VERSION.SDK_INT < PerformanceThresholds.MEDIUM_API_LEVEL -> PerformanceLevel.MEDIUM
            else -> PerformanceLevel.HIGH
        }

        // 4. GPU 信息（通过 ActivityManager 判断是否为低内存设备）
        val gpuLevel = if (isLowMemoryDevice(context)) {
            PerformanceLevel.LOW
        } else {
            PerformanceLevel.HIGH
        }

        // 取最低等级（短板效应）
        return listOf(cpuLevel, memoryLevel, apiLevel, gpuLevel).minByOrNull { it.ordinal }
            ?: PerformanceLevel.MEDIUM
    }

    /**
     * 检测内存等级
     */
    private fun detectMemoryLevel(context: Context): PerformanceLevel {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return PerformanceLevel.MEDIUM
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val totalMemoryMb = memoryInfo.totalMem / (1024 * 1024)

            when {
                totalMemoryMb < PerformanceThresholds.LOW_MEMORY_MB -> PerformanceLevel.LOW
                totalMemoryMb < PerformanceThresholds.MEDIUM_MEMORY_MB -> PerformanceLevel.MEDIUM
                else -> PerformanceLevel.HIGH
            }
        } catch (e: Exception) {
            // 检测失败时默认中等
            PerformanceLevel.MEDIUM
        }
    }

    /**
     * 判断是否为低内存设备（系统标记）
     *
     * ActivityManager.isLowRamDevice 为 true 表示设备内存较小（通常 <= 1GB），
     * 此类设备应使用最简效果。
     */
    private fun isLowMemoryDevice(context: Context): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            activityManager?.isLowRamDevice == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 重置缓存
     *
     * 用于测试或需要重新检测的场景。
     */
    fun reset() {
        cachedLevel = null
    }

    /**
     * 获取当前缓存的性能等级（不触发检测）
     *
     * @return 缓存的性能等级，未检测过返回 null
     */
    fun getCachedLevel(): PerformanceLevel? = cachedLevel
}
