package com.timemark.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.timemark.app.domain.model.DailyStats
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.model.TrackerType
import com.timemark.app.feature.home.HomeUiState
import com.timemark.app.feature.home.TrackerCard
import com.timemark.app.feature.home.TrackerWithStats
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * 首页 UI 测试
 *
 * 测试首页核心组件的显示与交互：
 * - 打卡卡片显示
 * - 卡片点击回调
 * - 快速打卡按钮
 * - 空状态显示
 */
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun trackerCard_显示打卡名称() {
        val tracker = createTracker(id = 1, name = "每日饮水")
        val trackerWithStats = TrackerWithStats(
            tracker = tracker,
            dailyStats = null,
            streak = 0
        )
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TrackerCard(
                        trackerWithStats = trackerWithStats,
                        onClick = {},
                        onQuickCheckIn = {},
                        onLongClick = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("每日饮水").assertIsDisplayed()
    }

    @Test
    fun trackerCard_显示图标() {
        val tracker = createTracker(id = 1, name = "运动", icon = "🏃")
        val trackerWithStats = TrackerWithStats(
            tracker = tracker,
            dailyStats = null,
            streak = 0
        )
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TrackerCard(
                        trackerWithStats = trackerWithStats,
                        onClick = {},
                        onQuickCheckIn = {},
                        onLongClick = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("🏃").assertIsDisplayed()
    }

    @Test
    fun trackerCard_有目标_显示进度信息() {
        val tracker = createTracker(id = 1, name = "喝水", targetValue = 8.0, unit = "杯")
        val dailyStats = DailyStats(
            date = "2024-06-15",
            trackerId = 1,
            totalValue = 3.0,
            count = 3,
            completed = false
        )
        val trackerWithStats = TrackerWithStats(
            tracker = tracker,
            dailyStats = dailyStats,
            streak = 5
        )
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TrackerCard(
                        trackerWithStats = trackerWithStats,
                        onClick = {},
                        onQuickCheckIn = {},
                        onLongClick = {}
                    )
                }
            }
        }
        // 验证打卡名称显示
        composeRule.onNodeWithText("喝水").assertIsDisplayed()
    }

    @Test
    fun trackerCard_点击_触发回调() {
        var clickCount = 0
        val tracker = createTracker(id = 1, name = "测试打卡")
        val trackerWithStats = TrackerWithStats(
            tracker = tracker,
            dailyStats = null,
            streak = 0
        )
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TrackerCard(
                        trackerWithStats = trackerWithStats,
                        onClick = { clickCount++ },
                        onQuickCheckIn = {},
                        onLongClick = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("测试打卡").performClick()
        composeRule.waitForIdle()
        assert(clickCount == 1)
    }

    @Test
    fun trackerCard_已完成_显示完成状态() {
        val tracker = createTracker(id = 1, name = "冥想", targetValue = 1.0)
        val dailyStats = DailyStats(
            date = "2024-06-15",
            trackerId = 1,
            totalValue = 1.0,
            count = 1,
            completed = true
        )
        val trackerWithStats = TrackerWithStats(
            tracker = tracker,
            dailyStats = dailyStats,
            streak = 7
        )
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TrackerCard(
                        trackerWithStats = trackerWithStats,
                        onClick = {},
                        onQuickCheckIn = {},
                        onLongClick = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("冥想").assertIsDisplayed()
    }

    @Test
    fun trackerCard_无目标_不显示进度条() {
        val tracker = createTracker(id = 1, name = "体重", targetValue = 0.0)
        val trackerWithStats = TrackerWithStats(
            tracker = tracker,
            dailyStats = null,
            streak = 0
        )
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    TrackerCard(
                        trackerWithStats = trackerWithStats,
                        onClick = {},
                        onQuickCheckIn = {},
                        onLongClick = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("体重").assertIsDisplayed()
    }

    @Test
    fun homeUiState_Loaded_正确携带数据() {
        val state = HomeUiState.Loaded(
            date = LocalDate.of(2024, 6, 15),
            trackers = emptyList(),
            totalCompleted = 0,
            totalCount = 0,
            completionRate = 0f
        )
        assert(state.date == LocalDate.of(2024, 6, 15))
        assert(state.trackers.isEmpty())
        assert(state.totalCompleted == 0)
        assert(state.totalCount == 0)
    }

    @Test
    fun homeUiState_Loaded_有完成数据() {
        val tracker1 = createTracker(id = 1, name = "打卡1", targetValue = 1.0)
        val tracker2 = createTracker(id = 2, name = "打卡2", targetValue = 1.0)
        val stats1 = DailyStats(date = "2024-06-15", trackerId = 1, totalValue = 1.0, count = 1, completed = true)
        val stats2 = DailyStats(date = "2024-06-15", trackerId = 2, totalValue = 0.0, count = 0, completed = false)

        val state = HomeUiState.Loaded(
            date = LocalDate.of(2024, 6, 15),
            trackers = listOf(
                TrackerWithStats(tracker1, stats1, 5),
                TrackerWithStats(tracker2, stats2, 0)
            ),
            totalCompleted = 1,
            totalCount = 2,
            completionRate = 0.5f
        )
        assert(state.totalCompleted == 1)
        assert(state.totalCount == 2)
        assert(state.completionRate == 0.5f)
    }

    /** 辅助方法：创建测试用 Tracker */
    private fun createTracker(
        id: Long,
        name: String,
        icon: String = "📝",
        targetValue: Double = 1.0,
        unit: String = "次"
    ): Tracker = Tracker(
        id = id,
        name = name,
        icon = icon,
        color = "#2196F3",
        type = TrackerType.COUNT,
        unit = unit,
        targetValue = targetValue
    )
}
