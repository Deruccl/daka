package com.timemark.app.domain.usecase.settings

import com.timemark.app.domain.model.FirstDayOfWeek
import com.timemark.app.domain.model.LockMethod
import com.timemark.app.domain.model.ThemeMode
import com.timemark.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取设置用例
 *
 * 暴露各类设置项的 Flow，便于 UI 层订阅。
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun themeMode(): Flow<ThemeMode> = settingsRepository.themeMode
    fun blurEnabled(): Flow<Boolean> = settingsRepository.blurEnabled
    fun animationEnabled(): Flow<Boolean> = settingsRepository.animationEnabled
    fun soundEnabled(): Flow<Boolean> = settingsRepository.soundEnabled
    fun hapticEnabled(): Flow<Boolean> = settingsRepository.hapticEnabled
    fun use24HourFormat(): Flow<Boolean> = settingsRepository.use24HourFormat
    fun firstDayOfWeek(): Flow<FirstDayOfWeek> = settingsRepository.firstDayOfWeek
    fun language(): Flow<String> = settingsRepository.language
    fun lockMethod(): Flow<LockMethod> = settingsRepository.lockMethod
    fun autoLockMinutes(): Flow<Int> = settingsRepository.autoLockMinutes
    fun aiGlobalEnabled(): Flow<Boolean> = settingsRepository.aiGlobalEnabled
    fun aiWifiOnly(): Flow<Boolean> = settingsRepository.aiWifiOnly
    fun dailyTokenLimit(): Flow<Int> = settingsRepository.dailyTokenLimit
    fun monthlyTokenLimit(): Flow<Int> = settingsRepository.monthlyTokenLimit
    fun monthlyBudgetLimit(): Flow<Double> = settingsRepository.monthlyBudgetLimit
    fun collaborativeMode(): Flow<Boolean> = settingsRepository.collaborativeMode

    // Task 32: 数据隐私与安全补全
    fun appLockEnabled(): Flow<Boolean> = settingsRepository.appLockEnabled
    fun biometricEnabled(): Flow<Boolean> = settingsRepository.biometricEnabled
    fun secureScreen(): Flow<Boolean> = settingsRepository.secureScreen
    fun autoBackupEnabledV2(): Flow<Boolean> = settingsRepository.autoBackupEnabledV2
    fun autoBackupFrequencyV2(): Flow<String> = settingsRepository.autoBackupFrequencyV2
    fun autoBackupKeepCount(): Flow<Int> = settingsRepository.autoBackupKeepCount
    fun networkLogEnabled(): Flow<Boolean> = settingsRepository.networkLogEnabled
    fun disableNetworkAccess(): Flow<Boolean> = settingsRepository.disableNetworkAccess

    // Task 33.3: 各 AI 功能独立开关
    fun aiFoodRecognitionEnabled(): Flow<Boolean> = settingsRepository.aiFoodRecognitionEnabled
    fun aiNutritionAnalysisEnabled(): Flow<Boolean> = settingsRepository.aiNutritionAnalysisEnabled
    fun aiChatEnabled(): Flow<Boolean> = settingsRepository.aiChatEnabled
    fun aiWaterAnalysisEnabled(): Flow<Boolean> = settingsRepository.aiWaterAnalysisEnabled
    fun aiExerciseAnalysisEnabled(): Flow<Boolean> = settingsRepository.aiExerciseAnalysisEnabled
    fun aiSleepAnalysisEnabled(): Flow<Boolean> = settingsRepository.aiSleepAnalysisEnabled
    fun aiHabitAnalysisEnabled(): Flow<Boolean> = settingsRepository.aiHabitAnalysisEnabled

    // Task 33.4: Token 优化相关
    fun aiCacheEnabled(): Flow<Boolean> = settingsRepository.aiCacheEnabled
    fun aiImageQuality(): Flow<Int> = settingsRepository.aiImageQuality

    /** 获取应用锁密码（明文，已从 Keystore 解密） */
    fun lockPassword(): Flow<String?> = settingsRepository.lockPassword

    /** 获取数据库加密开关 */
    fun databaseEncryptionEnabled(): Flow<Boolean> = settingsRepository.databaseEncryptionEnabled

    /** 获取数据库密码（明文，已从 Keystore 解密） */
    fun databasePassword(): Flow<String?> = settingsRepository.databasePassword
}
