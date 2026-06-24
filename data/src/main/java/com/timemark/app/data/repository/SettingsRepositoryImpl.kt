package com.timemark.app.data.repository

import com.timemark.app.data.datastore.SettingsDataStore
import com.timemark.app.domain.model.FirstDayOfWeek
import com.timemark.app.domain.model.LockMethod
import com.timemark.app.domain.model.ThemeMode
import com.timemark.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * SettingsRepository 接口实现。
 * 委托给 SettingsDataStore 完成实际的偏好设置读写。
 */
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override val themeMode: Flow<ThemeMode> = settingsDataStore.themeMode
    override val blurEnabled: Flow<Boolean> = settingsDataStore.blurEnabled
    override val animationEnabled: Flow<Boolean> = settingsDataStore.animationEnabled
    override val soundEnabled: Flow<Boolean> = settingsDataStore.soundEnabled
    override val hapticEnabled: Flow<Boolean> = settingsDataStore.hapticEnabled
    override val use24HourFormat: Flow<Boolean> = settingsDataStore.use24HourFormat
    override val firstDayOfWeek: Flow<FirstDayOfWeek> = settingsDataStore.firstDayOfWeek
    override val language: Flow<String> = settingsDataStore.language
    override val lockMethod: Flow<LockMethod> = settingsDataStore.lockMethod
    override val lockPassword: Flow<String?> = settingsDataStore.lockPassword
    override val autoLockMinutes: Flow<Int> = settingsDataStore.autoLockMinutes
    override val databaseEncryptionEnabled: Flow<Boolean> = settingsDataStore.databaseEncryptionEnabled
    override val databasePassword: Flow<String?> = settingsDataStore.databasePassword
    override val aiGlobalEnabled: Flow<Boolean> = settingsDataStore.aiGlobalEnabled
    override val aiWifiOnly: Flow<Boolean> = settingsDataStore.aiWifiOnly
    override val dailyTokenLimit: Flow<Int> = settingsDataStore.dailyTokenLimit
    override val monthlyTokenLimit: Flow<Int> = settingsDataStore.monthlyTokenLimit
    override val monthlyBudgetLimit: Flow<Double> = settingsDataStore.monthlyBudgetLimit
    override val collaborativeMode: Flow<Boolean> = settingsDataStore.collaborativeMode

    // Task 32: 数据隐私与安全补全
    override val appLockEnabled: Flow<Boolean> = settingsDataStore.appLockEnabled
    override val biometricEnabled: Flow<Boolean> = settingsDataStore.biometricEnabled
    override val secureScreen: Flow<Boolean> = settingsDataStore.secureScreen
    override val autoBackupEnabledV2: Flow<Boolean> = settingsDataStore.autoBackupEnabledV2
    override val autoBackupFrequencyV2: Flow<String> = settingsDataStore.autoBackupFrequencyV2
    override val autoBackupKeepCount: Flow<Int> = settingsDataStore.autoBackupKeepCount
    override val networkLogEnabled: Flow<Boolean> = settingsDataStore.networkLogEnabled
    override val disableNetworkAccess: Flow<Boolean> = settingsDataStore.disableNetworkAccess

    // Task 33.3: 各 AI 功能独立开关
    override val aiFoodRecognitionEnabled: Flow<Boolean> = settingsDataStore.aiFoodRecognitionEnabled
    override val aiNutritionAnalysisEnabled: Flow<Boolean> = settingsDataStore.aiNutritionAnalysisEnabled
    override val aiChatEnabled: Flow<Boolean> = settingsDataStore.aiChatEnabled
    override val aiWaterAnalysisEnabled: Flow<Boolean> = settingsDataStore.aiWaterAnalysisEnabled
    override val aiExerciseAnalysisEnabled: Flow<Boolean> = settingsDataStore.aiExerciseAnalysisEnabled
    override val aiSleepAnalysisEnabled: Flow<Boolean> = settingsDataStore.aiSleepAnalysisEnabled
    override val aiHabitAnalysisEnabled: Flow<Boolean> = settingsDataStore.aiHabitAnalysisEnabled

    // Task 33.4: Token 优化相关
    override val aiCacheEnabled: Flow<Boolean> = settingsDataStore.aiCacheEnabled
    override val aiImageQuality: Flow<Int> = settingsDataStore.aiImageQuality

    override suspend fun setThemeMode(mode: ThemeMode) = settingsDataStore.setThemeMode(mode)
    override suspend fun setBlurEnabled(enabled: Boolean) = settingsDataStore.setBlurEnabled(enabled)
    override suspend fun setAnimationEnabled(enabled: Boolean) = settingsDataStore.setAnimationEnabled(enabled)
    override suspend fun setSoundEnabled(enabled: Boolean) = settingsDataStore.setSoundEnabled(enabled)
    override suspend fun setHapticEnabled(enabled: Boolean) = settingsDataStore.setHapticEnabled(enabled)
    override suspend fun setUse24HourFormat(use24: Boolean) = settingsDataStore.setUse24HourFormat(use24)
    override suspend fun setFirstDayOfWeek(day: FirstDayOfWeek) = settingsDataStore.setFirstDayOfWeek(day)
    override suspend fun setLanguage(lang: String) = settingsDataStore.setLanguage(lang)
    override suspend fun setLockMethod(method: LockMethod) = settingsDataStore.setLockMethod(method)
    override suspend fun setLockPassword(password: String?) = settingsDataStore.setLockPassword(password)
    override suspend fun setAutoLockMinutes(minutes: Int) = settingsDataStore.setAutoLockMinutes(minutes)
    override suspend fun setDatabaseEncryptionEnabled(enabled: Boolean) =
        settingsDataStore.setDatabaseEncryptionEnabled(enabled)
    override suspend fun setDatabasePassword(password: String?) = settingsDataStore.setDatabasePassword(password)
    override suspend fun setAIGlobalEnabled(enabled: Boolean) = settingsDataStore.setAIGlobalEnabled(enabled)
    override suspend fun setAIWifiOnly(wifiOnly: Boolean) = settingsDataStore.setAIWifiOnly(wifiOnly)
    override suspend fun setDailyTokenLimit(limit: Int) = settingsDataStore.setDailyTokenLimit(limit)
    override suspend fun setMonthlyTokenLimit(limit: Int) = settingsDataStore.setMonthlyTokenLimit(limit)
    override suspend fun setMonthlyBudgetLimit(limit: Double) = settingsDataStore.setMonthlyBudgetLimit(limit)
    override suspend fun setCollaborativeMode(enabled: Boolean) = settingsDataStore.setCollaborativeMode(enabled)

    // Task 32: 数据隐私与安全补全
    override suspend fun setAppLockEnabled(enabled: Boolean) = settingsDataStore.setAppLockEnabled(enabled)
    override suspend fun setBiometricEnabled(enabled: Boolean) = settingsDataStore.setBiometricEnabled(enabled)
    override suspend fun setSecureScreen(enabled: Boolean) = settingsDataStore.setSecureScreen(enabled)
    override suspend fun setAutoBackupEnabledV2(enabled: Boolean) = settingsDataStore.setAutoBackupEnabledV2(enabled)
    override suspend fun setAutoBackupFrequencyV2(frequency: String) = settingsDataStore.setAutoBackupFrequencyV2(frequency)
    override suspend fun setAutoBackupKeepCount(count: Int) = settingsDataStore.setAutoBackupKeepCount(count)
    override suspend fun setNetworkLogEnabled(enabled: Boolean) = settingsDataStore.setNetworkLogEnabled(enabled)
    override suspend fun setDisableNetworkAccess(enabled: Boolean) = settingsDataStore.setDisableNetworkAccess(enabled)

    // Task 33.3: 各 AI 功能独立开关 setter
    override suspend fun setAIFoodRecognitionEnabled(enabled: Boolean) = settingsDataStore.setAIFoodRecognitionEnabled(enabled)
    override suspend fun setAINutritionAnalysisEnabled(enabled: Boolean) = settingsDataStore.setAINutritionAnalysisEnabled(enabled)
    override suspend fun setAIChatEnabled(enabled: Boolean) = settingsDataStore.setAIChatEnabled(enabled)
    override suspend fun setAIWaterAnalysisEnabled(enabled: Boolean) = settingsDataStore.setAIWaterAnalysisEnabled(enabled)
    override suspend fun setAIExerciseAnalysisEnabled(enabled: Boolean) = settingsDataStore.setAIExerciseAnalysisEnabled(enabled)
    override suspend fun setAISleepAnalysisEnabled(enabled: Boolean) = settingsDataStore.setAISleepAnalysisEnabled(enabled)
    override suspend fun setAIHabitAnalysisEnabled(enabled: Boolean) = settingsDataStore.setAIHabitAnalysisEnabled(enabled)

    // Task 33.4: Token 优化相关 setter
    override suspend fun setAICacheEnabled(enabled: Boolean) = settingsDataStore.setAICacheEnabled(enabled)
    override suspend fun setAIImageQuality(quality: Int) = settingsDataStore.setAIImageQuality(quality)
}
