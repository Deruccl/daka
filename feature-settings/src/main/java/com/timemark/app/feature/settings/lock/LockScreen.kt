package com.timemark.app.feature.settings.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.data.security.BiometricHelper
import kotlinx.coroutines.launch

/**
 * 应用锁解锁页面（Task 32.1）
 *
 * 功能：
 * - 密码输入解锁
 * - 指纹/面部识别解锁
 * - 忘记密码提示
 *
 * @param onUnlocked 解锁成功回调
 */
@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val lockMethod by viewModel.lockMethod.collectAsStateWithLifecycle()

    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var biometricAvailable by remember { mutableStateOf(false) }

    // 检查生物识别可用性
    LaunchedEffect(biometricEnabled) {
        biometricAvailable = biometricEnabled && BiometricHelper.isBiometricAvailable(context)
    }

    // 如果启用生物识别且可用，自动弹出认证
    LaunchedEffect(biometricAvailable) {
        if (biometricAvailable) {
            val activity = context as? FragmentActivity
            if (activity != null) {
                BiometricHelper.authenticate(
                    activity = activity,
                    onSuccess = { onUnlocked() },
                    onError = { _, _ -> },
                    onFailed = {}
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 锁图标
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(48.dp)
                )

                Text(
                    text = "应用已锁定",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 密码输入框
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
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                // 解锁按钮
                GlassButton(
                    text = "解锁",
                    onClick = {
                        scope.launch {
                            if (password.isBlank()) {
                                errorMessage = "请输入密码"
                            } else if (viewModel.verifyPassword(password)) {
                                onUnlocked()
                            } else {
                                errorMessage = "密码错误，请重试"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 生物识别按钮
                if (biometricAvailable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassButton(
                        text = "使用生物识别解锁",
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                BiometricHelper.authenticate(
                                    activity = activity,
                                    onSuccess = { onUnlocked() },
                                    onError = { _, _ -> },
                                    onFailed = {}
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        type = GlassButtonType.SECONDARY,
                        icon = { Icon(Icons.Default.Fingerprint, contentDescription = null) }
                    )
                }

                // 忘记密码提示
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "忘记密码？请在系统设置中清除应用数据重置。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
