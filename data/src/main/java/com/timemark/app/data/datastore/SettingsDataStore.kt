package com.timemark.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.timemark.app.data.security.KeystoreCrypto
import com.timemark.app.domain.model.FirstDayOfWeek
import com.timemark.app.domain.model.LockMethod
import com.timemark.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 设置项 Preferences Key 集合。
 * 密码类 key（LOCK_PASSWORD、DATABASE_PASSWORD）存储的是经 KeystoreCrypto 加密后的密文。
 */
object SettingsKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val BLUR_ENABLED = booleanPreferencesKey("blur_enabled")
    val ANIMATION_ENABLED = booleanPreferencesKey("animation_enabled")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    val USE_24_HOUR_FORMAT = booleanPreferencesKey("use_24_hour_format")
    val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
    val LANGUAGE = stringPreferencesKey("language")
    val LOCK_METHOD = stringPreferencesKey("lock_method")
    val LOCK_PASSWORD = stringPreferencesKey("lock_password") // 加密后
    val AUTO_LOCK_MINUTES = intPreferencesKey("auto_lock_minutes")
    val DATABASE_ENCRYPTION_ENABLED = booleanPreferencesKey("database_encryption_enabled")
    val DATABASE_PASSWORD = stringPreferencesKey("database_password") // 加密后
    val AI_GLOBAL_ENABLED = booleanPreferencesKey("ai_global_enabled")
    val AI_WIFI_ONLY = booleanPreferencesKey("ai_wifi_only")
    val DAILY_TOKEN_LIMIT = intPreferencesKey("daily_token_limit")
    val MONTHLY_TOKEN_LIMIT = intPreferencesKey("monthly_token_limit")
    val MONTHLY_BUDGET_LIMIT = doublePreferencesKey("monthly_budget_limit")
    val COLLABORATIVE_MODE = booleanPreferencesKey("collaborative_mode")

    // Task 32: 数据隐私与安全补全相关 key
    val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    val SECURE_SCREEN = booleanPreferencesKey("secure_screen")
    val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled_v2")
    val AUTO_BACKUP_FREQUENCY = stringPreferencesKey("auto_backup_frequency_v2") // daily/weekly/monthly
    val AUTO_BACKUP_KEEP_COUNT = intPreferencesKey("auto_backup_keep_count")
    val NETWORK_LOG_ENABLED = booleanPreferencesKey("network_log_enabled")
    val DISABLE_NETWORK_ACCESS = booleanPreferencesKey("disable_network_access")

    // Task 33.3: 各 AI 功能独立开关
    val AI_FOOD_RECOGNITION_ENABLED = booleanPreferencesKey("ai_food_recognition_enabled")
    val AI_NUTRITION_ANALYSIS_ENABLED = booleanPreferencesKey("ai_nutrition_analysis_enabled")
    val AI_CHAT_ENABLED = booleanPreferencesKey("ai_chat_enabled")
    val AI_WATER_ANALYSIS_ENABLED = booleanPreferencesKey("ai_water_analysis_enabled")
    val AI_EXERCISE_ANALYSIS_ENABLED = booleanPreferencesKey("ai_exercise_analysis_enabled")
    val AI_SLEEP_ANALYSIS_ENABLED = booleanPreferencesKey("ai_sleep_analysis_enabled")
    val AI_HABIT_ANALYSIS_ENABLED = booleanPreferencesKey("ai_habit_analysis_enabled")

    // Task 33.4: Token 优化相关 key
    val AI_CACHE_ENABLED = booleanPreferencesKey("ai_cache_enabled")
    val AI_IMAGE_QUALITY = intPreferencesKey("ai_image_quality") // 50-100
}

/**
 * 基于 Preferences DataStore 的设置存储实现。
 * 负责读写所有应用偏好设置；密码类设置通过 KeystoreCrypto 加密后存储。
 */
class SettingsDataStore(private val context: Context) {

    /** 主题模式 */
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        prefs[SettingsKeys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    /** 模糊效果开关 */
    val blurEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.BLUR_ENABLED] ?: true }

    /** 动画开关 */
    val animationEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.ANIMATION_ENABLED] ?: true }

    /** 音效开关 */
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.SOUND_ENABLED] ?: true }

    /** 触觉反馈开关 */
    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.HAPTIC_ENABLED] ?: true }

    /** 24 小时制开关 */
    val use24HourFormat: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.USE_24_HOUR_FORMAT] ?: true }

    /** 每周第一天 */
    val firstDayOfWeek: Flow<FirstDayOfWeek> = context.dataStore.data.map { prefs ->
        prefs[SettingsKeys.FIRST_DAY_OF_WEEK]?.let { runCatching { FirstDayOfWeek.valueOf(it) }.getOrNull() }
            ?: FirstDayOfWeek.MONDAY
    }

    /** 语言代码（如 zh-CN） */
    val language: Flow<String> = context.dataStore.data.map { it[SettingsKeys.LANGUAGE] ?: "zh-CN" }

    /** 应用锁方式 */
    val lockMethod: Flow<LockMethod> = context.dataStore.data.map { prefs ->
        prefs[SettingsKeys.LOCK_METHOD]?.let { runCatching { LockMethod.valueOf(it) }.getOrNull() }
            ?: LockMethod.NONE
    }

    /** 应用锁密码（明文，从 Keystore 解密后返回；未设置返回 null） */
    val lockPassword: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SettingsKeys.LOCK_PASSWORD]?.let { decryptOrNull(it) }
    }

    /** 自动锁定分钟数 */
    val autoLockMinutes: Flow<Int> = context.dataStore.data.map { it[SettingsKeys.AUTO_LOCK_MINUTES] ?: 5 }

    /** 数据库加密开关 */
    val databaseEncryptionEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[SettingsKeys.DATABASE_ENCRYPTION_ENABLED] ?: false }

    /** 数据库密码（明文，从 Keystore 解密后返回；未设置返回 null） */
    val databasePassword: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SettingsKeys.DATABASE_PASSWORD]?.let { decryptOrNull(it) }
    }

    /** AI 全局开关 */
    val aiGlobalEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AI_GLOBAL_ENABLED] ?: false }

    /** AI 仅 Wi-Fi 下使用 */
    val aiWifiOnly: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AI_WIFI_ONLY] ?: false }

    /** 每日 Token 上限 */
    val dailyTokenLimit: Flow<Int> = context.dataStore.data.map { it[SettingsKeys.DAILY_TOKEN_LIMIT] ?: 100000 }

    /** 每月 Token 上限 */
    val monthlyTokenLimit: Flow<Int> = context.dataStore.data.map { it[SettingsKeys.MONTHLY_TOKEN_LIMIT] ?: 3000000 }

    /** 每月预算上限 */
    val monthlyBudgetLimit: Flow<Double> = context.dataStore.data.map { it[SettingsKeys.MONTHLY_BUDGET_LIMIT] ?: 10.0 }

    /** 协作模式开关 */
    val collaborativeMode: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.COLLABORATIVE_MODE] ?: true }

    /** 应用锁开关（Task 32.1） */
    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.APP_LOCK_ENABLED] ?: false }

    /** 生物识别开关（Task 32.1） */
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.BIOMETRIC_ENABLED] ?: false }

    /** 安全屏幕开关：禁止截图与最近任务列表内容预览（Task 32.2） */
    val secureScreen: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.SECURE_SCREEN] ?: false }

    /** 自动备份开关（Task 32.4） */
    val autoBackupEnabledV2: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AUTO_BACKUP_ENABLED] ?: false }

    /** 自动备份频率：daily/weekly/monthly（Task 32.4） */
    val autoBackupFrequencyV2: Flow<String> = context.dataStore.data.map { it[SettingsKeys.AUTO_BACKUP_FREQUENCY] ?: "weekly" }

    /** 自动备份保留版本数（Task 32.4） */
    val autoBackupKeepCount: Flow<Int> = context.dataStore.data.map { it[SettingsKeys.AUTO_BACKUP_KEEP_COUNT] ?: 7 }

    /** 网络请求日志开关（Task 32.6） */
    val networkLogEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.NETWORK_LOG_ENABLED] ?: false }

    /** 禁用网络访问开关（Task 32.6） */
    val disableNetworkAccess: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.DISABLE_NETWORK_ACCESS] ?: false }

    // Task 33.3: 各 AI 功能独立开关（默认 true，仅在用户主动关闭时禁用）
    /** 食物识别开关 */
    val aiFoodRecognitionEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AI_FOOD_RECOGNITION_ENABLED] ?: true }

    /** 营养分析开关 */
    val aiNutritionAnalysisEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AI_NUTRITION_ANALYSIS_ENABLED] ?: true }

    /** AI 聊天开关 */
    val aiChatEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AI_CHAT_ENABLED] ?: true }

    /** 饮水分析开关 */
    val aiWaterAnalysisEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AI_WATER_ANALYSIS_ENABLED] ?: true }

    /** 运动分析开关 */
    val aiExerciseAnalysisEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AI_EXERCISE_ANALYSIS_ENABLED] ?: true }

    /** 睡眠分析开关 */
    val aiSleepAnalysisEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AI_SLEEP_ANALYSIS_ENABLED] ?: true }

    /** 习惯分析开关 */
    val aiHabitAnalysisEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AI_HABIT_ANALYSIS_ENABLED] ?: true }

    // Task 33.4: Token 优化相关
    /** AI 响应缓存开关 */
    val aiCacheEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.AI_CACHE_ENABLED] ?: true }

    /** 图片压缩质量（50-100） */
    val aiImageQuality: Flow<Int> = context.dataStore.data.map { it[SettingsKeys.AI_IMAGE_QUALITY] ?: 80 }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[SettingsKeys.THEME_MODE] = mode.name }
    }

    suspend fun setBlurEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.BLUR_ENABLED] = enabled }
    }

    suspend fun setAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.ANIMATION_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setUse24HourFormat(use24: Boolean) {
        context.dataStore.edit { it[SettingsKeys.USE_24_HOUR_FORMAT] = use24 }
    }

    suspend fun setFirstDayOfWeek(day: FirstDayOfWeek) {
        context.dataStore.edit { it[SettingsKeys.FIRST_DAY_OF_WEEK] = day.name }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[SettingsKeys.LANGUAGE] = lang }
    }

    suspend fun setLockMethod(method: LockMethod) {
        context.dataStore.edit { it[SettingsKeys.LOCK_METHOD] = method.name }
    }

    /**
     * 设置应用锁密码。传入 null 清空密码。
     * 明文经 KeystoreCrypto 加密后存储。
     */
    suspend fun setLockPassword(password: String?) {
        context.dataStore.edit { prefs ->
            if (password.isNullOrEmpty()) {
                prefs.remove(SettingsKeys.LOCK_PASSWORD)
            } else {
                prefs[SettingsKeys.LOCK_PASSWORD] = KeystoreCrypto.encrypt(password)
            }
        }
    }

    suspend fun setAutoLockMinutes(minutes: Int) {
        context.dataStore.edit { it[SettingsKeys.AUTO_LOCK_MINUTES] = minutes }
    }

    suspend fun setDatabaseEncryptionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.DATABASE_ENCRYPTION_ENABLED] = enabled }
    }

    /**
     * 设置数据库密码。传入 null 清空密码。
     * 明文经 KeystoreCrypto 加密后存储。
     */
    suspend fun setDatabasePassword(password: String?) {
        context.dataStore.edit { prefs ->
            if (password.isNullOrEmpty()) {
                prefs.remove(SettingsKeys.DATABASE_PASSWORD)
            } else {
                prefs[SettingsKeys.DATABASE_PASSWORD] = KeystoreCrypto.encrypt(password)
            }
        }
    }

    suspend fun setAIGlobalEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AI_GLOBAL_ENABLED] = enabled }
    }

    suspend fun setAIWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AI_WIFI_ONLY] = wifiOnly }
    }

    suspend fun setDailyTokenLimit(limit: Int) {
        context.dataStore.edit { it[SettingsKeys.DAILY_TOKEN_LIMIT] = limit }
    }

    suspend fun setMonthlyTokenLimit(limit: Int) {
        context.dataStore.edit { it[SettingsKeys.MONTHLY_TOKEN_LIMIT] = limit }
    }

    suspend fun setMonthlyBudgetLimit(limit: Double) {
        context.dataStore.edit { it[SettingsKeys.MONTHLY_BUDGET_LIMIT] = limit }
    }

    suspend fun setCollaborativeMode(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.COLLABORATIVE_MODE] = enabled }
    }

    /** 设置应用锁开关（Task 32.1） */
    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.APP_LOCK_ENABLED] = enabled }
    }

    /** 设置生物识别开关（Task 32.1） */
    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.BIOMETRIC_ENABLED] = enabled }
    }

    /** 设置安全屏幕开关（Task 32.2） */
    suspend fun setSecureScreen(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.SECURE_SCREEN] = enabled }
    }

    /** 设置自动备份开关（Task 32.4） */
    suspend fun setAutoBackupEnabledV2(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AUTO_BACKUP_ENABLED] = enabled }
    }

    /** 设置自动备份频率（Task 32.4） */
    suspend fun setAutoBackupFrequencyV2(frequency: String) {
        context.dataStore.edit { it[SettingsKeys.AUTO_BACKUP_FREQUENCY] = frequency }
    }

    /** 设置自动备份保留版本数（Task 32.4） */
    suspend fun setAutoBackupKeepCount(count: Int) {
        context.dataStore.edit { it[SettingsKeys.AUTO_BACKUP_KEEP_COUNT] = count }
    }

    /** 设置网络请求日志开关（Task 32.6） */
    suspend fun setNetworkLogEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.NETWORK_LOG_ENABLED] = enabled }
    }

    /** 设置禁用网络访问开关（Task 32.6） */
    suspend fun setDisableNetworkAccess(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.DISABLE_NETWORK_ACCESS] = enabled }
    }

    // Task 33.3: 各 AI 功能独立开关 setter
    suspend fun setAIFoodRecognitionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AI_FOOD_RECOGNITION_ENABLED] = enabled }
    }

    suspend fun setAINutritionAnalysisEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AI_NUTRITION_ANALYSIS_ENABLED] = enabled }
    }

    suspend fun setAIChatEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AI_CHAT_ENABLED] = enabled }
    }

    suspend fun setAIWaterAnalysisEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AI_WATER_ANALYSIS_ENABLED] = enabled }
    }

    suspend fun setAIExerciseAnalysisEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AI_EXERCISE_ANALYSIS_ENABLED] = enabled }
    }

    suspend fun setAISleepAnalysisEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AI_SLEEP_ANALYSIS_ENABLED] = enabled }
    }

    suspend fun setAIHabitAnalysisEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AI_HABIT_ANALYSIS_ENABLED] = enabled }
    }

    // Task 33.4: Token 优化相关 setter
    suspend fun setAICacheEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.AI_CACHE_ENABLED] = enabled }
    }

    /** 设置图片压缩质量（限制在 50-100） */
    suspend fun setAIImageQuality(quality: Int) {
        val clamped = quality.coerceIn(50, 100)
        context.dataStore.edit { it[SettingsKeys.AI_IMAGE_QUALITY] = clamped }
    }

    /** 解密密文，失败时返回 null（兼容历史数据或异常情况）。 */
    private fun decryptOrNull(cipherText: String): String? = runCatching {
        KeystoreCrypto.decrypt(cipherText)
    }.getOrNull()
}
