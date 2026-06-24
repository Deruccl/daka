package com.timemark.app.performance

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureNanoTime

/**
 * 启动性能测试
 *
 * 测试应用启动相关的性能指标：
 * - Compose UI 初始化时间 < 1s
 * - 主题渲染时间 < 300ms
 * - 多次初始化的平均时间
 *
 * 注意：完整的冷启动/热启动测试需要使用 Macrobenchmark 库。
 * 此测试验证核心组件的初始化性能。
 */
@RunWith(AndroidJUnit4::class)
class StartupPerformanceTest {

    @get:Rule
    val composeRule = createComposeRule()

    companion object {
        /** 冷启动模拟阈值（毫秒） */
        private const val COLD_START_THRESHOLD_MS = 1000L

        /** 热启动模拟阈值（毫秒） */
        private const val WARM_START_THRESHOLD_MS = 300L
    }

    /**
     * 测试 Compose UI 首次初始化时间（模拟冷启动）
     *
     * 首次 setContent 包含 Compose 运行时初始化、主题加载等开销。
     */
    @Test
    fun compose首次初始化_应小于1秒() {
        val initTimeMs = measureNanoTime {
            composeRule.setContent {
                MaterialTheme {
                    Surface {
                        Text("启动测试")
                    }
                }
            }
            composeRule.waitForIdle()
        } / 1_000_000

        println("Compose 首次初始化耗时: ${initTimeMs}ms")
        assertTrue(
            "Compose 首次初始化时间 ${initTimeMs}ms 超过阈值 ${COLD_START_THRESHOLD_MS}ms",
            initTimeMs < COLD_START_THRESHOLD_MS
        )
    }

    /**
     * 测试 Compose UI 重新渲染时间（模拟热启动）
     *
     * 已初始化后的重新渲染应更快。
     */
    @Test
    fun compose重新渲染_应小于300ms() {
        // 先初始化
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    Text("初始化")
                }
            }
        }
        composeRule.waitForIdle()

        // 测量重新渲染时间
        val rerenderTimeMs = measureNanoTime {
            composeRule.setContent {
                MaterialTheme {
                    Surface {
                        Text("重新渲染")
                    }
                }
            }
            composeRule.waitForIdle()
        } / 1_000_000

        println("Compose 重新渲染耗时: ${rerenderTimeMs}ms")
        assertTrue(
            "Compose 重新渲染时间 ${rerenderTimeMs}ms 超过阈值 ${WARM_START_THRESHOLD_MS}ms",
            rerenderTimeMs < WARM_START_THRESHOLD_MS
        )
    }

    /**
     * 测试多次初始化的平均时间
     */
    @Test
    fun 多次初始化_平均时间应小于1秒() {
        val iterations = 3
        var totalTime = 0L

        repeat(iterations) { i ->
            val time = measureNanoTime {
                composeRule.setContent {
                    MaterialTheme {
                        Surface {
                            Text("第 ${i + 1} 次初始化")
                        }
                    }
                }
                composeRule.waitForIdle()
            } / 1_000_000
            println("第 ${i + 1} 次初始化耗时: ${time}ms")
            totalTime += time
        }

        val avgTime = totalTime / iterations
        println("平均初始化耗时: ${avgTime}ms")
        assertTrue(
            "平均初始化时间 ${avgTime}ms 超过阈值 ${COLD_START_THRESHOLD_MS}ms",
            avgTime < COLD_START_THRESHOLD_MS
        )
    }

    /**
     * 测试主题切换性能
     */
    @Test
    fun 主题渲染_应快速完成() {
        val renderTimeMs = measureNanoTime {
            composeRule.setContent {
                MaterialTheme {
                    Surface {
                        Text("主题测试")
                    }
                }
            }
            composeRule.waitForIdle()
        } / 1_000_000

        println("主题渲染耗时: ${renderTimeMs}ms")
        assertTrue(
            "主题渲染时间 ${renderTimeMs}ms 过长",
            renderTimeMs < COLD_START_THRESHOLD_MS
        )
    }

    /**
     * 测试复杂 Composable 树的初始化时间
     */
    @Test
    fun 复合Composable初始化_应小于1秒() {
        val initTimeMs = measureNanoTime {
            composeRule.setContent {
                MaterialTheme {
                    Surface {
                        ComplexComposable()
                    }
                }
            }
            composeRule.waitForIdle()
        } / 1_000_000

        println("复合 Composable 初始化耗时: ${initTimeMs}ms")
        assertTrue(
            "复合 Composable 初始化时间 ${initTimeMs}ms 超过阈值 ${COLD_START_THRESHOLD_MS}ms",
            initTimeMs < COLD_START_THRESHOLD_MS
        )
    }

    /**
     * 测试嵌套 Composable 的渲染性能
     */
    @Test
    fun 嵌套Composable渲染_应快速完成() {
        val renderTimeMs = measureNanoTime {
            composeRule.setContent {
                MaterialTheme {
                    Surface {
                        NestedComposable(depth = 5)
                    }
                }
            }
            composeRule.waitForIdle()
        } / 1_000_000

        println("嵌套 Composable 渲染耗时: ${renderTimeMs}ms")
        assertTrue(
            "嵌套 Composable 渲染时间 ${renderTimeMs}ms 过长",
            renderTimeMs < COLD_START_THRESHOLD_MS
        )
    }

    /**
     * 复合 Composable，包含多个子组件
     */
    @Composable
    private fun ComplexComposable() {
        androidx.compose.foundation.layout.Column {
            repeat(10) { index ->
                Text("组件 $index")
            }
        }
    }

    /**
     * 嵌套 Composable，模拟深层组件树
     */
    @Composable
    private fun NestedComposable(depth: Int) {
        if (depth > 0) {
            androidx.compose.foundation.layout.Column {
                Text("层级 $depth")
                NestedComposable(depth - 1)
            }
        }
    }
}
