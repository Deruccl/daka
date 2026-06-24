package com.timemark.app.feature.settings.lock

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassDialog
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.feature.settings.components.SelectSettingItem
import com.timemark.app.feature.settings.components.SettingsSection
import com.timemark.app.feature.settings.components.SwitchSettingItem

/**
 * 应用锁设置页面（Task 32.1）
 *
 * 功能：
 * - 设置/修改/关闭密码
 * - 指纹/面部识别开关
 * - 自动锁定时间选择
 */
@Composable
fun AppLockScreen(navController: NavController) {
    val viewModel: AppLockViewModel = hiltViewModel()
    val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle()
    val lockMethod by viewModel.lockMethod.collectAsStateWithLifecycle()
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val autoLockMinutes by viewModel.autoLockMinutes.collectAsStateWithLifecycle()
    val hasPassword by viewModel.hasPassword.collectAsStateWithLifecycle()

    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showDisableLockDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "应用锁",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 应用锁开关
            item {
                SettingsSection("应用锁") {
                    SwitchSettingItem(
                        title = "启用应用锁",
                        checked = appLockEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                showSetPasswordDialog = true
                            } else {
                                showDisableLockDialog = true
                            }
                        },
                        description = "启动应用时需要验证身份"
                    )
                }
            }

            // 密码设置
            if (appLockEnabled) {
                item {
                    SettingsSection("密码") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassButton(
                                text = if (hasPassword) "修改密码" else "设置密码",
                                onClick = { showSetPasswordDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            GlassButton(
                                text = "关闭应用锁",
                                onClick = { showDisableLockDialog = true },
                                modifier = Modifier.weight(1f),
                                type = GlassButtonType.SECONDARY
                            )
                        }
                    }
                }

                // 生物识别
                item {
                    SettingsSection("生物识别") {
                        SwitchSettingItem(
                            title = "指纹/面部识别",
                            checked = biometricEnabled,
                            onCheckedChange = viewModel::setBiometricEnabled,
                            description = "使用生物识别快速解锁（需设备支持）"
                        )
                    }
                }

                // 自动锁定时间
                item {
                    SettingsSection("自动锁定") {
                        SelectSettingItem(
                            title = "自动锁定时间",
                            selectedLabel = autoLockLabel(autoLockMinutes),
                            options = listOf(
                                "1 分钟" to 1,
                                "5 分钟" to 5,
                                "15 分钟" to 15,
                                "30 分钟" to 30
                            ),
                            onSelect = viewModel::setAutoLockMinutes,
                            description = "应用退至后台后多久自动锁定"
                        )
                    }
                }
            }
        }
    }

    // 设置/修改密码对话框
    if (showSetPasswordDialog) {
        PasswordSetDialog(
            onDismiss = { showSetPasswordDialog = false },
            onConfirm = { password ->
                viewModel.enablePasswordLock(password)
                showSetPasswordDialog = false
            }
        )
    }

    // 关闭应用锁确认对话框
    if (showDisableLockDialog) {
        GlassDialog(
            onDismissRequest = { showDisableLockDialog = false },
            title = "关闭应用锁",
            content = {
                Text(
                    text = "关闭后将清除密码与生物识别设置，应用启动时不再需要验证身份。确定关闭？",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                GlassButton(
                    text = "确定关闭",
                    onClick = {
                        viewModel.disableAppLock()
                        showDisableLockDialog = false
                    }
                )
            },
            dismissButton = {
                GlassButton(
                    text = "取消",
                    onClick = { showDisableLockDialog = false },
                    type = GlassButtonType.SECONDARY
                )
            }
        )
    }
}

/**
 * 设置密码对话框
 * 支持输入密码并确认
 */
@Composable
private fun PasswordSetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    GlassDialog(
        onDismissRequest = onDismiss,
        title = "设置密码",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("请输入密码") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("请确认密码") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            GlassButton(
                text = "确定",
                onClick = {
                    when {
                        password.isBlank() -> errorMessage = "密码不能为空"
                        password.length < 4 -> errorMessage = "密码至少 4 位"
                        password != confirmPassword -> errorMessage = "两次输入的密码不一致"
                        else -> onConfirm(password)
                    }
                }
            )
        },
        dismissButton = {
            GlassButton(
                text = "取消",
                onClick = onDismiss,
                type = GlassButtonType.SECONDARY
            )
        }
    )
}

/** 自动锁定时间显示文字 */
private fun autoLockLabel(minutes: Int): String = when (minutes) {
    1 -> "1 分钟"
    5 -> "5 分钟"
    15 -> "15 分钟"
    30 -> "30 分钟"
    else -> "$minutes 分钟"
}
