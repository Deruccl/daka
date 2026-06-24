package com.timemark.app.domain.usecase.settings

import com.timemark.app.domain.model.FirstDayOfWeek
import com.timemark.app.domain.model.LockMethod
import com.timemark.app.domain.model.ThemeMode
import com.timemark.app.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 更新设置用例
 *
 * 提供各类设置项的写入方法，均为 suspend 操作。
 */
class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun themeMode(mode: ThemeMode) = settingsRepository.setThemeMode(mode)
    suspend fun blurEnabled(enabled: Boolean) = settingsRepository.setBlurEnabled(enabled)
    suspend fun animationEnabled(enabled: Boolean) = settingsRepository.setAnimationEnabled(enabled)
    suspend fun soundEnabled(enabled: Boolean) = settingsRepository.setSoundEnabled(enabled)
    suspend fun hapticEnabled(enabled: Boolean) = settingsRepository.setHapticEnabled(enabled)
    suspend fun use24HourFormat(use24: Boolean) = settingsRepository.setUse24HourFormat(use24)
    suspend fun firstDayOfWeek(day: FirstDayOfWeek) = settingsRepository.setFirstDayOfWeek(day)
    suspend fun language(lang: String) = settingsRepository.setLanguage(lang)
    suspend fun lockMethod(method: LockMethod) = settingsRepository.setLockMethod(method)
    suspend fun lockPassword(password: String?) = settingsRepository.setLockPassword(password)
    suspend fun autoLockMinutes(minutes: Int) = settingsRepository.setAutoLockMinutes(minutes)
    suspend fun aiGlobalEnabled(enabled: Boolean) = settingsRepository.setAIGlobalEnabled(enabled)
    suspend fun aiWifiOnly(wifiOnly: Boolean) = settingsRepository.setAIWifiOnly(wifiOnly)
    suspend fun dailyTokenLimit(limit: Int) = settingsRepository.setDailyTokenLimit(limit)
    suspend fun monthlyTokenLimit(limit: Int) = settingsRepository.setMonthlyTokenLimit(limit)
    suspend fun monthlyBudgetLimit(limit: Double) = settingsRepository.setMonthlyBudgetLimit(limit)
    suspend fun collaborativeMode(enabled: Boolean) = settingsRepository.setCollaborativeMode(enabled)

    // Task 32: 数据隐私与安全补全
    suspend fun appLockEnabled(enabled: Boolean) = settingsRepository.setAppLockEnabled(enabled)
    suspend fun biometricEnabled(enabled: Boolean) = settingsRepository.setBiometricEnabled(enabled)
    suspend fun secureScreen(enabled: Boolean) = settingsRepository.setSecureScreen(enabled)
    suspend fun autoBackupEnabledV2(enabled: Boolean) = settingsRepository.setAutoBackupEnabledV2(enabled)
    suspend fun autoBackupFrequencyV2(frequency: String) = settingsRepository.setAutoBackupFrequencyV2(frequency)
    suspend fun autoBackupKeepCount(count: Int) = settingsRepository.setAutoBackupKeepCount(count)
    suspend fun networkLogEnabled(enabled: Boolean) = settingsRepository.setNetworkLogEnabled(enabled)
    suspend fun disableNetworkAccess(enabled: Boolean) = settingsRepository.setDisableNetworkAccess(enabled)

    // Task 33.3: 各 AI 功能独立开关 setter
    suspend fun aiFoodRecognitionEnabled(enabled: Boolean) = settingsRepository.setAIFoodRecognitionEnabled(enabled)
    suspend fun aiNutritionAnalysisEnabled(enabled: Boolean) = settingsRepository.setAINutritionAnalysisEnabled(enabled)
    suspend fun aiChatEnabled(enabled: Boolean) = settingsRepository.setAIChatEnabled(enabled)
    suspend fun aiWaterAnalysisEnabled(enabled: Boolean) = settingsRepository.setAIWaterAnalysisEnabled(enabled)
    suspend fun aiExerciseAnalysisEnabled(enabled: Boolean) = settingsRepository.setAIExerciseAnalysisEnabled(enabled)
    suspend fun aiSleepAnalysisEnabled(enabled: Boolean) = settingsRepository.setAISleepAnalysisEnabled(enabled)
    suspend fun aiHabitAnalysisEnabled(enabled: Boolean) = settingsRepository.setAIHabitAnalysisEnabled(enabled)

    // Task 33.4: Token 优化相关 setter
    suspend fun aiCacheEnabled(enabled: Boolean) = settingsRepository.setAICacheEnabled(enabled)
    suspend fun aiImageQuality(quality: Int) = settingsRepository.setAIImageQuality(quality)

    /** 设置应用锁密码（明文，内部经 Keystore 加密后存储） */
    suspend fun lockPassword(password: String?) = settingsRepository.setLockPassword(password)

    /** 设置数据库加密开关 */
    suspend fun databaseEncryptionEnabled(enabled: Boolean) = settingsRepository.setDatabaseEncryptionEnabled(enabled)

    /** 设置数据库密码（明文，内部经 Keystore 加密后存储） */
    suspend fun databasePassword(password: String?) = settingsRepository.setDatabasePassword(password)
}
