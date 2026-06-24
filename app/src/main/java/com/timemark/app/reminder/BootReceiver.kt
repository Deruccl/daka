package com.timemark.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.timemark.app.core.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 开机自启广播接收器
 *
 * 设备重启后，AlarmManager 中注册的所有闹钟会被清除。
 * 此接收器在 BOOT_COMPLETED 广播时重新调度所有打卡项的提醒。
 *
 * 使用 @AndroidEntryPoint 注入 [reminderScheduler]，
 * 通过 goAsync() 保持接收器存活直到重新调度完成。
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) {
            return
        }

        Logger.i(tag = TAG, msg = "收到开机广播，开始重新调度提醒")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reminderScheduler.rescheduleAll()
                Logger.i(tag = TAG, msg = "提醒重新调度完成")
            } catch (e: Exception) {
                Logger.e(tag = TAG, msg = "提醒重新调度失败: ${e.message}", tr = e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
