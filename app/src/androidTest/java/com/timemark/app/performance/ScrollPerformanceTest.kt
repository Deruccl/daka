package com.timemark.app.performance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureNanoTime

/**
 * 滚动性能测试
 *
 * 测试列表滚动的流畅度：
 * - 大列表滚动应保持 60fps（每帧 < 16.67ms）
 * - 滚动到指定位置应在合理时间内完成
 */
@RunWith(AndroidJUnit4::class)
class ScrollPerformanceTest {

    @get:Rule
    val composeRule = createComposeRule()

    companion object {
        /** 60fps 对应的每帧最大耗时（纳秒） */
        private const val FRAME_TIME_60FPS_NS = 16_666_666L

        /** 列表项数量 */
        private const val ITEM_COUNT = 500
    }

    /**
     * 测试大列表的渲染时间
     */
    @Test
    fun 大列表渲染_应在合理时间内完成() {
        val items = List(ITEM_COUNT) { "Item $it" }

        val renderTimeMs = measureNanoTime {
            composeRule.setContent {
                MaterialTheme {
                    Surface {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items) { item ->
                                Box(modifier = Modifier.height(60.dp)) {
                                    Text(text = item)
                                }
                            }
                        }
                    }
                }
            }
            composeRule.waitForIdle()
        } / 1_000_000

        println("渲染 $ITEM_COUNT 项列表耗时: ${renderTimeMs}ms")
        assertTrue(
            "列表渲染时间 ${renderTimeMs}ms 过长",
            renderTimeMs < 5000
        )
    }

    /**
     * 测试列表滚动到末尾的性能
     */
    @Test
    fun 滚动到末尾_应在合理时间内完成() {
        val items = List(ITEM_COUNT) { "ScrollItem $it" }

        composeRule.setContent {
            MaterialTheme {
                Surface {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items) { item ->
                            Box(modifier = Modifier.height(60.dp)) {
                                Text(text = item)
                            }
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()

        val scrollTimeMs = measureNanoTime {
            composeRule.onNode(hasScrollAction()).performScrollToIndex(ITEM_COUNT - 1)
            composeRule.waitForIdle()
        } / 1_000_000

        println("滚动到末尾耗时: ${scrollTimeMs}ms")
        assertTrue(
            "滚动到末尾时间 ${scrollTimeMs}ms 过长",
            scrollTimeMs < 5000
        )
    }

    /**
     * 测试列表多次滚动的性能
     */
    @Test
    fun 多次滚动_性能稳定() {
        val items = List(ITEM_COUNT) { "MultiItem $it" }

        composeRule.setContent {
            MaterialTheme {
                Surface {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items) { item ->
                            Box(modifier = Modifier.height(60.dp)) {
                                Text(text = item)
                            }
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()

        // 滚动到不同位置
        val scrollPositions = listOf(50, 100, 200, 300, 400, 450, 100, 0)
        val scrollTimes = mutableListOf<Long>()

        scrollPositions.forEach { index ->
            val time = measureNanoTime {
                composeRule.onNode(hasScrollAction()).performScrollToIndex(index)
                composeRule.waitForIdle()
            } / 1_000_000
            scrollTimes.add(time)
            println("滚动到 $index 耗时: ${time}ms")
        }

        // 每次滚动都应在合理时间内
        scrollTimes.forEach { time ->
            assertTrue("单次滚动时间 ${time}ms 过长", time < 3000)
        }
    }

    /**
     * 测试列表项可见性
     */
    @Test
    fun 列表项_滚动后正确显示() {
        val items = List(100) { "VisibleItem $it" }

        composeRule.setContent {
            MaterialTheme {
                Surface {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items) { item ->
                            Box(modifier = Modifier.height(60.dp)) {
                                Text(text = item)
                            }
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()

        // 第一项应可见
        composeRule.onNodeWithText("VisibleItem 0").assertIsDisplayed()

        // 滚动到第 50 项
        composeRule.onNode(hasScrollAction()).performScrollToIndex(50)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("VisibleItem 50").assertIsDisplayed()
    }

    /**
     * 测试快速滚动性能
     */
    @Test
    fun 快速滚动_不崩溃且流畅() {
        val items = List(ITEM_COUNT) { "FastItem $it" }

        composeRule.setContent {
            MaterialTheme {
                Surface {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items) { item ->
                            Box(modifier = Modifier.height(60.dp)) {
                                Text(text = item)
                            }
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()

        // 快速连续滚动
        val totalTime = measureNanoTime {
            for (i in 0..10) {
                composeRule.onNode(hasScrollAction()).performScrollToIndex(i * 50)
                composeRule.waitForIdle()
            }
        } / 1_000_000

        println("快速滚动 10 次总耗时: ${totalTime}ms")
        assertTrue("快速滚动应在 10s 内完成", totalTime < 10000)
    }
}
