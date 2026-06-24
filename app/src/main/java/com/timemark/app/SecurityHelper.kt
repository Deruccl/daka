package com.timemark.app

import android.view.WindowManager
import androidx.fragment.app.FragmentActivity

/**
 * 安全屏幕辅助工具（Task 32.2）
 *
 * 提供安全相关的 Activity 窗口标志操作：
 * - applySecureFlag：应用/移除 FLAG_SECURE，禁止截图与最近任务列表内容预览
 * - hideContentFromRecents：隐藏最近任务列表中的应用内容预览
 *
 * FLAG_SECURE 的作用：
 * 1. 阻止屏幕截图
 * 2. 在最近任务列表中显示空白缩略图
 * 3. 阻止内容被投射到外部显示器
 */
object SecurityHelper {

    /**
     * 应用或移除 FLAG_SECURE。
     *
     * @param activity 目标 Activity
     * @param enabled true 表示启用安全标志（禁止截图），false 表示移除
     */
    fun applySecureFlag(activity: FragmentActivity, enabled: Boolean) {
        if (enabled) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    /**
     * 隐藏最近任务列表中的应用内容预览。
     * 等同于设置 FLAG_SECURE，使最近任务缩略图显示为空白。
     *
     * @param activity 目标 Activity
     */
    fun hideContentFromRecents(activity: FragmentActivity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    /**
     * 检查当前是否已启用 FLAG_SECURE。
     */
    fun isSecureFlagEnabled(activity: FragmentActivity): Boolean {
        val flags = activity.window.attributes.flags
        return (flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
    }
}
