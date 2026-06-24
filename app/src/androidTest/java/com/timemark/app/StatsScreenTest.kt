package com.timemark.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.timemark.app.domain.model.TimeViewLevel
import com.timemark.app.feature.stats.ViewLevelSelector
import org.junit.Rule
import org.junit.Test

/**
 * 统计页面 UI 测试
 *
 * 测试统计页核心组件：
 * - 时间视图级别选择器显示
 * - 视图切换交互
 */
class StatsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun viewLevelSelector_显示所有级别选项() {
        var selectedLevel = TimeViewLevel.DAY
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    ViewLevelSelector(
                        current = selectedLevel,
                        onSelect = { selectedLevel = it }
                    )
                }
            }
        }
        // 验证所有级别文字都显示
        composeRule.onNodeWithText("分").assertIsDisplayed()
        composeRule.onNodeWithText("时").assertIsDisplayed()
        composeRule.onNodeWithText("日").assertIsDisplayed()
        composeRule.onNodeWithText("周").assertIsDisplayed()
        composeRule.onNodeWithText("月").assertIsDisplayed()
        composeRule.onNodeWithText("年").assertIsDisplayed()
    }

    @Test
    fun viewLevelSelector_点击周视图_切换到周() {
        var selectedLevel = TimeViewLevel.DAY
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    ViewLevelSelector(
                        current = selectedLevel,
                        onSelect = { selectedLevel = it }
                    )
                }
            }
        }
        composeRule.onNodeWithText("周").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.WEEK)
    }

    @Test
    fun viewLevelSelector_点击月视图_切换到月() {
        var selectedLevel = TimeViewLevel.DAY
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    ViewLevelSelector(
                        current = selectedLevel,
                        onSelect = { selectedLevel = it }
                    )
                }
            }
        }
        composeRule.onNodeWithText("月").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.MONTH)
    }

    @Test
    fun viewLevelSelector_点击年视图_切换到年() {
        var selectedLevel = TimeViewLevel.DAY
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    ViewLevelSelector(
                        current = selectedLevel,
                        onSelect = { selectedLevel = it }
                    )
                }
            }
        }
        composeRule.onNodeWithText("年").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.YEAR)
    }

    @Test
    fun viewLevelSelector_点击分视图_切换到分() {
        var selectedLevel = TimeViewLevel.HOUR
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    ViewLevelSelector(
                        current = selectedLevel,
                        onSelect = { selectedLevel = it }
                    )
                }
            }
        }
        composeRule.onNodeWithText("分").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.MINUTE)
    }

    @Test
    fun viewLevelSelector_点击时视图_切换到时() {
        var selectedLevel = TimeViewLevel.DAY
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    ViewLevelSelector(
                        current = selectedLevel,
                        onSelect = { selectedLevel = it }
                    )
                }
            }
        }
        composeRule.onNodeWithText("时").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.HOUR)
    }

    @Test
    fun viewLevelSelector_点击日视图_切换到日() {
        var selectedLevel = TimeViewLevel.WEEK
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    ViewLevelSelector(
                        current = selectedLevel,
                        onSelect = { selectedLevel = it }
                    )
                }
            }
        }
        composeRule.onNodeWithText("日").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.DAY)
    }

    @Test
    fun viewLevelSelector_初始为分钟_正确显示() {
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    ViewLevelSelector(
                        current = TimeViewLevel.MINUTE,
                        onSelect = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("分").assertIsDisplayed()
    }

    @Test
    fun viewLevelSelector_连续切换_状态正确() {
        var selectedLevel = TimeViewLevel.MINUTE
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    ViewLevelSelector(
                        current = selectedLevel,
                        onSelect = { selectedLevel = it }
                    )
                }
            }
        }
        // 分 -> 时 -> 日 -> 周 -> 月 -> 年
        composeRule.onNodeWithText("时").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.HOUR)

        composeRule.onNodeWithText("日").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.DAY)

        composeRule.onNodeWithText("周").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.WEEK)

        composeRule.onNodeWithText("月").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.MONTH)

        composeRule.onNodeWithText("年").performClick()
        composeRule.waitForIdle()
        assert(selectedLevel == TimeViewLevel.YEAR)
    }
}
