package com.timemark.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.timemark.app.backup.AutoBackupScheduler
import com.timemark.app.core.utils.Logger
import com.timemark.app.crash.CrashHandler
import com.timemark.app.data.datastore.SettingsDataStore
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 应用 Application 类
 *
 * 职责：
 * - 作为 Hilt 依赖注入入口（@HiltAndroidApp）
 * - 创建通知渠道（打卡提醒 / AI 任务）
 * - 通过 Configuration.Provider 提供 WorkManager 自定义配置（使用 HiltWorkerFactory）
 * - Task 32.4: 初始化自动备份调度
 * - 在 debug 构建中配置 LeakCanary 内存泄漏检测
 *
 * onCreate 仅执行必要初始化，保证冷启动 < 1s。
 */
@HiltAndroidApp
class TimeMarkApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var autoBackupScheduler: AutoBackupScheduler

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    /** 崩溃处理器实例 */
    private var crashHandler: CrashHandler? = null

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Task 38.3: 初始化日志工具，设置应用上下文以启用文件日志
        Logger.init(this)

        // Task 38.4: 注册崩溃处理器（先安装，再根据设置决定是否启用）
        crashHandler = CrashHandler.install(this)

        // 仅做必要初始化，避免在主线程执行耗时操作
        createNotificationChannels()
        configureLeakCanary()
        // Task 32.4: 初始化自动备份调度
        appScope.launch {
            autoBackupScheduler.scheduleIfNeeded()
        }
        // Task 38.3/38.4: 从设置读取日志与崩溃收集开关，应用到工具类
        appScope.launch {
            applyLoggingAndCrashSettings()
        }
        Logger.i(tag = TAG, msg = "TimeMarkApp onCreate")
    }

    /**
     * Task 38.3/38.4: 从 DataStore 读取日志与崩溃收集设置并应用
     *
     * - 日志开关：控制 Logger 是否写入文件
     * - 日志级别：控制 Logger 写入文件的最低级别
     * - 崩溃收集开关：控制 CrashHandler 是否捕获崩溃
     */
    private suspend fun applyLoggingAndCrashSettings() {
        runCatching {
            // 日志开关
            val loggingEnabled = settingsDataStore.loggingEnabled.first()
            Logger.setLoggingEnabled(loggingEnabled)

            // 日志级别
            val logLevelStr = settingsDataStore.logLevel.first()
            val logLevel = when (logLevelStr) {
                "VERBOSE" -> Logger.LogLevel.VERBOSE
                "DEBUG" -> Logger.LogLevel.DEBUG
                "INFO" -> Logger.LogLevel.INFO
                "WARN" -> Logger.LogLevel.WARN
                "ERROR" -> Logger.LogLevel.ERROR
                else -> Logger.LogLevel.DEBUG
            }
            Logger.setLogLevel(logLevel)

            // 崩溃收集开关
            val crashEnabled = settingsDataStore.crashReportEnabled.first()
            crashHandler?.setEnabled(crashEnabled)

            Logger.d(tag = TAG, msg = "日志与崩溃设置已应用：logging=$loggingEnabled, level=$logLevelStr, crash=$crashEnabled")
        }
    }

    /**
     * 创建通知渠道
     * - tracker_reminder：打卡提醒，高重要性（弹出横幅、声音、振动）
     * - ai_task：AI 任务，低重要性（静默通知）
     */
    private fun createNotificationChannels() {
        // Android 8.0 (API 26) 起必须创建通知渠道
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 打卡提醒渠道
        val reminderChannel = NotificationChannel(
            CHANNEL_TRACKER_REMINDER,
            "打卡提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "打卡任务到期提醒通知"
            enableVibration(true)
        }
        manager.createNotificationChannel(reminderChannel)

        // AI 任务渠道
        val aiChannel = NotificationChannel(
            CHANNEL_AI_TASK,
            "AI 任务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "AI 任务进度与结果通知"
        }
        manager.createNotificationChannel(aiChannel)
    }

    /**
     * 配置 LeakCanary（仅在 debug 构建生效）
     *
     * LeakCanary 依赖以 debugImplementation 引入，Release 包不含此类。
     * 使用反射访问 LeakCanary API，避免在 Release 编译时找不到类。
     * - 自定义通知渠道：复用打卡提醒渠道（高重要性），便于在通知栏查看泄漏信息
     * - 保留默认的 Activity/Fragment/ViewModel 监控
     */
    private fun configureLeakCanary() {
        try {
            // 反射获取 leakcanary.LeakCanary 单例
            val leakCanaryClass = Class.forName("leakcanary.LeakCanary")
            val configField = leakCanaryClass.getDeclaredField("config")
            val config = configField.get(null) // 静态字段，传 null

            // 反射调用 config.copy(notificationChannel = CHANNEL_TRACKER_REMINDER)
            val copyMethod = config.javaClass.getMethod(
                "copy",
                String::class.java
            )
            val newConfig = copyMethod.invoke(config, CHANNEL_TRACKER_REMINDER)
            configField.set(null, newConfig)

            Logger.d(tag = TAG, msg = "LeakCanary 已配置（通知渠道：$CHANNEL_TRACKER_REMINDER）")
        } catch (e: ClassNotFoundException) {
            // Release 构建中 LeakCanary 类不存在，正常情况
            Logger.d(tag = TAG, msg = "LeakCanary 未启用（Release 构建）")
        } catch (e: Throwable) {
            // 反射调用失败，LeakCanary 将使用默认配置
            Logger.w(tag = TAG, msg = "LeakCanary 配置失败，使用默认配置：${e.message}")
        }
    }

    /**
     * WorkManager 配置
     * 使用 HiltWorkerFactory 支持依赖注入的 Worker（如 AutoBackupWorker）
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    companion object {
        private const val TAG = "TimeMarkApp"

        /** 打卡提醒通知渠道 ID */
        const val CHANNEL_TRACKER_REMINDER = "tracker_reminder"

        /** AI 任务通知渠道 ID */
        const val CHANNEL_AI_TASK = "ai_task"
    }
}
