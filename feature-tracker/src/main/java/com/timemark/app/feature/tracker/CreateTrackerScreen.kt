package com.timemark.app.feature.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.feature.tracker.components.StepIndicator
import com.timemark.app.feature.tracker.steps.StepAdvancedSettings
import com.timemark.app.feature.tracker.steps.StepBasicSettings
import com.timemark.app.feature.tracker.steps.StepPreview
import com.timemark.app.feature.tracker.steps.StepTypeSelection

/**
 * 创建打卡项页面
 *
 * 分步引导式创建（4 步）：
 * - Step 0: 类型选择/模板
 * - Step 1: 基础设置
 * - Step 2: 高级设置
 * - Step 3: 预览确认
 *
 * 顶部显示步骤指示器，底部显示上一步/下一步导航按钮。
 */
@Composable
fun CreateTrackerScreen(navController: NavController) {
    val viewModel: CreateTrackerViewModel = hiltViewModel()
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val draft by viewModel.trackerDraft.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "新建打卡 (${currentStep + 1}/4)",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 步骤指示器
            StepIndicator(currentStep = currentStep, totalSteps = 4)

            // 当前步骤内容（可滚动）
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (currentStep) {
                    0 -> StepTypeSelection(viewModel, draft)
                    1 -> StepBasicSettings(viewModel, draft)
                    2 -> StepAdvancedSettings(viewModel, draft)
                    3 -> StepPreview(draft)
                }
            }

            // 底部导航按钮
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    GlassButton(
                        text = "上一步",
                        onClick = { viewModel.previousStep() },
                        type = GlassButtonType.SECONDARY
                    )
                }
                if (currentStep < 3) {
                    GlassButton(
                        text = "下一步",
                        onClick = { viewModel.nextStep() }
                    )
                } else {
                    GlassButton(
                        text = "确认创建",
                        onClick = {
                            if (viewModel.save()) {
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }
        }
    }
}
