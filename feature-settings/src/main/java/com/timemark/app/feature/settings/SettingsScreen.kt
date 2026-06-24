package com.timemark.app.feature.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassDialog
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.FirstDayOfWeek
import com.timemark.app.domain.model.ThemeMode
import com.timemark.app.feature.settings.components.NavigateSettingItem
import com.timemark.app.feature.settings.components.SelectSettingItem
import com.timemark.app.feature.settings.components.SettingsSection
import com.timemark.app.feature.settings.components.SwitchSettingItem

/**
 * 设置页面
 *
 * 分组展示各类设置项：
 * - 外观：主题模式、模糊效果、动画效果
 * - 通用：音效、触觉反馈、24 小时制、每周第一天、语言
 * - 隐私与安全：应用锁、自动锁定时间
 * - AI 设置：全局开关、仅 WiFi、Token 限制、预算限制、协同模式
 * - 数据管理：备份与恢复、导出数据
 * - 关于：关于应用
 */
@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = hiltViewModel()

    // 收集各设置项状态
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val blurEnabled by viewModel.blurEnabled.collectAsStateWithLifecycle()
    val animationEnabled by viewModel.animationEnabled.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
    val use24HourFormat by viewModel.use24HourFormat.collectAsStateWithLifecycle()
    val firstDayOfWeek by viewModel.firstDayOfWeek.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val aiGlobalEnabled by viewModel.aiGlobalEnabled.collectAsStateWithLifecycle()
    val aiWifiOnly by viewModel.aiWifiOnly.collectAsStateWithLifecycle()
    val dailyTokenLimit by viewModel.dailyTokenLimit.collectAsStateWithLifecycle()
    val monthlyTokenLimit by viewModel.monthlyTokenLimit.collectAsStateWithLifecycle()
    val monthlyBudgetLimit by viewModel.monthlyBudgetLimit.collectAsStateWithLifecycle()
    val collaborativeMode by viewModel.collaborativeMode.collectAsStateWithLifecycle()
    val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle()
    val secureScreen by viewModel.secureScreen.collectAsStateWithLifecycle()
    val databaseEncryptionEnabled by viewModel.databaseEncryptionEnabled.collectAsStateWithLifecycle()
    val autoBackupEnabledV2 by viewModel.autoBackupEnabledV2.collectAsStateWithLifecycle()
    val autoBackupFrequencyV2 by viewModel.autoBackupFrequencyV2.collectAsStateWithLifecycle()
    val autoBackupKeepCount by viewModel.autoBackupKeepCount.collectAsStateWithLifecycle()
    val networkLogEnabled by viewModel.networkLogEnabled.collectAsStateWithLifecycle()
    val disableNetworkAccess by viewModel.disableNetworkAccess.collectAsStateWithLifecycle()
    // Task 37.2: 无障碍设置
    val highContrastMode by viewModel.highContrastMode.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
    // Task 38.3/38.4: 日志与崩溃收集设置
    val loggingEnabled by viewModel.loggingEnabled.collectAsStateWithLifecycle()
    val crashReportEnabled by viewModel.crashReportEnabled.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { GlassTopBar(title = "设置") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 外观设置
            item {
                SettingsSection("外观") {
                    SelectSettingItem(
                        title = "主题模式",
                        selectedLabel = themeModeLabel(themeMode),
                        options = listOf(
                            "跟随系统" to ThemeMode.SYSTEM,
                            "浅色" to ThemeMode.LIGHT,
                            "深色" to ThemeMode.DARK
                        ),
                        onSelect = viewModel::setThemeMode
                    )
                    SwitchSettingItem(
                        title = "模糊效果",
                        checked = blurEnabled,
                        onCheckedChange = viewModel::setBlurEnabled,
                        description = "液态玻璃质感背景模糊"
                    )
                    SwitchSettingItem(
                        title = "动画效果",
                        checked = animationEnabled,
                        onCheckedChange = viewModel::setAnimationEnabled,
                        description = "页面切换与交互动画"
                    )
                    // Task 37.2: 高对比度模式
                    SwitchSettingItem(
                        title = "高对比度模式",
                        checked = highContrastMode,
                        onCheckedChange = viewModel::setHighContrastMode,
                        description = "增强文字与背景对比度，提升可读性"
                    )
                    // Task 37.2: 字体缩放
                    SelectSettingItem(
                        title = "字体大小",
                        selectedLabel = fontScaleLabel(fontScale),
                        options = listOf(
                            "标准" to 1.0f,
                            "大号" to 1.15f,
                            "超大号" to 1.3f
                        ),
                        onSelect = viewModel::setFontScale
                    )
                }
            }

            // 通用设置
            item {
                SettingsSection("通用") {
                    SwitchSettingItem(
                        title = "音效",
                        checked = soundEnabled,
                        onCheckedChange = viewModel::setSoundEnabled
                    )
                    SwitchSettingItem(
                        title = "触觉反馈",
                        checked = hapticEnabled,
                        onCheckedChange = viewModel::setHapticEnabled
                    )
                    SwitchSettingItem(
                        title = "24 小时制",
                        checked = use24HourFormat,
                        onCheckedChange = viewModel::setUse24HourFormat
                    )
                    SelectSettingItem(
                        title = "每周第一天",
                        selectedLabel = firstDayOfWeekLabel(firstDayOfWeek),
                        options = listOf(
                            "周一" to FirstDayOfWeek.MONDAY,
                            "周日" to FirstDayOfWeek.SUNDAY
                        ),
                        onSelect = viewModel::setFirstDayOfWeek
                    )
                    SelectSettingItem(
                        title = "语言",
                        selectedLabel = languageLabel(language),
                        options = listOf(
                            "简体中文" to "zh-CN",
                            "English" to "en-US",
                            "日本語" to "ja-JP"
                        ),
                        onSelect = viewModel::setLanguage
                    )
                }
            }

            // 隐私与安全
            item {
                SettingsSection("隐私与安全") {
                    NavigateSettingItem(
                        title = "应用锁",
                        onClick = { navController.navigate("app_lock") },
                        description = if (appLockEnabled) "已启用" else "设置启动验证"
                    )
                    SwitchSettingItem(
                        title = "禁止截图",
                        checked = secureScreen,
                        onCheckedChange = viewModel::setSecureScreen,
                        description = "阻止截图与最近任务列表内容预览"
                    )
                    SwitchSettingItem(
                        title = "数据库加密",
                        checked = databaseEncryptionEnabled,
                        onCheckedChange = viewModel::setDatabaseEncryptionEnabled,
                        description = "使用 SQLCipher 加密本地数据库"
                    )
                    SwitchSettingItem(
                        title = "禁用网络访问",
                        checked = disableNetworkAccess,
                        onCheckedChange = viewModel::setDisableNetworkAccess,
                        description = "完全禁止应用联网（AI 功能将不可用）"
                    )
                    SwitchSettingItem(
                        title = "网络请求日志",
                        checked = networkLogEnabled,
                        onCheckedChange = viewModel::setNetworkLogEnabled,
                        description = "记录 AI 网络请求用于审计"
                    )
                    if (networkLogEnabled) {
                        NavigateSettingItem(
                            title = "查看网络日志",
                            onClick = { navController.navigate("network_log") }
                        )
                    }
                }
            }

            // AI 设置
            item {
                SettingsSection("AI 设置") {
                    SwitchSettingItem(
                        title = "启用 AI",
                        checked = aiGlobalEnabled,
                        onCheckedChange = viewModel::setAIGlobalEnabled,
                        description = "全局 AI 功能开关"
                    )
                    if (aiGlobalEnabled) {
                        // Task 33.3: AI 功能独立设置入口
                        NavigateSettingItem(
                            title = "AI 功能设置",
                            onClick = { navController.navigate("ai_feature_settings") },
                            description = "各功能独立开关、仅 WiFi 模式、Token 优化"
                        )
                        SwitchSettingItem(
                            title = "仅 WiFi 下使用",
                            checked = aiWifiOnly,
                            onCheckedChange = viewModel::setAIWifiOnly,
                            description = "避免在移动网络下消耗流量"
                        )
                        NumberInputDialog(
                            title = "每日 Token 限制",
                            value = dailyTokenLimit,
                            label = "$dailyTokenLimit",
                            onConfirm = viewModel::setDailyTokenLimit
                        )
                        NumberInputDialog(
                            title = "每月 Token 限制",
                            value = monthlyTokenLimit,
                            label = "$monthlyTokenLimit",
                            onConfirm = viewModel::setMonthlyTokenLimit
                        )
                        DecimalInputDialog(
                            title = "每月预算限制",
                            value = monthlyBudgetLimit,
                            label = "¥$monthlyBudgetLimit",
                            onConfirm = viewModel::setMonthlyBudgetLimit
                        )
                        SwitchSettingItem(
                            title = "协同模式",
                            checked = collaborativeMode,
                            onCheckedChange = viewModel::setCollaborativeMode,
                            description = "多 AI 提供商协同工作"
                        )
                    }
                }
            }

            // 数据管理
            item {
                SettingsSection("数据管理") {
                    NavigateSettingItem(
                        title = "备份与恢复",
                        onClick = { navController.navigate("backup_restore") },
                        description = "手动备份、恢复、自动备份"
                    )
                    NavigateSettingItem(
                        title = "导出数据",
                        onClick = { navController.navigate("backup_restore") },
                        description = "导出为 CSV / JSON / PDF"
                    )
                    // Task 38.3: 日志管理入口
                    NavigateSettingItem(
                        title = "日志管理",
                        onClick = { navController.navigate("log_settings") },
                        description = if (loggingEnabled) "日志已启用" else "日志已关闭"
                    )
                    // Task 38.4: 崩溃日志入口
                    NavigateSettingItem(
                        title = "崩溃日志",
                        onClick = { navController.navigate("crash_log") },
                        description = if (crashReportEnabled) "崩溃收集已启用" else "崩溃收集已关闭"
                    )
                }
            }

            // 自动备份（Task 32.4）
            item {
                SettingsSection("自动备份") {
                    SwitchSettingItem(
                        title = "启用自动备份",
                        checked = autoBackupEnabledV2,
                        onCheckedChange = viewModel::setAutoBackupEnabledV2,
                        description = "定期自动备份应用数据"
                    )
                    if (autoBackupEnabledV2) {
                        SelectSettingItem(
                            title = "备份频率",
                            selectedLabel = autoBackupFrequencyLabel(autoBackupFrequencyV2),
                            options = listOf(
                                "每天" to "daily",
                                "每周" to "weekly",
                                "每月" to "monthly"
                            ),
                            onSelect = viewModel::setAutoBackupFrequencyV2
                        )
                        NumberInputDialog(
                            title = "保留版本数",
                            value = autoBackupKeepCount,
                            label = "$autoBackupKeepCount 个",
                            onConfirm = viewModel::setAutoBackupKeepCount
                        )
                    }
                }
            }

            // 关于
            item {
                SettingsSection("关于") {
                    NavigateSettingItem(
                        title = "关于应用",
                        onClick = { navController.navigate("about") }
                    )
                }
            }
        }
    }
}

/**
 * 整数输入设置项：点击弹出对话框输入数值
 */
@Composable
private fun NumberInputDialog(
    title: String,
    value: Int,
    label: String,
    onConfirm: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf(value.toString()) }

    NavigateSettingItem(
        title = title,
        onClick = {
            inputValue = value.toString()
            showDialog = true
        },
        description = label
    )

    if (showDialog) {
        GlassDialog(
            onDismissRequest = { showDialog = false },
            title = title,
            content = {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it.filter { c -> c.isDigit() } },
                    label = { Text("请输入数值") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                GlassButton(
                    text = "确定",
                    onClick = {
                        inputValue.toIntOrNull()?.let(onConfirm)
                        showDialog = false
                    }
                )
            },
            dismissButton = {
                GlassButton(
                    text = "取消",
                    onClick = { showDialog = false },
                    type = GlassButtonType.SECONDARY
                )
            }
        )
    }
}

/**
 * 小数输入设置项：点击弹出对话框输入数值
 */
@Composable
private fun DecimalInputDialog(
    title: String,
    value: Double,
    label: String,
    onConfirm: (Double) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf(value.toString()) }

    NavigateSettingItem(
        title = title,
        onClick = {
            inputValue = value.toString()
            showDialog = true
        },
        description = label
    )

    if (showDialog) {
        GlassDialog(
            onDismissRequest = { showDialog = false },
            title = title,
            content = {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("请输入金额（元）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                GlassButton(
                    text = "确定",
                    onClick = {
                        inputValue.toDoubleOrNull()?.let(onConfirm)
                        showDialog = false
                    }
                )
            },
            dismissButton = {
                GlassButton(
                    text = "取消",
                    onClick = { showDialog = false },
                    type = GlassButtonType.SECONDARY
                )
            }
        )
    }
}

/** 主题模式显示文字 */
private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "跟随系统"
    ThemeMode.LIGHT -> "浅色"
    ThemeMode.DARK -> "深色"
}

/** 每周第一天显示文字 */
private fun firstDayOfWeekLabel(day: FirstDayOfWeek): String = when (day) {
    FirstDayOfWeek.MONDAY -> "周一"
    FirstDayOfWeek.SUNDAY -> "周日"
}

/** 语言显示文字 */
private fun languageLabel(lang: String): String = when (lang) {
    "zh-CN" -> "简体中文"
    "en-US" -> "English"
    "ja-JP" -> "日本語"
    else -> lang
}

/** 自动备份频率显示文字（Task 32.4） */
private fun autoBackupFrequencyLabel(frequency: String): String = when (frequency) {
    "daily" -> "每天"
    "weekly" -> "每周"
    "monthly" -> "每月"
    else -> frequency
}

/** 字体大小显示文字（Task 37.2） */
private fun fontScaleLabel(scale: Float): String = when {
    scale >= 1.3f -> "超大号"
    scale >= 1.15f -> "大号"
    else -> "标准"
}
