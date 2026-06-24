package com.timemark.app.feature.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassDialog
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.core.utils.Logger
import com.timemark.app.feature.settings.components.NavigateSettingItem
import com.timemark.app.feature.settings.components.SelectSettingItem
import com.timemark.app.feature.settings.components.SettingsSection
import com.timemark.app.feature.settings.components.SwitchSettingItem
import java.io.File

/**
 * Task 38.3: 日志管理页面
 *
 * 功能：
 * - 日志开关（开启/关闭文件日志写入）
 * - 日志级别选择（DEBUG/INFO/WARN/ERROR）
 * - 日志文件大小显示
 * - 导出日志按钮（通过系统分享面板分享/保存到文件）
 * - 清除日志按钮（带确认对话框）
 *
 * 日志文件存储在 app/files/logs/timemark.log，超过 5MB 自动轮转。
 */
@Composable
fun LogSettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val context = LocalContext.current

    // 收集日志设置状态
    val loggingEnabled by viewModel.loggingEnabled.collectAsState()
    val logLevel by viewModel.logLevel.collectAsState()

    // 日志文件大小（可刷新）
    var logFileSize by remember { mutableStateOf(Logger.getLogFileSizeFormatted()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // 每次进入页面或操作后刷新文件大小
    LaunchedEffect(refreshTrigger) {
        logFileSize = Logger.getLogFileSizeFormatted()
    }

    // 清除日志确认对话框
    var showClearDialog by remember { mutableStateOf(false) }

    // 导出结果提示
    var exportMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "日志管理",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 日志设置
            item {
                SettingsSection("日志设置") {
                    SwitchSettingItem(
                        title = "启用日志",
                        checked = loggingEnabled,
                        onCheckedChange = viewModel::setLoggingEnabled,
                        description = "开启后将日志写入文件（app/logs/timemark.log）"
                    )
                    if (loggingEnabled) {
                        SelectSettingItem(
                            title = "日志级别",
                            selectedLabel = logLevelLabel(logLevel),
                            options = listOf(
                                "DEBUG（调试）" to "DEBUG",
                                "INFO（信息）" to "INFO",
                                "WARN（警告）" to "WARN",
                                "ERROR（错误）" to "ERROR"
                            ),
                            onSelect = viewModel::setLogLevel,
                            description = "仅不低于此级别的日志写入文件"
                        )
                    }
                }
            }

            // 日志文件信息
            if (loggingEnabled) {
                item {
                    SettingsSection("日志文件") {
                        NavigateSettingItem(
                            title = "日志文件大小",
                            onClick = { refreshTrigger++ },
                            description = "$logFileSize（点击刷新）"
                        )
                        NavigateSettingItem(
                            title = "日志文件路径",
                            onClick = {},
                            description = "app/files/logs/timemark.log"
                        )
                    }
                }

                // 操作按钮
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassButton(
                            text = "导出日志",
                            onClick = {
                                val exportedFile = Logger.exportLogs()
                                if (exportedFile != null) {
                                    shareLogFile(context, exportedFile)
                                } else {
                                    exportMessage = "日志文件为空，无需导出"
                                }
                            },
                            type = GlassButtonType.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                        GlassButton(
                            text = "清除日志",
                            onClick = { showClearDialog = true },
                            type = GlassButtonType.SECONDARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 说明
            item {
                SettingsSection("说明") {
                    Text(
                        text = "• 日志文件大小上限 5MB，超过后自动轮转\n" +
                            "• 轮转时保留最近 1 个备份（timemark.log.bak）\n" +
                            "• 导出日志将合并主日志与备份日志\n" +
                            "• 关闭日志后不再写入文件，但 Logcat 输出不受影响",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    // 清除日志确认对话框
    if (showClearDialog) {
        GlassDialog(
            onDismissRequest = { showClearDialog = false },
            title = "清除日志",
            content = {
                Text(
                    text = "确定清除所有日志文件？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                GlassButton(
                    text = "确定清除",
                    onClick = {
                        Logger.clearLogs()
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
 * 通过系统分享面板分享日志文件
 *
 * 使用 FileProvider 生成 content:// URI，避免 FileUriExposedException。
 */
private fun shareLogFile(context: android.content.Context, file: File) {
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
        context.startActivity(Intent.createChooser(shareIntent, "分享日志文件"))
    }.onFailure { e ->
        // FileProvider 未配置时，回退到直接打开文件
        android.widget.Toast.makeText(
            context,
            "导出成功：${file.absolutePath}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

/** 日志级别显示文字 */
private fun logLevelLabel(level: String): String = when (level) {
    "DEBUG" -> "DEBUG（调试）"
    "INFO" -> "INFO（信息）"
    "WARN" -> "WARN（警告）"
    "ERROR" -> "ERROR（错误）"
    else -> level
}
