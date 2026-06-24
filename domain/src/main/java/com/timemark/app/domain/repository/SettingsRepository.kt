package com.timemark.app.domain.repository

import com.timemark.app.domain.model.FirstDayOfWeek
import com.timemark.app.domain.model.LockMethod
import com.timemark.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** 设置仓库接口 */
interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val blurEnabled: Flow<Boolean>
    val animationEnabled: Flow<Boolean>
    val soundEnabled: Flow<Boolean>
    val hapticEnabled: Flow<Boolean>
    val use24HourFormat: Flow<Boolean>
    val firstDayOfWeek: Flow<FirstDayOfWeek>
    val language: Flow<String>
    val lockMethod: Flow<LockMethod>
    val lockPassword: Flow<String?>  // 加密后的密码
    val autoLockMinutes: Flow<Int>
    val databaseEncryptionEnabled: Flow<Boolean>
    val databasePassword: Flow<String?>  // 加密后的密码
    val aiGlobalEnabled: Flow<Boolean>
    val aiWifiOnly: Flow<Boolean>
    val dailyTokenLimit: Flow<Int>
    val monthlyTokenLimit: Flow<Int>
    val monthlyBudgetLimit: Flow<Double>
    val collaborativeMode: Flow<Boolean>

    // Task 32: 数据隐私与安全补全
    val appLockEnabled: Flow<Boolean>
    val biometricEnabled: Flow<Boolean>
    val secureScreen: Flow<Boolean>
    val autoBackupEnabledV2: Flow<Boolean>
    val autoBackupFrequencyV2: Flow<String>
    val autoBackupKeepCount: Flow<Int>
    val networkLogEnabled: Flow<Boolean>
    val disableNetworkAccess: Flow<Boolean>

    // Task 33.3: 各 AI 功能独立开关
    val aiFoodRecognitionEnabled: Flow<Boolean>
    val aiNutritionAnalysisEnabled: Flow<Boolean>
    val aiChatEnabled: Flow<Boolean>
    val aiWaterAnalysisEnabled: Flow<Boolean>
    val aiExerciseAnalysisEnabled: Flow<Boolean>
    val aiSleepAnalysisEnabled: Flow<Boolean>
    val aiHabitAnalysisEnabled: Flow<Boolean>

    // Task 33.4: Token 优化相关
    val aiCacheEnabled: Flow<Boolean>
    val aiImageQuality: Flow<Int>

    // Task 37.2: 无障碍相关
    val highContrastMode: Flow<Boolean>
    val fontScale: Flow<Float>

    // Task 38.3: 日志管理相关
    val loggingEnabled: Flow<Boolean>
    val logLevel: Flow<String>

    // Task 38.4: 崩溃收集相关
    val crashReportEnabled: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setBlurEnabled(enabled: Boolean)
    suspend fun setAnimationEnabled(enabled: Boolean)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setHapticEnabled(enabled: Boolean)
    suspend fun setUse24HourFormat(use24: Boolean)
    suspend fun setFirstDayOfWeek(day: FirstDayOfWeek)
    suspend fun setLanguage(lang: String)
    suspend fun setLockMethod(method: LockMethod)
    suspend fun setLockPassword(password: String?)
    suspend fun setAutoLockMinutes(minutes: Int)
    suspend fun setDatabaseEncryptionEnabled(enabled: Boolean)
    suspend fun setDatabasePassword(password: String?)
    suspend fun setAIGlobalEnabled(enabled: Boolean)
    suspend fun setAIWifiOnly(wifiOnly: Boolean)
    suspend fun setDailyTokenLimit(limit: Int)
    suspend fun setMonthlyTokenLimit(limit: Int)
    suspend fun setMonthlyBudgetLimit(limit: Double)
    suspend fun setCollaborativeMode(enabled: Boolean)

    // Task 32: 数据隐私与安全补全
    suspend fun setAppLockEnabled(enabled: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setSecureScreen(enabled: Boolean)
    suspend fun setAutoBackupEnabledV2(enabled: Boolean)
    suspend fun setAutoBackupFrequencyV2(frequency: String)
    suspend fun setAutoBackupKeepCount(count: Int)
    suspend fun setNetworkLogEnabled(enabled: Boolean)
    suspend fun setDisableNetworkAccess(enabled: Boolean)

    // Task 33.3: 各 AI 功能独立开关 setter
    suspend fun setAIFoodRecognitionEnabled(enabled: Boolean)
    suspend fun setAINutritionAnalysisEnabled(enabled: Boolean)
    suspend fun setAIChatEnabled(enabled: Boolean)
    suspend fun setAIWaterAnalysisEnabled(enabled: Boolean)
    suspend fun setAIExerciseAnalysisEnabled(enabled: Boolean)
    suspend fun setAISleepAnalysisEnabled(enabled: Boolean)
    suspend fun setAIHabitAnalysisEnabled(enabled: Boolean)

    // Task 33.4: Token 优化相关 setter
    suspend fun setAICacheEnabled(enabled: Boolean)
    suspend fun setAIImageQuality(quality: Int)

    // Task 37.2: 无障碍相关 setter
    suspend fun setHighContrastMode(enabled: Boolean)
    suspend fun setFontScale(scale: Float)

    // Task 38.3: 日志管理相关 setter
    suspend fun setLoggingEnabled(enabled: Boolean)
    suspend fun setLogLevel(level: String)

    // Task 38.4: 崩溃收集相关 setter
    suspend fun setCrashReportEnabled(enabled: Boolean)
}
