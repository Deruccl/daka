package com.timemark.app.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.timemark.app.core.utils.Logger
import com.timemark.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 自动备份调度器（Task 32.4）
 *
 * 根据用户设置调度自动备份任务：
 * - 频率：每日（daily）/ 每周（weekly）/ 每月（monthly）
 * - 使用 PeriodicWorkRequest 周期性执行
 * - 使用 KEEP 策略避免重复调度
 *
 * 调度时机：
 * - 应用启动时调用 scheduleIfNeeded() 检查并更新调度
 * - 用户修改自动备份设置时调用 reschedule()
 */
@Singleton
class AutoBackupScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    /**
     * 检查设置并按需调度自动备份。
     * 如果自动备份已启用，根据频率创建/更新周期性任务；
     * 如果已禁用，取消已有任务。
     */
    suspend fun scheduleIfNeeded() {
        val enabled = settingsRepository.autoBackupEnabledV2.first()
        if (enabled) {
            val frequency = settingsRepository.autoBackupFrequencyV2.first()
            scheduleBackup(frequency)
        } else {
            cancelBackup()
        }
    }

    /**
     * 重新调度自动备份（用户修改设置后调用）。
     */
    suspend fun reschedule() {
        scheduleIfNeeded()
    }

    /**
     * 根据频率调度周期性备份任务。
     *
     * @param frequency 频率：daily/weekly/monthly
     */
    private fun scheduleBackup(frequency: String) {
        val (intervalDays, intervalUnit) = when (frequency) {
            "daily" -> 1L to TimeUnit.DAYS
            "weekly" -> 7L to TimeUnit.DAYS
            "monthly" -> 30L to TimeUnit.DAYS
            else -> 7L to TimeUnit.DAYS
        }

        // 约束条件：不需要网络（离线备份）
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            intervalDays,
            intervalUnit
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AutoBackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Logger.i(
            tag = TAG,
            msg = "自动备份已调度，频率：$frequency（每 $intervalDays 天）"
        )
    }

    /**
     * 取消自动备份任务。
     */
    private fun cancelBackup() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(AutoBackupWorker.WORK_NAME)
        Logger.i(tag = TAG, msg = "自动备份已取消")
    }

    companion object {
        private const val TAG = "AutoBackupScheduler"
    }
}
