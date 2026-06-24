package com.timemark.app.feature.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassDialog
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.feature.tracker.steps.StepAdvancedSettings
import com.timemark.app.feature.tracker.steps.StepBasicSettings
import com.timemark.app.feature.tracker.steps.StepPreview

/**
 * 编辑打卡项页面
 *
 * 复用创建流程的步骤组件（基础设置 + 高级设置 + 预览），
 * 以单页可滚动表单呈现，预填原始数据。
 *
 * 顶部右侧提供删除按钮，删除前弹出确认对话框。
 *
 * @param navController 导航控制器
 * @param trackerId 待编辑的打卡项 ID
 */
@Composable
fun EditTrackerScreen(navController: NavController, trackerId: Long) {
    val viewModel: EditTrackerViewModel = hiltViewModel()
    val draft by viewModel.trackerDraft.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()

    // 删除确认对话框状态
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 删除完成后退出页面
    LaunchedEffect(deleted) {
        if (deleted) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "编辑打卡",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        val currentDraft = draft
        if (currentDraft == null) {
            // 加载中
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 复用基础设置步骤
                StepBasicSettings(
                    actions = viewModel,
                    draft = currentDraft
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 复用高级设置步骤
                StepAdvancedSettings(
                    actions = viewModel,
                    draft = currentDraft
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 预览
                StepPreview(draft = currentDraft)

                // 底部操作按钮
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "取消",
                        onClick = { navController.popBackStack() },
                        type = GlassButtonType.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "保存",
                        onClick = {
                            if (viewModel.save()) {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        GlassDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "确认删除",
            content = {
                Text(
                    text = "确定要删除这个打卡项吗？关联的所有打卡记录将一并删除，此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                GlassButton(
                    text = "删除",
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                    }
                )
            },
            dismissButton = {
                GlassButton(
                    text = "取消",
                    onClick = { showDeleteDialog = false },
                    type = GlassButtonType.SECONDARY
                )
            }
        )
    }
}
