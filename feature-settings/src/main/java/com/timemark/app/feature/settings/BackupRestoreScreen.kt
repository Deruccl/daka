package com.timemark.app.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.feature.settings.backup.BackupRestoreViewModel
import com.timemark.app.feature.settings.backup.BackupState
import com.timemark.app.feature.settings.components.SelectSettingItem
import com.timemark.app.feature.settings.components.SettingsSection
import com.timemark.app.feature.settings.components.SwitchSettingItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 备份与恢复页面
 *
 * 功能：
 * - 完整备份（数据库 + 配置）
 * - 仅数据备份
 * - 从备份恢复（文件选择器）
 * - 导出 CSV / JSON
 * - 自动备份设置（开关、频率）
 * - 备份/恢复进度显示
 */
@Composable
fun BackupRestoreScreen(navController: NavController) {
    val viewModel: BackupRestoreViewModel = hiltViewModel()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val autoBackupFrequency by viewModel.autoBackupFrequency.collectAsStateWithLifecycle()
    val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()

    // 文件选择器：用于选择备份文件恢复
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreFromBackup(it) }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "备份与恢复",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 状态提示
            item {
                BackupStatusCard(
                    state = backupState,
                    lastBackupTime = lastBackupTime,
                    onDismiss = viewModel::resetState
                )
            }

            // 备份操作
            item {
                SettingsSection("数据备份") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "完整备份",
                            onClick = viewModel::createFullBackup,
                            modifier = Modifier.weight(1f),
                            enabled = backupState !is BackupState.InProgress
                        )
                        GlassButton(
                            text = "仅数据",
                            onClick = viewModel::createDataOnlyBackup,
                            modifier = Modifier.weight(1f),
                            type = GlassButtonType.SECONDARY,
                            enabled = backupState !is BackupState.InProgress
                        )
                    }
                    Text(
                        text = "备份文件保存在应用外部存储目录下的 backup 文件夹中。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // 恢复操作
            item {
                SettingsSection("数据恢复") {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        GlassButton(
                            text = "从备份文件恢复",
                            onClick = {
                                restoreLauncher.launch(arrayOf("application/zip", "*/*"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            type = GlassButtonType.SECONDARY,
                            enabled = backupState !is BackupState.InProgress
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ 恢复操作会覆盖现有数据，请谨慎操作。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 数据导出
            item {
                SettingsSection("数据导出") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "导出 CSV",
                            onClick = { viewModel.exportCSV() },
                            modifier = Modifier.weight(1f),
                            enabled = backupState !is BackupState.InProgress
                        )
                        GlassButton(
                            text = "导出 JSON",
                            onClick = { viewModel.exportJSON() },
                            modifier = Modifier.weight(1f),
                            type = GlassButtonType.SECONDARY,
                            enabled = backupState !is BackupState.InProgress
                        )
                    }
                    // Task 32.5: PDF 导出按钮
                    GlassButton(
                        text = "导出 PDF 报告",
                        onClick = { viewModel.exportToPdf() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        enabled = backupState !is BackupState.InProgress
                    )
                    Text(
                        text = "导出所有打卡记录，文件保存在 export 文件夹中。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // 自动备份设置
            item {
                SettingsSection("自动备份") {
                    SwitchSettingItem(
                        title = "启用自动备份",
                        checked = autoBackupEnabled,
                        onCheckedChange = viewModel::setAutoBackupEnabled,
                        description = "定期自动备份应用数据"
                    )
                    if (autoBackupEnabled) {
                        SelectSettingItem(
                            title = "备份频率",
                            selectedLabel = "每 $autoBackupFrequency 天",
                            options = listOf(
                                "每天" to 1,
                                "每 3 天" to 3,
                                "每 7 天" to 7,
                                "每 14 天" to 14,
                                "每 30 天" to 30
                            ),
                            onSelect = viewModel::setAutoBackupFrequency
                        )
                    }
                }
            }
        }
    }
}

/**
 * 备份状态卡片
 *
 * 显示当前操作进度、成功/失败消息、最近备份时间。
 */
@Composable
private fun BackupStatusCard(
    state: BackupState,
    lastBackupTime: Long,
    onDismiss: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state) {
                is BackupState.Idle -> {
                    Text(
                        text = "最近备份",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (lastBackupTime > 0) {
                            formatTime(lastBackupTime)
                        } else {
                            "尚未备份"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                is BackupState.InProgress -> {
                    CircularProgressIndicator()
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                is BackupState.Success -> {
                    Text(
                        text = "✅",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    GlassButton(
                        text = "知道了",
                        onClick = onDismiss,
                        type = GlassButtonType.SMALL
                    )
                }
                is BackupState.Error -> {
                    Text(
                        text = "❌",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    GlassButton(
                        text = "关闭",
                        onClick = onDismiss,
                        type = GlassButtonType.SMALL
                    )
                }
            }
        }
    }
}

/** 格式化时间戳为可读字符串 */
private fun formatTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
