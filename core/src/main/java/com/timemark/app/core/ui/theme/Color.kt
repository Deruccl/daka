package com.timemark.app.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 色彩系统
 *
 * 定义 TimeMark 应用的完整调色板：
 * - 主色（靛蓝）/ 次色（粉红）/ 第三色（翠绿）
 * - 语义色（成功/警告/错误/信息）
 * - 中性色（浅色与深色模式）
 * - 渐变色（主色渐变、成功渐变、玻璃渐变）
 * - 浅色与深色 ColorScheme
 */

// region 主色 - 靛蓝色 #6366F1
val Primary = Color(0xFF6366F1)
val PrimaryLight = Color(0xFF818CF8)
val PrimaryDark = Color(0xFF4F46E5)
val PrimaryContainer = Color(0xFFEEF2FF)
// endregion

// region 次色 - 粉红色 #EC4899
val Secondary = Color(0xFFEC4899)
val SecondaryLight = Color(0xFFF472B6)
val SecondaryDark = Color(0xFFDB2777)
// endregion

// region 第三色 - 翠绿色 #10B981
val Tertiary = Color(0xFF10B981)
val TertiaryLight = Color(0xFF34D399)
val TertiaryDark = Color(0xFF059669)
// endregion

// region 语义色
val Success = Color(0xFF10B981)
val Warning = Color(0xFFF59E0B)
val Error = Color(0xFFEF4444)
val Info = Color(0xFF3B82F6)
// endregion

// region 中性色 - 浅色模式
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xE60F172A)  // 90% 不透明度
val LightOnSurface = Color(0xE60F172A)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightOutline = Color(0xFF94A3B8)
// endregion

// region 中性色 - 深色模式
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkOnPrimary = Color(0xFFFFFFFF)
val DarkOnSecondary = Color(0xFFFFFFFF)
val DarkOnBackground = Color(0xE6F8FAFC)
val DarkOnSurface = Color(0xE6F8FAFC)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF64748B)
// endregion

// region 渐变色
val PrimaryGradient = listOf(Primary, Secondary)
val SuccessGradient = listOf(Tertiary, TertiaryLight)
val GlassGradientLight = listOf(Color(0xCCFFFFFF), Color(0x66FFFFFF))  // 80% -> 40%
val GlassGradientDark = listOf(Color(0xCC1E293B), Color(0x661E293B))
// endregion

// region 浅色 ColorScheme
val LightColorScheme = lightColorScheme(
    primary = Primary, onPrimary = LightOnPrimary,
    primaryContainer = PrimaryContainer, onPrimaryContainer = PrimaryDark,
    secondary = Secondary, onSecondary = LightOnSecondary,
    secondaryContainer = SecondaryLight, onSecondaryContainer = SecondaryDark,
    tertiary = Tertiary, onTertiary = LightOnPrimary,
    tertiaryContainer = TertiaryLight, onTertiaryContainer = TertiaryDark,
    background = LightBackground, onBackground = LightOnBackground,
    surface = LightSurface, onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFF1F5F9), onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline, outlineVariant = Color(0xFFE2E8F0),
    error = Error, onError = Color(0xFFFFFFFF)
)
// endregion

// region 深色 ColorScheme
val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight, onPrimary = DarkOnPrimary,
    primaryContainer = PrimaryDark, onPrimaryContainer = PrimaryContainer,
    secondary = SecondaryLight, onSecondary = DarkOnSecondary,
    secondaryContainer = SecondaryDark, onSecondaryContainer = SecondaryLight,
    tertiary = TertiaryLight, onTertiary = DarkOnPrimary,
    tertiaryContainer = TertiaryDark, onTertiaryContainer = TertiaryLight,
    background = DarkBackground, onBackground = DarkOnBackground,
    surface = DarkSurface, onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF334155), onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline, outlineVariant = Color(0xFF334155),
    error = Error, onError = Color(0xFFFFFFFF)
)
// endregion
