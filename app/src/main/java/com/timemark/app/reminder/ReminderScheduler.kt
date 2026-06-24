package com.timemark.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.timemark.app.domain.model.ReminderFrequency
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.repository.TrackerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 提醒调度器
 *
 * 负责为打卡项调度提醒：
 * - DAILY：使用 AlarmManager 设置每日定时闹钟
 * - WEEKLY：为每周指定日分别设置闹钟
 * - INTERVAL：使用 WorkManager 设置周期性间隔提醒
 * - SMART：暂按每日提醒处理（后续可扩展为基于历史数据的智能提醒）
 *
 * 闹钟触发后由 [ReminderReceiver] 接收并显示通知。
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackerRepository: TrackerRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * 为指定打卡项调度提醒
     * 若 tracker 未启用提醒（reminderEnabled = false），则不做任何操作。
     */
    suspend fun scheduleReminder(tracker: Tracker) {
        if (!tracker.reminderEnabled) return

        when (tracker.reminderFrequency) {
            ReminderFrequency.DAILY -> scheduleDailyReminder(tracker)
            ReminderFrequency.WEEKLY -> scheduleWeeklyReminder(tracker)
            ReminderFrequency.INTERVAL -> scheduleIntervalReminder(tracker)
            ReminderFrequency.SMART -> scheduleSmartReminder(tracker)
        }
    }

    /**
     * 取消指定打卡项的所有提醒
     * 包括 AlarmManager 闹钟与 WorkManager 任务。
     */
    fun cancelReminder(trackerId: Long) {
        // 取消 AlarmManager 闹钟（每日 + 每周各天）
        val days = listOf(1, 2, 3, 4, 5, 6, 7) // 周一至周日
        days.forEach { day ->
            val pendingIntent = createPendingIntent(
                trackerId = trackerId,
                trackerName = "",
                dayOfWeek = day,
                action = ACTION_REMINDER
            )
            alarmManager.cancel(pendingIntent)
        }
        // 也取消无 dayOfWeek 的每日闹钟
        val dailyPendingIntent = createPendingIntent(
            trackerId = trackerId,
            trackerName = "",
            dayOfWeek = -1,
            action = ACTION_REMINDER
        )
        alarmManager.cancel(dailyPendingIntent)

        // 取消 WorkManager 间隔提醒
        cancelIntervalWork(trackerId)
    }

    /**
     * 重新调度所有打卡项的提醒
     * 在设备重启后由 [BootReceiver] 调用。
     */
    suspend fun rescheduleAll() {
        val trackers = trackerRepository.getAllTrackers().first()
        trackers.forEach { tracker ->
            if (tracker.reminderEnabled) {
                scheduleReminder(tracker)
            }
        }
    }

    /**
     * 调度每日提醒
     * 使用 setExactAndAllowWhileIdle 确保在 Doze 模式下也能触发。
     */
    private fun scheduleDailyReminder(tracker: Tracker) {
        val reminderTime = tracker.reminderTime ?: "09:00"
        val triggerTime = calculateNextTriggerTime(reminderTime, dayOfWeek = null)
        val pendingIntent = createPendingIntent(
            trackerId = tracker.id,
            trackerName = tracker.name,
            dayOfWeek = -1,
            action = ACTION_REMINDER,
            reminderTime = reminderTime
        )
        setExactAlarm(triggerTime, pendingIntent)
    }

    /**
     * 调度每周提醒
     * 为 reminderDays 中指定的每一天分别设置闹钟。
     */
    private fun scheduleWeeklyReminder(tracker: Tracker) {
        val reminderTime = tracker.reminderTime ?: "09:00"
        val days = tracker.reminderDays.ifEmpty { listOf(1, 2, 3, 4, 5, 6, 7) }
        days.forEach { day ->
            val triggerTime = calculateNextTriggerTime(reminderTime, dayOfWeek = day)
            val pendingIntent = createPendingIntent(
                trackerId = tracker.id,
                trackerName = tracker.name,
                dayOfWeek = day,
                action = ACTION_REMINDER,
                reminderTime = reminderTime
            )
            setExactAlarm(triggerTime, pendingIntent)
        }
    }

    /**
     * 调度间隔提醒
     * 使用 WorkManager PeriodicWorkRequest 实现周期性提醒。
     */
    private fun scheduleIntervalReminder(tracker: Tracker) {
        val intervalHours = tracker.reminderIntervalHours.coerceAtLeast(1)
        val constraints = androidx.work.Constraints.Builder()
            .build()
        val inputData = androidx.work.workDataOf(
            "tracker_id" to tracker.id,
            "tracker_name" to tracker.name
        )
        val request = androidx.work.PeriodicWorkRequestBuilder<ReminderWorker>(
            intervalHours.toLong(),
            java.util.concurrent.TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            getWorkName(tracker.id),
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * 调度智能提醒
     * 当前按每日提醒处理，后续可扩展为基于历史打卡数据的智能时间推荐。
     */
    private fun scheduleSmartReminder(tracker: Tracker) {
        scheduleDailyReminder(tracker)
    }

    /**
     * 计算下一次触发时间
     * @param reminderTime 提醒时间 "HH:mm"
     * @param dayOfWeek 周几（1=周一, 7=周日），null 表示每天
     * @return 触发时间戳（毫秒）
     */
    private fun calculateNextTriggerTime(reminderTime: String, dayOfWeek: Int?): Long {
        val parts = reminderTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (dayOfWeek != null) {
                // 转换：1=周一 -> Calendar.MONDAY=2
                val calendarDayOfWeek = if (dayOfWeek == 7) Calendar.SUNDAY else dayOfWeek + 1
                set(Calendar.DAY_OF_WEEK, calendarDayOfWeek)
                // 如果本周该日已过，跳到下周
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            } else {
                // 每日提醒：如果今天的时间已过，跳到明天
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }
        return calendar.timeInMillis
    }

    /**
     * 设置精确闹钟
     * Android 12+ 需要检查 canScheduleExactAlarms，不支持时降级为非精确闹钟。
     */
    private fun setExactAlarm(triggerTime: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * 取消间隔提醒的 WorkManager 任务
     */
    private fun cancelIntervalWork(trackerId: Long) {
        androidx.work.WorkManager.getInstance(context)
            .cancelUniqueWork(getWorkName(trackerId))
    }

    /**
     * 创建提醒 PendingIntent
     * @param trackerId 打卡项 ID
     * @param trackerName 打卡项名称
     * @param dayOfWeek 周几（-1 表示每日）
     * @param action 动作类型
     * @param reminderTime 提醒时间 "HH:mm"（用于接收器重新调度）
     */
    private fun createPendingIntent(
        trackerId: Long,
        trackerName: String,
        dayOfWeek: Int,
        action: String,
        reminderTime: String? = null
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_TRACKER_ID, trackerId)
            putExtra(EXTRA_TRACKER_NAME, trackerName)
            putExtra(EXTRA_DAY_OF_WEEK, dayOfWeek)
            reminderTime?.let { putExtra(EXTRA_REMINDER_TIME, it) }
        }
        // 使用 trackerId * 10 + dayOfWeek 作为请求码，确保唯一
        val requestCode = (trackerId * 10 + dayOfWeek).toInt()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_REMINDER = "com.timemark.app.action.REMINDER"
        const val ACTION_COMPLETE = "com.timemark.app.action.COMPLETE"
        const val ACTION_SNOOZE = "com.timemark.app.action.SNOOZE"

        const val EXTRA_TRACKER_ID = "tracker_id"
        const val EXTRA_TRACKER_NAME = "tracker_name"
        const val EXTRA_DAY_OF_WEEK = "day_of_week"
        const val EXTRA_REMINDER_TIME = "reminder_time"

        /** 生成 WorkManager 唯一任务名 */
        fun getWorkName(trackerId: Long): String = "reminder_interval_$trackerId"
    }
}
