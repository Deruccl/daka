package com.timemark.app.feature.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.EmptyState
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.Record
import com.timemark.app.feature.tracker.detail.DateSelector
import com.timemark.app.feature.tracker.detail.TodayStatusCard
import com.timemark.app.feature.tracker.detail.TrackerDetailViewModel
import com.timemark.app.feature.tracker.detail.ViewModeSelector
import com.timemark.app.feature.tracker.detail.RecordItem
import com.timemark.app.feature.tracker.record.AddRecordDialog
import com.timemark.app.feature.tracker.record.EditRecordDialog
import com.timemark.app.feature.tracker.record.rememberDeleteWithUndoState
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 打卡项详情页面
 *
 * 页面结构：
 * - 顶部：返回按钮、打卡名称、编辑入口
 * - 中部：今日状态卡片（数值、进度、连续天数、快速操作）
 * - 下部：日期选择器、视图模式选择器、历史记录时间轴
 *
 * 支持添加、编辑、删除（带 5 秒撤销）记录。
 *
 * @param navController 导航控制器
 * @param trackerId 打卡项 ID
 */
@Composable
fun TrackerDetailScreen(navController: NavController, trackerId: Long) {
    val viewModel: TrackerDetailViewModel = hiltViewModel()
    val tracker by viewModel.tracker.collectAsStateWithLifecycle()
    val todayStats by viewModel.todayStats.collectAsStateWithLifecycle()
    val todayRecords by viewModel.todayRecords.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()

    // 对话框状态
    var showAddDialog by remember { mutableStateOf(false) }
    var editRecord by remember { mutableStateOf<Record?>(null) }

    // Snackbar 与撤销状态
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteState = rememberDeleteWithUndoState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            GlassTopBar(
                title = tracker?.name ?: "详情",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate("edit_tracker/$trackerId") }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        val currentTracker = tracker
        if (currentTracker == null) {
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
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 今日状态卡片
                item {
                    TodayStatusCard(
                        tracker = currentTracker,
                        stats = todayStats,
                        streak = streak,
                        selectedDate = selectedDate,
                        onQuickAdd = { viewModel.quickAddRecord() },
                        onAddRecord = { showAddDialog = true }
                    )
                }

                // 日期选择器
                item {
                    DateSelector(
                        selectedDate = selectedDate,
                        onPrevious = { viewModel.previousDay() },
                        onNext = { viewModel.nextDay() },
                        onSelect = { viewModel.selectDate(it) }
                    )
                }

                // 视图模式选择器
                item {
                    ViewModeSelector(
                        viewMode = viewMode,
                        onModeChange = { viewModel.setViewMode(it) }
                    )
                }

                // 历史记录列表（过滤掉待删除的记录）
                val visibleRecords = todayRecords.filter { !deleteState.isPending(it) }
                if (visibleRecords.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Filled.Inbox,
                            title = "暂无记录",
                            description = "点击上方“添加记录”按钮，开始你的打卡之旅",
                            actionText = "添加记录",
                            onActionClick = { showAddDialog = true }
                        )
                    }
                } else {
                    items(visibleRecords, key = { it.id }) { record ->
                        val isLast = visibleRecords.lastOrNull()?.id == record.id
                        RecordItem(
                            record = record,
                            isLast = isLast,
                            onClick = { editRecord = record },
                            onEdit = { editRecord = record },
                            onDelete = {
                                deleteState.delete(
                                    record = record,
                                    snackbarHostState = snackbarHostState,
                                    scope = scope,
                                    onDelete = { id -> viewModel.deleteRecord(id) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // 添加记录对话框
    if (showAddDialog) {
        AddRecordDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { value, time, note ->
                val dateStr = selectedDate.format(DateTimeFormatter.ISO_DATE)
                val timeStr = if (time.isBlank()) {
                    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                } else time
                viewModel.addRecord(
                    value = value,
                    date = dateStr,
                    time = timeStr,
                    note = note,
                    images = emptyList(),
                    tags = emptyList()
                )
                showAddDialog = false
            },
            defaultTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        )
    }

    // 编辑记录对话框
    editRecord?.let { record ->
        EditRecordDialog(
            record = record,
            onDismiss = { editRecord = null },
            onConfirm = { updated ->
                viewModel.updateRecord(updated)
                editRecord = null
            }
        )
    }
}
