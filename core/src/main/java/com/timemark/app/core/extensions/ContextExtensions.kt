package com.timemark.app.core.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.core.content.ContextCompat

/**
 * Context 相关扩展函数
 *
 * 提供尺寸转换、屏幕信息获取、深色模式判断、权限检查等便捷方法。
 */

/** dp 转 px（Float） */
fun Context.dpToPx(dp: Float): Float = dp * resources.displayMetrics.density

/** dp 转 px（Int） */
fun Context.dpToPxInt(dp: Float): Int = (dp * resources.displayMetrics.density + 0.5f).toInt()

/** px 转 dp */
fun Context.pxToDp(px: Float): Float = px / resources.displayMetrics.density

/** sp 转 px */
fun Context.spToPx(sp: Float): Float = sp * resources.displayMetrics.scaledDensity

/** 屏幕宽度（px） */
fun Context.screenWidth(): Int = resources.displayMetrics.widthPixels

/** 屏幕高度（px） */
fun Context.screenHeight(): Int = resources.displayMetrics.heightPixels

/**
 * 判断当前是否为深色模式
 * 依据系统 UI 模式（night）判断
 */
fun Context.isDarkMode(): Boolean {
    val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mode == Configuration.UI_MODE_NIGHT_YES
}

/**
 * 检查是否已授予指定权限
 * @param permission 权限名，如 Manifest.permission.CAMERA
 */
fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
