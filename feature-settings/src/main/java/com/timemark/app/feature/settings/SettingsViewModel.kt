package com.timemark.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.model.FirstDayOfWeek
import com.timemark.app.domain.model.LockMethod
import com.timemark.app.domain.model.ThemeMode
import com.timemark.app.domain.usecase.settings.GetSettingsUseCase
import com.timemark.app.domain.usecase.settings.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置页面 ViewModel
 *
 * 将各类设置项以 StateFlow 暴露给 UI，提供对应的写入方法。
 * 所有设置均通过 [GetSettingsUseCase] 读取、[UpdateSettingsUseCase] 写入。
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {

    /** 主题模式 */
    val themeMode: StateFlow<ThemeMode> = getSettingsUseCase.themeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    /** 模糊效果 */
    val blurEnabled: StateFlow<Boolean> = getSettingsUseCase.blurEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** 动画效果 */
    val animationEnabled: StateFlow<Boolean> = getSettingsUseCase.animationEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** 音效 */
    val soundEnabled: StateFlow<Boolean> = getSettingsUseCase.soundEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** 触觉反馈 */
    val hapticEnabled: StateFlow<Boolean> = getSettingsUseCase.hapticEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** 24 小时制 */
    val use24HourFormat: StateFlow<Boolean> = getSettingsUseCase.use24HourFormat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** 每周第一天 */
    val firstDayOfWeek: StateFlow<FirstDayOfWeek> = getSettingsUseCase.firstDayOfWeek()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FirstDayOfWeek.MONDAY)

    /** 语言 */
    val language: StateFlow<String> = getSettingsUseCase.language()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "zh-CN")

    /** 应用锁方式 */
    val lockMethod: StateFlow<LockMethod> = getSettingsUseCase.lockMethod()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LockMethod.NONE)

    /** 自动锁定时间（分钟） */
    val autoLockMinutes: StateFlow<Int> = getSettingsUseCase.autoLockMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    /** AI 全局开关 */
    val aiGlobalEnabled: StateFlow<Boolean> = getSettingsUseCase.aiGlobalEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** AI 仅 WiFi */
    val aiWifiOnly: StateFlow<Boolean> = getSettingsUseCase.aiWifiOnly()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 每日 Token 限制 */
    val dailyTokenLimit: StateFlow<Int> = getSettingsUseCase.dailyTokenLimit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100000)

    /** 每月 Token 限制 */
    val monthlyTokenLimit: StateFlow<Int> = getSettingsUseCase.monthlyTokenLimit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3000000)

    /** 每月预算限制 */
    val monthlyBudgetLimit: StateFlow<Double> = getSettingsUseCase.monthlyBudgetLimit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10.0)

    /** 协同模式 */
    val collaborativeMode: StateFlow<Boolean> = getSettingsUseCase.collaborativeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Task 32: 数据隐私与安全补全
    /** 应用锁开关 */
    val appLockEnabled: StateFlow<Boolean> = getSettingsUseCase.appLockEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 生物识别开关 */
    val biometricEnabled: StateFlow<Boolean> = getSettingsUseCase.biometricEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 安全屏幕开关 */
    val secureScreen: StateFlow<Boolean> = getSettingsUseCase.secureScreen()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 数据库加密开关 */
    val databaseEncryptionEnabled: StateFlow<Boolean> = getSettingsUseCase.databaseEncryptionEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 自动备份开关 */
    val autoBackupEnabledV2: StateFlow<Boolean> = getSettingsUseCase.autoBackupEnabledV2()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 自动备份频率 */
    val autoBackupFrequencyV2: StateFlow<String> = getSettingsUseCase.autoBackupFrequencyV2()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "weekly")

    /** 自动备份保留版本数 */
    val autoBackupKeepCount: StateFlow<Int> = getSettingsUseCase.autoBackupKeepCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    /** 网络请求日志开关 */
    val networkLogEnabled: StateFlow<Boolean> = getSettingsUseCase.networkLogEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 禁用网络访问开关 */
    val disableNetworkAccess: StateFlow<Boolean> = getSettingsUseCase.disableNetworkAccess()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Task 33.3: 各 AI 功能独立开关
    val aiFoodRecognitionEnabled: StateFlow<Boolean> = getSettingsUseCase.aiFoodRecognitionEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val aiNutritionAnalysisEnabled: StateFlow<Boolean> = getSettingsUseCase.aiNutritionAnalysisEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val aiChatEnabled: StateFlow<Boolean> = getSettingsUseCase.aiChatEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val aiWaterAnalysisEnabled: StateFlow<Boolean> = getSettingsUseCase.aiWaterAnalysisEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val aiExerciseAnalysisEnabled: StateFlow<Boolean> = getSettingsUseCase.aiExerciseAnalysisEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val aiSleepAnalysisEnabled: StateFlow<Boolean> = getSettingsUseCase.aiSleepAnalysisEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val aiHabitAnalysisEnabled: StateFlow<Boolean> = getSettingsUseCase.aiHabitAnalysisEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Task 33.4: Token 优化相关
    val aiCacheEnabled: StateFlow<Boolean> = getSettingsUseCase.aiCacheEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val aiImageQuality: StateFlow<Int> = getSettingsUseCase.aiImageQuality()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 80)

    // Task 37.2: 无障碍相关
    /** 高对比度模式开关 */
    val highContrastMode: StateFlow<Boolean> = getSettingsUseCase.highContrastMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 字体缩放比例 */
    val fontScale: StateFlow<Float> = getSettingsUseCase.fontScale()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    // Task 38.3: 日志管理相关
    /** 日志开关 */
    val loggingEnabled: StateFlow<Boolean> = getSettingsUseCase.loggingEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** 日志级别 */
    val logLevel: StateFlow<String> = getSettingsUseCase.logLevel()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DEBUG")

    // Task 38.4: 崩溃收集相关
    /** 崩溃收集开关 */
    val crashReportEnabled: StateFlow<Boolean> = getSettingsUseCase.crashReportEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { updateSettingsUseCase.themeMode(mode) }
    fun setBlurEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.blurEnabled(enabled) }
    fun setAnimationEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.animationEnabled(enabled) }
    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.soundEnabled(enabled) }
    fun setHapticEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.hapticEnabled(enabled) }
    fun setUse24HourFormat(use24: Boolean) = viewModelScope.launch { updateSettingsUseCase.use24HourFormat(use24) }
    fun setFirstDayOfWeek(day: FirstDayOfWeek) = viewModelScope.launch { updateSettingsUseCase.firstDayOfWeek(day) }
    fun setLanguage(lang: String) = viewModelScope.launch { updateSettingsUseCase.language(lang) }
    fun setLockMethod(method: LockMethod) = viewModelScope.launch { updateSettingsUseCase.lockMethod(method) }
    fun setAutoLockMinutes(minutes: Int) = viewModelScope.launch { updateSettingsUseCase.autoLockMinutes(minutes) }
    fun setAIGlobalEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.aiGlobalEnabled(enabled) }
    fun setAIWifiOnly(wifiOnly: Boolean) = viewModelScope.launch { updateSettingsUseCase.aiWifiOnly(wifiOnly) }
    fun setDailyTokenLimit(limit: Int) = viewModelScope.launch { updateSettingsUseCase.dailyTokenLimit(limit) }
    fun setMonthlyTokenLimit(limit: Int) = viewModelScope.launch { updateSettingsUseCase.monthlyTokenLimit(limit) }
    fun setMonthlyBudgetLimit(limit: Double) = viewModelScope.launch { updateSettingsUseCase.monthlyBudgetLimit(limit) }
    fun setCollaborativeMode(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.collaborativeMode(enabled) }

    // Task 32: 数据隐私与安全补全
    fun setSecureScreen(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.secureScreen(enabled) }
    fun setDatabaseEncryptionEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.databaseEncryptionEnabled(enabled) }
    fun setAutoBackupEnabledV2(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.autoBackupEnabledV2(enabled) }
    fun setAutoBackupFrequencyV2(frequency: String) = viewModelScope.launch { updateSettingsUseCase.autoBackupFrequencyV2(frequency) }
    fun setAutoBackupKeepCount(count: Int) = viewModelScope.launch { updateSettingsUseCase.autoBackupKeepCount(count) }
    fun setNetworkLogEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.networkLogEnabled(enabled) }
    fun setDisableNetworkAccess(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.disableNetworkAccess(enabled) }

    // Task 33.3: 各 AI 功能独立开关 setter
    fun setAIFoodRecognitionEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.aiFoodRecognitionEnabled(enabled) }
    fun setAINutritionAnalysisEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.aiNutritionAnalysisEnabled(enabled) }
    fun setAIChatEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.aiChatEnabled(enabled) }
    fun setAIWaterAnalysisEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.aiWaterAnalysisEnabled(enabled) }
    fun setAIExerciseAnalysisEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.aiExerciseAnalysisEnabled(enabled) }
    fun setAISleepAnalysisEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.aiSleepAnalysisEnabled(enabled) }
    fun setAIHabitAnalysisEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.aiHabitAnalysisEnabled(enabled) }

    // Task 33.4: Token 优化相关 setter
    fun setAICacheEnabled(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.aiCacheEnabled(enabled) }
    fun setAIImageQuality(quality: Int) = viewModelScope.launch { updateSettingsUseCase.aiImageQuality(quality) }

    // Task 37.2: 无障碍相关 setter
    fun setHighContrastMode(enabled: Boolean) = viewModelScope.launch { updateSettingsUseCase.highContrastMode(enabled) }
    fun setFontScale(scale: Float) = viewModelScope.launch { updateSettingsUseCase.fontScale(scale) }

    // Task 38.3: 日志管理相关 setter
    fun setLoggingEnabled(enabled: Boolean) = viewModelScope.launch {
        updateSettingsUseCase.loggingEnabled(enabled)
        // 同步到 Logger 工具
        com.timemark.app.core.utils.Logger.setLoggingEnabled(enabled)
    }

    fun setLogLevel(level: String) = viewModelScope.launch {
        updateSettingsUseCase.logLevel(level)
        // 同步到 Logger 工具
        val logLevel = when (level) {
            "VERBOSE" -> com.timemark.app.core.utils.Logger.LogLevel.VERBOSE
            "DEBUG" -> com.timemark.app.core.utils.Logger.LogLevel.DEBUG
            "INFO" -> com.timemark.app.core.utils.Logger.LogLevel.INFO
            "WARN" -> com.timemark.app.core.utils.Logger.LogLevel.WARN
            "ERROR" -> com.timemark.app.core.utils.Logger.LogLevel.ERROR
            else -> com.timemark.app.core.utils.Logger.LogLevel.DEBUG
        }
        com.timemark.app.core.utils.Logger.setLogLevel(logLevel)
    }

    // Task 38.4: 崩溃收集相关 setter
    fun setCrashReportEnabled(enabled: Boolean) = viewModelScope.launch {
        updateSettingsUseCase.crashReportEnabled(enabled)
    }
}
