package com.timemark.app.core.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Task 37.4: 窗口尺寸分级
 *
 * 基于 Material 3 窗口大小类，将设备屏幕分为三档：
 * - COMPACT：手机竖屏（宽度 < 600dp）
 * - MEDIUM：小平板/手机横屏（600dp <= 宽度 < 840dp）
 * - EXPANDED：平板/大屏设备（宽度 >= 840dp）
 */
enum class WindowSizeClass {
    /** 紧凑：手机竖屏 */
    COMPACT,
    /** 中等：小平板/手机横屏 */
    MEDIUM,
    /** 展开：平板/大屏设备 */
    EXPANDED
}

/**
 * 根据当前窗口宽度判断尺寸分级
 *
 * - 宽度 < 600dp -> COMPACT
 * - 宽度 < 840dp -> MEDIUM
 * - 宽度 >= 840dp -> EXPANDED
 */
@Composable
fun windowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.dp
    return remember(widthDp) {
        when {
            widthDp < 600.dp -> WindowSizeClass.COMPACT
            widthDp < 840.dp -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }
    }
}

/**
 * 判断当前是否为平板设备（MEDIUM 或 EXPANDED）
 */
@Composable
fun isTablet(): Boolean {
    return windowSizeClass() != WindowSizeClass.COMPACT
}

/**
 * 判断当前是否为横屏
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
