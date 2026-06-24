package com.timemark.app.core.ui.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

/**
 * Task 37.3: TalkBack 屏幕阅读器支持
 *
 * 提供动态内容播报能力，辅助视障用户感知 UI 变化。
 *
 * - [announceOnChange]：值变化时自动播报
 * - [Modifier.liveRegion]：动态区域，内容变化时 TalkBack 自动播报
 * - [announceForAccessibility]：手动触发播报
 */

/**
 * 值变化时播报
 *
 * 当 [value] 变化时，通过 TalkBack 播报新值。
 * 适用于进度变化、状态切换等场景。
 *
 * @param value 监听的值（变化时触发播报）
 * @param announcement 播报内容，为 null 时不播报
 */
@Composable
fun announceOnChange(value: Any?, announcement: String?) {
    val context = LocalContext.current
    if (announcement != null) {
        LaunchedEffect(value) {
            // 使用 AccessibilityEvent 触发 TalkBack 播报
            context.announceForAccessibilityCompat(announcement)
        }
    }
}

/**
 * 动态区域修饰符
 *
 * 标记该元素为动态区域，内容变化时 TalkBack 会自动播报。
 * - [LiveRegionMode.Polite]：温和播报，不打断当前语音
 * - [LiveRegionMode.Assertive]：立即播报，可能打断当前语音
 *
 * @param politeness 播报模式，默认 Polite
 */
fun Modifier.liveRegion(politeness: LiveRegionMode = LiveRegionMode.Polite): Modifier =
    this.semantics { liveRegion = politeness }

/**
 * 手动触发无障碍播报
 *
 * 在 Composable 中调用，向 TalkBack 发送播报消息。
 *
 * @param message 播报内容
 */
@Composable
fun announceForAccessibility(message: String) {
    val context = LocalContext.current
    LaunchedEffect(message) {
        context.announceForAccessibilityCompat(message)
    }
}

/**
 * 兼容性播报：使用 AccessibilityManager 发送 TYPE_ANNOUNCEMENT 事件。
 * 兼容低版本 Android（API 26+）。
 */
private fun android.content.Context.announceForAccessibilityCompat(text: String) {
    val manager = getSystemService(android.content.Context.ACCESSIBILITY_SERVICE)
        as? android.view.accessibility.AccessibilityManager
    if (manager?.isEnabled != true) return
    val event = android.view.accessibility.AccessibilityEvent.obtain(
        android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
    )
    event.className = "AccessibilityHelper"
    event.packageName = packageName
    event.text.add(text)
    manager.sendAccessibilityEvent(event)
}
