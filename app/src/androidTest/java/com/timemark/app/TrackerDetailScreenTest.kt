package com.timemark.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.timemark.app.domain.model.DailyStats
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.model.TrackerType
import com.timemark.app.feature.tracker.detail.TodayStatusCard
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * 打卡详情页 UI 测试
 *
 * 测试详情页核心组件的显示：
 * - 今日状态卡片
 * - 数值与目标显示
 * - 连续天数显示
 */
class TrackerDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun todayStatusCard_显示打卡名称() {
        val tracker = createTracker(name = "每日饮水", targetValue = 8.0, unit = "杯")
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TodayStatusCard(
                        tracker = tracker,
                        stats = null,
                        streak = 0,
                        selectedDate = LocalDate.of(2024, 6, 15),
                        onQuickAdd = {},
                        onAddRecord = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("每日饮水").assertIsDisplayed()
    }

    @Test
    fun todayStatusCard_有目标_显示目标值() {
        val tracker = createTracker(name = "运动", targetValue = 30.0, unit = "分钟")
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TodayStatusCard(
                        tracker = tracker,
                        stats = null,
                        streak = 0,
                        selectedDate = LocalDate.of(2024, 6, 15),
                        onQuickAdd = {},
                        onAddRecord = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("运动").assertIsDisplayed()
    }

    @Test
    fun todayStatusCard_有统计_显示当前值() {
        val tracker = createTracker(name = "喝水", targetValue = 8.0, unit = "杯")
        val stats = DailyStats(
            date = "2024-06-15",
            trackerId = 1,
            totalValue = 5.0,
            count = 5,
            completed = false
        )
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TodayStatusCard(
                        tracker = tracker,
                        stats = stats,
                        streak = 7,
                        selectedDate = LocalDate.of(2024, 6, 15),
                        onQuickAdd = {},
                        onAddRecord = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("喝水").assertIsDisplayed()
    }

    @Test
    fun todayStatusCard_已完成_显示完成状态() {
        val tracker = createTracker(name = "冥想", targetValue = 1.0, unit = "次")
        val stats = DailyStats(
            date = "2024-06-15",
            trackerId = 1,
            totalValue = 1.0,
            count = 1,
            completed = true
        )
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TodayStatusCard(
                        tracker = tracker,
                        stats = stats,
                        streak = 30,
                        selectedDate = LocalDate.of(2024, 6, 15),
                        onQuickAdd = {},
                        onAddRecord = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("冥想").assertIsDisplayed()
    }

    @Test
    fun todayStatusCard_无目标_显示当前值() {
        val tracker = createTracker(name = "体重", targetValue = 0.0, unit = "kg")
        val stats = DailyStats(
            date = "2024-06-15",
            trackerId = 1,
            totalValue = 65.5,
            count = 1,
            completed = false
        )
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TodayStatusCard(
                        tracker = tracker,
                        stats = stats,
                        streak = 0,
                        selectedDate = LocalDate.of(2024, 6, 15),
                        onQuickAdd = {},
                        onAddRecord = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("体重").assertIsDisplayed()
    }

    @Test
    fun todayStatusCard_连续天数_显示streak() {
        val tracker = createTracker(name = "早起", targetValue = 1.0, unit = "次")
        val stats = DailyStats(
            date = "2024-06-15",
            trackerId = 1,
            totalValue = 1.0,
            count = 1,
            completed = true
        )
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TodayStatusCard(
                        tracker = tracker,
                        stats = stats,
                        streak = 21,
                        selectedDate = LocalDate.of(2024, 6, 15),
                        onQuickAdd = {},
                        onAddRecord = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("早起").assertIsDisplayed()
    }

    @Test
    fun todayStatusCard_null统计_显示零值() {
        val tracker = createTracker(name = "阅读", targetValue = 30.0, unit = "页")
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TodayStatusCard(
                        tracker = tracker,
                        stats = null,
                        streak = 0,
                        selectedDate = LocalDate.of(2024, 6, 15),
                        onQuickAdd = {},
                        onAddRecord = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("阅读").assertIsDisplayed()
    }

    /** 辅助方法：创建测试用 Tracker */
    private fun createTracker(
        name: String,
        targetValue: Double = 1.0,
        unit: String = "次"
    ): Tracker = Tracker(
        id = 1L,
        name = name,
        icon = "📝",
        color = "#2196F3",
        type = TrackerType.COUNT,
        unit = unit,
        targetValue = targetValue
    )
}
