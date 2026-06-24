package com.timemark.app.feature.settings

import android.content.Intent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassDialog
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.feature.settings.components.SettingsSection
import com.timemark.app.feature.settings.components.SwitchSettingItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Task 38.4: 崩溃日志页面
 *
 * 功能：
 * - 崩溃收集开关
 * - 崩溃记录列表（时间、简要信息）
 * - 点击查看详情（完整崩溃报告）
 * - 导出崩溃日志（通过系统分享面板）
 * - 清除崩溃记录（带确认对话框）
 *
 * 崩溃文件存储在 app/files/crashes/crash_YYYYMMDD_HHmmss.txt
 */
@Composable
fun CrashLogScreen(navController: NavController) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val context = LocalContext.current

    // 崩溃收集开关
    val crashReportEnabled by viewModel.crashReportEnabled.collectAsState()

    // 崩溃文件目录
    val crashDir = remember { File(context.filesDir, "crashes").also { if (!it.exists()) it.mkdirs() } }

    // 崩溃文件列表（可刷新）
    var crashFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // 选中的崩溃文件（查看详情）
    var selectedCrashContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    // 清除确认对话框
    var showClearDialog by remember { mutableStateOf(false) }

    // 导出结果提示
    var exportMessage by remember { mutableStateOf<String?>(null) }

    // 加载崩溃文件列表
    LaunchedEffect(refreshTrigger, crashReportEnabled) {
        crashFiles = crashDir.listFiles { file -> file.isFile && file.name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "崩溃日志",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 崩溃收集开关
            SettingsSection("崩溃收集") {
                SwitchSettingItem(
                    title = "启用崩溃收集",
                    checked = crashReportEnabled,
                    onCheckedChange = viewModel::setCrashReportEnabled,
                    description = "捕获未处理异常并保存到本地文件（最多保留 10 条）"
                )
            }

            // 操作栏
            if (crashFiles.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "共 ${crashFiles.size} 条崩溃记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    GlassButton(
                        text = "清除全部",
                        onClick = { showClearDialog = true },
                        type = GlassButtonType.SMALL
                    )
                }
            }

            // 崩溃记录列表
            if (crashFiles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "暂无崩溃记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "应用崩溃时将自动记录在此处",
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
                    items(crashFiles) { file ->
                        CrashFileItem(
                            file = file,
                            onClick = {
                                // 读取文件内容并显示详情
                                val content = file.readText()
                                selectedCrashContent = file.name to content
                            },
                            onExport = {
                                exportCrashFile(context, file)
                            }
                        )
                    }
                }
            }
        }
    }

    // 崩溃详情对话框
    selectedCrashContent?.let { (name, content) ->
        GlassDialog(
            onDismissRequest = { selectedCrashContent = null },
            title = "崩溃详情 - $name",
            content = {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 20,
                    overflow = TextOverflow.Ellipsis
                )
            },
            confirmButton = {
                GlassButton(
                    text = "关闭",
                    onClick = { selectedCrashContent = null },
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
            title = "清除崩溃记录",
            content = {
                Text(
                    text = "确定清除所有崩溃记录？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                GlassButton(
                    text = "确定清除",
                    onClick = {
                        crashFiles.forEach { it.delete() }
                        refreshTrigger++
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

    // 导出结果提示
    exportMessage?.let { message ->
        GlassDialog(
            onDismissRequest = { exportMessage = null },
            title = "提示",
            content = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                GlassButton(
                    text = "确定",
                    onClick = { exportMessage = null },
                    type = GlassButtonType.SECONDARY
                )
            },
            dismissButton = {}
        )
    }
}

/**
 * 崩溃文件列表项
 *
 * @param file 崩溃文件
 * @param onClick 点击查看详情
 * @param onExport 导出回调
 */
@Composable
private fun CrashFileItem(
    file: File,
    onClick: () -> Unit,
    onExport: () -> Unit
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
                    text = formatCrashFileName(file.name),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = formatFileSize(file.length()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // 提取简要信息（异常类型与消息）
            Text(
                text = extractCrashSummary(file),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                GlassButton(
                    text = "导出",
                    onClick = onExport,
                    type = GlassButtonType.SMALL
                )
            }
        }
    }
}

/**
 * 导出崩溃文件（通过系统分享面板）
 */
private fun exportCrashFile(context: android.content.Context, file: File) {
    runCatching {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享崩溃日志"))
    }.onFailure {
        android.widget.Toast.makeText(
            context,
            "导出失败：${file.absolutePath}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

/**
 * 格式化崩溃文件名为可读时间
 * crash_20240101_120000.txt -> 2024-01-01 12:00:00
 */
private fun formatCrashFileName(fileName: String): String {
    return runCatching {
        // 提取 crash_YYYYMMDD_HHmmss 部分
        val core = fileName.removePrefix("crash_").removeSuffix(".txt")
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val date = dateFormat.parse(core)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        outputFormat.format(date ?: Date())
    }.getOrDefault(fileName)
}

/**
 * 从崩溃文件中提取简要信息（异常类型与消息）
 */
private fun extractCrashSummary(file: File): String {
    return runCatching {
        val content = file.readText()
        // 查找"异常类型:"和"异常消息:"行
        val typeLine = content.lines().find { it.startsWith("异常类型:") }
        val msgLine = content.lines().find { it.startsWith("异常消息:") }
        buildString {
            if (typeLine != null) append(typeLine)
            if (msgLine != null) {
                if (isNotEmpty()) append(" | ")
                append(msgLine)
            }
            if (isEmpty()) append("（无法解析异常信息）")
        }
    }.getOrDefault("（无法读取崩溃信息）")
}

/** 格式化文件大小 */
private fun formatFileSize(size: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    return when {
        size >= mb -> String.format(Locale.getDefault(), "%.2f MB", size / mb)
        size >= kb -> String.format(Locale.getDefault(), "%.2f KB", size / kb)
        else -> "$size B"
    }
}
