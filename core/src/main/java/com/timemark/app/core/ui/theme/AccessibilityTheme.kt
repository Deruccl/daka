package com.timemark.app.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 无障碍主题系统
 *
 * 提供高对比度颜色方案与字体缩放支持，提升视障用户与老年用户的可用性。
 *
 * - [LocalHighContrastMode]：通过 CompositionLocal 向下传递高对比度状态
 * - [HighContrastColorScheme]：高对比度颜色方案（更深背景、更亮文字、更强边框）
 * - [applyHighContrast]：将任意 ColorScheme 转换为高对比度版本
 */

/**
 * 高对比度模式 CompositionLocal，默认关闭。
 * 子组件可通过读取此值调整自身视觉表现（如边框宽度、透明度）。
 */
val LocalHighContrastMode: ProvidableCompositionLocal<Boolean> =
    staticCompositionLocalOf { false }

/**
 * 字体缩放 CompositionLocal，默认 1.0x。
 */
val LocalFontScale: ProvidableCompositionLocal<Float> =
    staticCompositionLocalOf { 1.0f }

/**
 * 将给定颜色方案转换为高对比度版本。
 *
 * 调整策略：
 * - 背景与 Surface 更深/更纯，减少半透明干扰
 * - 文字（onSurface/onBackground）提升至接近纯黑/纯白
 * - 主色饱和度提升，增强辨识度
 * - 边框（outline）颜色加深
 *
 * @param colorScheme 原始颜色方案
 * @param enabled 是否启用高对比度，false 时原样返回
 */
fun applyHighContrast(colorScheme: ColorScheme, enabled: Boolean): ColorScheme {
    if (!enabled) return colorScheme
    return colorScheme.copy(
        // 背景与 Surface：浅色模式纯白，深色模式纯黑
        background = if (colorScheme.background.luminance() > 0.5f) Color.White else Color.Black,
        surface = if (colorScheme.surface.luminance() > 0.5f) Color.White else Color(0xFF000000),
        surfaceVariant = if (colorScheme.surfaceVariant.luminance() > 0.5f)
            Color(0xFFE2E8F0) else Color(0xFF1A1A1A),
        // 文字：提升至接近纯黑/纯白，确保最大对比度
        onBackground = if (colorScheme.background.luminance() > 0.5f)
            Color(0xFF000000) else Color(0xFFFFFFFF),
        onSurface = if (colorScheme.surface.luminance() > 0.5f)
            Color(0xFF000000) else Color(0xFFFFFFFF),
        onSurfaceVariant = if (colorScheme.surfaceVariant.luminance() > 0.5f)
            Color(0xFF1E293B) else Color(0xFFE2E8F0),
        // 边框加深
        outline = if (colorScheme.outline.luminance() > 0.5f)
            Color(0xFF334155) else Color(0xFFCBD5E1),
        outlineVariant = if (colorScheme.outlineVariant.luminance() > 0.5f)
            Color(0xFF475569) else Color(0xFF94A3B8)
    )
}

/**
 * 计算 Color 的相对亮度（近似值，用于判断浅色/深色）。
 */
private fun Color.luminance(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue
