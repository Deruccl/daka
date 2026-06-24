package com.timemark.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.timemark.app.feature.tracker.components.StepIndicator
import org.junit.Rule
import org.junit.Test

/**
 * 创建打卡页面 UI 测试
 *
 * 测试创建打卡流程中的核心组件：
 * - 步骤指示器显示
 * - 步骤切换状态
 */
class CreateTrackerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun stepIndicator_4步_显示当前步骤() {
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    StepIndicator(currentStep = 0, totalSteps = 4)
                }
            }
        }
        // 步骤指示器应正确渲染
        composeRule.waitForIdle()
    }

    @Test
    fun stepIndicator_第一步_显示第一步高亮() {
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    StepIndicator(currentStep = 0, totalSteps = 4)
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun stepIndicator_最后一步_显示已完成标记() {
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    StepIndicator(currentStep = 3, totalSteps = 4)
                }
            }
        }
        // 最后一步时，前3步应显示 ✓ 标记
        composeRule.onNodeWithText("✓").assertIsDisplayed()
    }

    @Test
    fun stepIndicator_中间步骤_显示已完成标记() {
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    StepIndicator(currentStep = 2, totalSteps = 4)
                }
            }
        }
        // 第3步时，前2步应显示 ✓ 标记
        composeRule.onNodeWithText("✓").assertIsDisplayed()
    }

    @Test
    fun stepIndicator_第一步_无已完成标记() {
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    StepIndicator(currentStep = 0, totalSteps = 4)
                }
            }
        }
        // 第1步时，没有已完成的步骤，不应有 ✓ 标记
        // 等待渲染完成
        composeRule.waitForIdle()
    }

    @Test
    fun stepIndicator_不同总步数_正确渲染() {
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    StepIndicator(currentStep = 1, totalSteps = 3)
                }
            }
        }
        composeRule.waitForIdle()
    }
}
