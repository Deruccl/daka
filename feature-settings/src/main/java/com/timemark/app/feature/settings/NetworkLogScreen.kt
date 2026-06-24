package com.timemark.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.timemark.app.ai.NetworkLogEntry
import com.timemark.app.ai.NetworkLogger
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassDialog
import com.timemark.app.core.ui.components.glass.GlassTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 网络请求日志页面（Task 32.6）
 *
 * 功能：
 * - 显示网络请求日志列表（URL、方法、响应码、响应时间、Token 数）
 * - 点击查看详情
 * - 清除日志按钮
 */
@Composable
fun NetworkLogScreen(navController: NavController) {
    val logs by NetworkLogger.logs.collectAsState()
    var selectedLog by remember { mutableStateOf<NetworkLogEntry?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "网络请求日志",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 操作栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "共 ${logs.size} 条记录（最近 100 条）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                GlassButton(
                    text = "清除日志",
                    onClick = { showClearDialog = true },
                    type = GlassButtonType.SMALL,
                    enabled = logs.isNotEmpty()
                )
            }

            // 日志列表
            if (logs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "暂无网络请求日志",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "启用网络请求日志后，AI 请求将记录在此处",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { entry ->
                        NetworkLogItem(entry = entry, onClick = { selectedLog = entry })
                    }
                }
            }
        }
    }

    // 详情对话框
    selectedLog?.let { entry ->
        GlassDialog(
            onDismissRequest = { selectedLog = null },
            title = "请求详情",
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LogDetailRow("时间", formatTime(entry.timestamp))
                    LogDetailRow("方法", entry.method)
                    LogDetailRow("URL", entry.url)
                    LogDetailRow("请求大小", "${entry.requestSize} 字节")
                    LogDetailRow("响应码", entry.responseCode.toString())
                    LogDetailRow("响应时间", "${entry.responseTimeMs} ms")
                    LogDetailRow("Token 数", entry.tokenCount.toString())
                    entry.errorMessage?.let {
                        LogDetailRow("错误", it)
                    }
                }
            },
            confirmButton = {
                GlassButton(
                    text = "关闭",
                    onClick = { selectedLog = null },
                    type = GlassButtonType.SECONDARY
                )
            },
            dismissButton = {}
        )
    }

    // 清除确认对话框
    if (showClearDialog) {
        GlassDialog(
            onDismissRequest = { showClearDialog = false },
            title = "清除日志",
            content = {
                Text(
                    text = "确定清除所有网络请求日志？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                GlassButton(
                    text = "确定清除",
                    onClick = {
                        NetworkLogger.clearLogs()
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
 * 单条日志项
 */
@Composable
private fun NetworkLogItem(
    entry: NetworkLogEntry,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entry.method} ${entry.responseCode}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (entry.responseCode in 200..299) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                Text(
                    text = formatTime(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = entry.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${entry.responseTimeMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.tokenCount > 0) {
                    Text(
                        text = "tokens: ${entry.tokenCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (entry.requestSize > 0) {
                    Text(
                        text = "req: ${entry.requestSize}B",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 日志详情行
 */
@Composable
private fun LogDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** 格式化时间戳 */
private fun formatTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
