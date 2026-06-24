package com.timemark.app.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
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
 * @param darkTheme 是否使用深色主题，默认跟随系统
 * @param dynamicColor 是否启用动态颜色（仅 API 31+ 生效）
 * @param content 主题包裹的内容
 */
@Composable
fun TimeMarkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
