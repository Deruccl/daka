package com.timemark.app.reminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.timemark.app.R
import com.timemark.app.TimeMarkApp

/**
 * 间隔提醒 Worker
 *
 * 由 [ReminderScheduler.scheduleIntervalReminder] 通过 WorkManager 调度。
 * 每隔指定小时数触发一次，显示打卡提醒通知。
 *
 * 通知包含"已完成"和"稍后提醒"快捷操作按钮，
 * 与 [ReminderReceiver] 显示的通知格式一致。
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val trackerId = inputData.getLong(ReminderScheduler.EXTRA_TRACKER_ID, 0)
        val trackerName = inputData.getString(ReminderScheduler.EXTRA_TRACKER_NAME) ?: "打卡"

        showNotification(applicationContext, trackerId, trackerName)
        return Result.success()
    }

    /**
     * 显示提醒通知
     * 通知包含两个快捷操作按钮：已完成、稍后提醒。
     */
    private fun showNotification(context: Context, trackerId: Long, trackerName: String) {
        val notificationManager = NotificationManagerCompat.from(context)

        val notification = NotificationCompat.Builder(context, TimeMarkApp.CHANNEL_TRACKER_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("打卡提醒")
            .setContentText("该完成 $trackerName 了")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            // 快捷操作：已完成
            .addAction(0, "已完成", createCompleteIntent(context, trackerId, trackerName))
            // 快捷操作：稍后提醒
            .addAction(0, "稍后提醒", createSnoozeIntent(context, trackerId, trackerName))
            .build()

        try {
            notificationManager.notify(trackerId.toInt(), notification)
        } catch (e: SecurityException) {
            // 通知权限被拒绝时忽略
        }
    }

    /**
     * 创建"已完成"操作的 PendingIntent
     */
    private fun createCompleteIntent(
        context: Context,
        trackerId: Long,
        trackerName: String
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_COMPLETE
            putExtra(ReminderScheduler.EXTRA_TRACKER_ID, trackerId)
            putExtra(ReminderScheduler.EXTRA_TRACKER_NAME, trackerName)
        }
        return PendingIntent.getBroadcast(
            context,
            (trackerId * 10 + COMPLETE_REQUEST_OFFSET).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * 创建"稍后提醒"操作的 PendingIntent
     */
    private fun createSnoozeIntent(
        context: Context,
        trackerId: Long,
        trackerName: String
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_SNOOZE
            putExtra(ReminderScheduler.EXTRA_TRACKER_ID, trackerId)
            putExtra(ReminderScheduler.EXTRA_TRACKER_NAME, trackerName)
        }
        return PendingIntent.getBroadcast(
            context,
            (trackerId * 10 + SNOOZE_REQUEST_OFFSET).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val COMPLETE_REQUEST_OFFSET = 100
        private const val SNOOZE_REQUEST_OFFSET = 200
    }
}
