package com.timemark.app.feature.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassDialog
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTextField
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.feature.ai.chat.AIChatViewModel

/**
 * AI 对话页面（Task 33.2 增强）
 *
 * 增强：
 * - 启动时加载历史对话
 * - 新消息自动保存到数据库
 * - 顶栏右侧"清空历史"按钮
 * - 历史消息支持滑动删除
 */
@Composable
fun AIChatScreen(navController: NavController) {
    val viewModel: AIChatViewModel = hiltViewModel()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val historyEntries by viewModel.historyEntries.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showClearDialog by remember { mutableStateOf(false) }

    // 新消息时自动滚动到底部
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "AI 对话",
                onBackClick = { navController.popBackStack() },
                actions = {
                    // 清空历史按钮
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "清空历史"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 欢迎消息
                if (messages.isEmpty()) {
                    item {
                        GlassCard(
                            level = GlassLevel.LIGHT,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "👋 你好！我是 TimeMark AI 助手",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "我可以帮你：\n• 分析打卡数据\n• 提供习惯养成建议\n• 回答健康相关问题\n• 聊天解闷\n\n请直接输入你的问题。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(messages.size) { index ->
                    val msg = messages[index]
                    // 通过时间戳匹配历史记录 ID（倒序查找）
                    val entry = historyEntries.firstOrNull { e ->
                        e.role == msg.role && e.content == msg.content
                    }
                    SwipeToDismissMessage(
                        message = msg,
                        entryId = entry?.id,
                        onDelete = { id -> viewModel.deleteHistory(id) }
                    )
                }

                // 加载中
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            GlassCard(
                                level = GlassLevel.LIGHT,
                                modifier = Modifier.widthIn(max = 200.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text("思考中...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // 错误提示
                error?.let { err ->
                    item {
                        GlassCard(
                            level = GlassLevel.LIGHT,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ $err",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // 输入区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = "输入消息..."
                )
                IconButton(
                    onClick = {
                        if (input.isNotBlank() && !isLoading) {
                            viewModel.send(input)
                            input = ""
                        }
                    },
                    enabled = input.isNotBlank() && !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送",
                        tint = if (input.isNotBlank() && !isLoading)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    // 清空历史确认对话框
    if (showClearDialog) {
        GlassDialog(
            onDismissRequest = { showClearDialog = false },
            title = "清空对话历史",
            content = {
                Text(
                    text = "确定要清空全部对话历史吗？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                GlassButton(
                    text = "清空",
                    onClick = {
                        viewModel.clear()
                        showClearDialog = false
                    }
                )
            },
            dismissButton = {
                GlassButton(
                    text = "取消",
                    onClick = { showClearDialog = false },
                    type = GlassButtonType.SECONDARY
                )
            }
        )
    }
}

/**
 * 可滑动删除的消息项
 *
 * 当 entryId 不为 null 时，长按或点击右侧删除图标可删除该条消息。
 */
@Composable
private fun SwipeToDismissMessage(
    message: ChatMessage,
    entryId: Long?,
    onDelete: (Long) -> Unit
) {
    val isUser = message.role == "user"
    val configuration = LocalConfiguration.current
    val maxBubbleWidth = with(LocalDensity.current) { (configuration.screenWidthDp.dp - 96.dp).toPx() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 删除按钮（仅历史记录可删除时显示）
        if (entryId != null) {
            IconButton(onClick = { onDelete(entryId) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .widthIn(max = with(LocalDensity.current) { maxBubbleWidth.toDp() })
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
