package com.timemark.app.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

/**
 * 应用主题入口
 *
 * 支持三种主题模式：
 * - 浅色模式：使用 LightColorScheme
 * - 深色模式：使用 DarkColorScheme
 * - 跟随系统：根据 isSystemInDarkTheme 自动切换
 *
 * Android 12+（API 31+）支持动态颜色（Material You），
 * 可根据用户壁纸提取色彩，启用 dynamicColor 后优先使用动态配色。
 *
 * Task 37.2：支持高对比度模式与字体缩放，通过 CompositionLocal 向下传递。
 *
 * Task 38.1：集成自定义字体（Noto Sans SC / Inter / JetBrains Mono），
 * 通过 [rememberAppFontFamilies] 运行时查找字体资源并注入 Typography。
 *
 * Task 38.2：主题切换时使用 [animateColorScheme] 为 ColorScheme 的所有颜色
 * 应用 600ms EaseInOutCubic 平滑过渡动画，避免突兀的颜色跳变。
 *
 * @param darkTheme 是否使用深色主题，默认跟随系统
 * @param dynamicColor 是否启用动态颜色（仅 API 31+ 生效）
 * @param highContrast 是否启用高对比度模式，默认关闭
 * @param fontScale 字体缩放比例（1.0 / 1.15 / 1.3），默认 1.0
 * @param content 主题包裹的内容
 */
@Composable
fun TimeMarkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    highContrast: Boolean = false,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Task 37.2: 应用高对比度颜色方案
    val targetColorScheme = applyHighContrast(baseColorScheme, highContrast)

    // Task 38.2: 主题切换时为 ColorScheme 所有颜色应用 600ms 平滑过渡动画
    val colorScheme = animateColorScheme(targetColorScheme)

    // Task 38.1: 运行时查找字体资源，构建字体族（支持降级）
    val fontFamilies = rememberAppFontFamilies()

    // Task 37.2 + 38.1: 根据字体缩放生成自适应排版，并注入自定义字体
    val typography = adaptiveTypography(
        scale = fontScale,
        appFontFamily = fontFamilies.app,
        monoFontFamily = fontFamilies.mono
    )

    // 通过 CompositionLocal 向下传递高对比度状态与字体缩放
    CompositionLocalProvider(
        LocalHighContrastMode provides highContrast,
        LocalFontScale provides fontScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = Shapes,
            content = content
        )
    }
}
