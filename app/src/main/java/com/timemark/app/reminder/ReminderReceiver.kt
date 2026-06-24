package com.timemark.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.timemark.app.R
import com.timemark.app.TimeMarkApp
import com.timemark.app.domain.model.Record
import com.timemark.app.domain.repository.RecordRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

/**
 * 打卡提醒广播接收器
 *
 * 处理三种动作：
 * - ACTION_REMINDER：显示提醒通知，并重新调度下一次提醒
 * - ACTION_COMPLETE：快速完成打卡（插入一条记录），取消通知
 * - ACTION_SNOOZE：稍后提醒（15 分钟后再次通知），取消当前通知
 *
 * 使用 @AndroidEntryPoint 启用 Hilt 注入，在 onReceive 中访问 [recordRepository]。
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var recordRepository: RecordRepository

    override fun onReceive(context: Context, intent: Intent) {
        val trackerId = intent.getLongExtra(ReminderScheduler.EXTRA_TRACKER_ID, 0)
        val trackerName = intent.getStringExtra(ReminderScheduler.EXTRA_TRACKER_NAME) ?: "打卡"
        val dayOfWeek = intent.getIntExtra(ReminderScheduler.EXTRA_DAY_OF_WEEK, -1)
        val reminderTime = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_TIME)

        when (intent.action) {
            ReminderScheduler.ACTION_REMINDER -> {
                // 显示提醒通知
                showNotification(context, trackerId, trackerName)
                // 重新调度下一次提醒（每日/每周）
                if (reminderTime != null) {
                    rescheduleNext(context, trackerId, trackerName, dayOfWeek, reminderTime)
                }
            }
            ReminderScheduler.ACTION_COMPLETE -> {
                // 快速完成打卡
                handleComplete(context, trackerId, trackerName)
            }
            ReminderScheduler.ACTION_SNOOZE -> {
                // 稍后提醒：15 分钟后再次通知
                scheduleSnooze(context, trackerId, trackerName)
                // 取消当前通知
                NotificationManagerCompat.from(context).cancel(trackerId.toInt())
            }
        }
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
     * 处理"已完成"操作
     * 插入一条打卡记录，并取消通知。
     */
    private fun handleComplete(context: Context, trackerId: Long, trackerName: String) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val date = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                val record = Record(
                    trackerId = trackerId,
                    value = 1.0,
                    date = date,
                    time = time,
                    timestamp = System.currentTimeMillis()
                )
                recordRepository.insertRecord(record)
                // 取消通知
                NotificationManagerCompat.from(context).cancel(trackerId.toInt())
            } catch (e: Exception) {
                // 忽略插入失败
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * 调度稍后提醒（15 分钟后）
     */
    private fun scheduleSnooze(context: Context, trackerId: Long, trackerName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + SNOOZE_INTERVAL_MS
        val pendingIntent = createReminderPendingIntent(context, trackerId, trackerName, -1)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
            )
        }
    }

    /**
     * 重新调度下一次提醒
     * 在闹钟触发后，为下一个周期（明天/下周）设置新的闹钟。
     */
    private fun rescheduleNext(
        context: Context,
        trackerId: Long,
        trackerName: String,
        dayOfWeek: Int,
        reminderTime: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val parts = reminderTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (dayOfWeek > 0) {
                // 每周提醒：跳到下周同一天
                val calendarDayOfWeek = if (dayOfWeek == 7) Calendar.SUNDAY else dayOfWeek + 1
                set(Calendar.DAY_OF_WEEK, calendarDayOfWeek)
                add(Calendar.WEEK_OF_YEAR, 1)
            } else {
                // 每日提醒：跳到明天
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val pendingIntent = createReminderPendingIntent(context, trackerId, trackerName, dayOfWeek)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
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

    /**
     * 创建提醒 PendingIntent（用于稍后提醒和重新调度）
     */
    private fun createReminderPendingIntent(
        context: Context,
        trackerId: Long,
        trackerName: String,
        dayOfWeek: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_REMINDER
            putExtra(ReminderScheduler.EXTRA_TRACKER_ID, trackerId)
            putExtra(ReminderScheduler.EXTRA_TRACKER_NAME, trackerName)
            putExtra(ReminderScheduler.EXTRA_DAY_OF_WEEK, dayOfWeek)
        }
        return PendingIntent.getBroadcast(
            context,
            (trackerId * 10 + dayOfWeek).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        /** 稍后提醒间隔：15 分钟 */
        private const val SNOOZE_INTERVAL_MS = 15L * 60 * 1000

        /** 请求码偏移：已完成 */
        private const val COMPLETE_REQUEST_OFFSET = 100

        /** 请求码偏移：稍后提醒 */
        private const val SNOOZE_REQUEST_OFFSET = 200
    }
}
