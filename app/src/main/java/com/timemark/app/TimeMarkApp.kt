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
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // 仅做必要初始化，避免在主线程执行耗时操作
        createNotificationChannels()
        configureLeakCanary()
        // Task 32.4: 初始化自动备份调度
        appScope.launch {
            autoBackupScheduler.scheduleIfNeeded()
        }
        Logger.i(tag = TAG, msg = "TimeMarkApp onCreate")
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
