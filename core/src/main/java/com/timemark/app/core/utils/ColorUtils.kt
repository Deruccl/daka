package com.timemark.app.core.utils

import android.graphics.Color

/**
 * 颜色工具类
 *
 * 提供 ARGB 颜色的解析、格式化、变亮/变暗、透明度调整与混合等操作。
 * 颜色值统一使用 Android 的 Int 表示（ARGB_8888）。
 */
object ColorUtils {

    /**
     * 解析十六进制颜色字符串
     * 支持 "#RRGGBB" 与 "#AARRGGBB" 两种格式（前缀 # 可选）
     * @return ARGB 颜色 Int；无法解析时抛出 IllegalArgumentException
     */
    fun parseColor(hex: String): Int {
        val normalized = hex.removePrefix("#")
        return when (normalized.length) {
            6 -> Color.parseColor("#FF$normalized") // 不透明
            8 -> Color.parseColor("#$normalized")
            else -> throw IllegalArgumentException("Invalid color hex: $hex")
        }
    }

    /**
     * 将颜色 Int 转为十六进制字符串
     * @param includeAlpha 是否包含 Alpha 通道
     * @return "#RRGGBB" 或 "#AARRGGBB"
     */
    fun toHexString(color: Int, includeAlpha: Boolean = false): String {
        return if (includeAlpha) {
            String.format("#%08X", color)
        } else {
            String.format("#%06X", color and 0x00FFFFFF)
        }
    }

    /**
     * 将颜色变亮
     * @param factor 0..1，0 表示不变，1 表示完全变白
     */
    fun lighten(color: Int, factor: Float): Int {
        val f = factor.coerceIn(0f, 1f)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(
            Color.alpha(color),
            (r + (255 - r) * f).toInt().coerceIn(0, 255),
            (g + (255 - g) * f).toInt().coerceIn(0, 255),
            (b + (255 - b) * f).toInt().coerceIn(0, 255)
        )
    }

    /**
     * 将颜色变暗
     * @param factor 0..1，0 表示不变，1 表示完全变黑
     */
    fun darken(color: Int, factor: Float): Int {
        val f = factor.coerceIn(0f, 1f)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(
            Color.alpha(color),
            (r * (1 - f)).toInt().coerceIn(0, 255),
            (g * (1 - f)).toInt().coerceIn(0, 255),
            (b * (1 - f)).toInt().coerceIn(0, 255)
        )
    }

    /**
     * 调整颜色透明度
     * @param alpha 0..1，0 完全透明，1 完全不透明
     */
    fun withAlpha(color: Int, alpha: Float): Int {
        val a = (alpha.coerceIn(0f, 1f) * 255).toInt().coerceIn(0, 255)
        return (color and 0x00FFFFFF) or (a shl 24)
    }

    /**
     * 混合两个颜色
     * @param ratio color1 占比，0..1，0 返回 color2，1 返回 color1
     */
    fun blend(color1: Int, color2: Int, ratio: Float): Int {
        val r = ratio.coerceIn(0f, 1f)
        val a1 = Color.alpha(color1)
        val a2 = Color.alpha(color2)
        return Color.argb(
            (a1 * r + a2 * (1 - r)).toInt().coerceIn(0, 255),
            (Color.red(color1) * r + Color.red(color2) * (1 - r)).toInt().coerceIn(0, 255),
            (Color.green(color1) * r + Color.green(color2) * (1 - r)).toInt().coerceIn(0, 255),
            (Color.blue(color1) * r + Color.blue(color2) * (1 - r)).toInt().coerceIn(0, 255)
        )
    }
}
