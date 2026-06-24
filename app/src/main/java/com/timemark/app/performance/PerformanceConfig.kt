package com.timemark.app.performance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 性能配置中心
 *
 * 集中管理应用性能相关配置：
 * - 协程调度器优化（按任务类型分配最优线程）
 * - Compose 性能优化开关
 * - 内存与帧率监控阈值
 *
 * 设计目标：
 * - 冷启动 < 1s（主线程仅执行必要初始化）
 * - 滑动帧率稳定 60fps
 * - 内存占用 < 256MB
 */
@Singleton
class PerformanceConfig @Inject constructor() {

    /**
     * 协程调度器配置
     *
     * 按任务类型分配调度器，避免主线程阻塞：
     * - Main：UI 更新、轻量级状态计算
     * - IO：数据库、网络、文件读写
     * - Default：CPU 密集型任务（图表计算、JSON 解析、图片压缩）
     * - Unconfined：极少数无需上下文的瞬时任务
     */
    data class DispatcherConfig(
        val main: CoroutineDispatcher = Dispatchers.Main,
        val io: CoroutineDispatcher = Dispatchers.IO,
        val default: CoroutineDispatcher = Dispatchers.Default
    )

    /** 当前调度器配置（可通过依赖注入替换为测试调度器） */
    val dispatchers: DispatcherConfig = DispatcherConfig()

    /**
     * Compose 性能优化配置
     *
     * - enableStrongSkippingMode：启用强跳过模式（相同参数的 Composable 跳过重组）
     * - enable instability detection：检测不稳定参数（编译期报告）
     * - reportMetrics：输出 Compose 编译指标到 build/compose_metrics
     */
    data class ComposePerformanceConfig(
        val enableStrongSkippingMode: Boolean = true,
        val reportMetrics: Boolean = true,
        val reportReports: Boolean = true
    )

    /** Compose 性能配置 */
    val compose: ComposePerformanceConfig = ComposePerformanceConfig()

    /**
     * 内存监控阈值（单位：MB）
     *
     * - warningThreshold：发出警告（日志记录）
     * - criticalThreshold：触发 GC 与缓存清理
     */
    data class MemoryThresholdConfig(
        val warningThresholdMb: Int = 192,
        val criticalThresholdMb: Int = 256
    )

    /** 内存阈值配置 */
    val memory: MemoryThresholdConfig = MemoryThresholdConfig()

    /**
     * 帧率监控配置
     *
     * - targetFrameRateMs：目标帧时间（16.6ms = 60fps）
     * - jankThresholdMs：卡顿判定阈值（超过则记录为 jank）
     */
    data class FrameRateConfig(
        val targetFrameRateMs: Long = 16L,
        val jankThresholdMs: Long = 32L
    )

    /** 帧率监控配置 */
    val frameRate: FrameRateConfig = FrameRateConfig()

    /**
     * 冷启动优化配置
     *
     * - maxColdStartMs：冷启动最大允许时间（毫秒）
     * - deferInitDelayMs：非关键初始化延迟时间（毫秒）
     */
    data class StartupConfig(
        val maxColdStartMs: Long = 1000L,
        val deferInitDelayMs: Long = 300L
    )

    /** 冷启动配置 */
    val startup: StartupConfig = StartupConfig()

    companion object {
        private const val TAG = "PerformanceConfig"
    }
}
