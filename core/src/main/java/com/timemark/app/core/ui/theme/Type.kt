package com.timemark.app.core.ui.theme

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// region Task 38.1: 字体资源名称常量
/** Noto Sans SC 字体资源名（对应 res/font/ 下的 ttf 文件名，不含扩展名） */
private const val FONT_NOTO_SANS_SC_REGULAR = "noto_sans_sc_regular"
private const val FONT_NOTO_SANS_SC_MEDIUM = "noto_sans_sc_medium"
private const val FONT_NOTO_SANS_SC_BOLD = "noto_sans_sc_bold"

/** Inter 字体资源名 */
private const val FONT_INTER_REGULAR = "inter_regular"
private const val FONT_INTER_MEDIUM = "inter_medium"
private const val FONT_INTER_BOLD = "inter_bold"

/** JetBrains Mono 字体资源名 */
private const val FONT_JETBRAINS_MONO_REGULAR = "jetbrains_mono_regular"
private const val FONT_JETBRAINS_MONO_BOLD = "jetbrains_mono_bold"
// endregion

/**
 * Task 38.1: 安全加载字体资源
 *
 * 通过资源名运行时查找字体资源 ID。若字体文件未放入 res/font/ 目录，返回 null，
 * 由 [buildFontFamily] 统一降级为系统默认字体。
 *
 * 采用运行时查找而非直接引用 R.font.xxx，可避免字体文件缺失时编译失败，
 * 实现真正的"无字体即降级"方案。
 *
 * @param context 上下文，用于访问资源
 * @param name 字体资源名（res/font/ 下的文件名，不含扩展名）
 * @param weight 字重
 * @return 字体 Font，资源不存在时返回 null
 */
private fun safeFont(context: Context, name: String, weight: FontWeight): Font? {
    val resId = context.resources.getIdentifier(name, "font", context.packageName)
    return if (resId != 0) Font(resId, weight) else null
}

/**
 * Task 38.1: 构建字体族
 *
 * 将多个 Font 组装为 FontFamily。若全部为 null（字体文件均未提供），
 * 返回 [fallback] 指定的降级字体族。
 *
 * @param fonts 字体列表（可能含 null）
 * @param fallback 全部字体缺失时的降级字体族
 * @return 组装后的 FontFamily
 */
private fun buildFontFamily(vararg fonts: Font?, fallback: FontFamily = FontFamily.Default): FontFamily {
    val validFonts = fonts.filterNotNull()
    return if (validFonts.isEmpty()) fallback else FontFamily(fonts = validFonts)
}

/**
 * Task 38.1: 字体族集合
 *
 * 封装应用所需的全部字体族，便于在主题中统一应用。
 *
 * @param app 应用正文字体族（中文优先 Noto Sans SC，降级 Inter，再降级系统默认）
 * @param mono 等宽字体族（JetBrains Mono，降级系统等宽）
 * @param notoSansSC Noto Sans SC 字体族（中文）
 * @param inter Inter 字体族（英文/数字）
 */
data class AppFontFamilies(
    val app: FontFamily,
    val mono: FontFamily,
    val notoSansSC: FontFamily,
    val inter: FontFamily
)

/**
 * Task 38.1: 记住应用字体族
 *
 * 在 Composable 上下文中根据当前 Context 查找字体资源，构建字体族集合。
 * 结果通过 remember 缓存，避免重复查找。
 *
 * 降级策略：
 * 1. Noto Sans SC 字体族：未提供时降级为 FontFamily.Default
 * 2. Inter 字体族：未提供时降级为 FontFamily.Default
 * 3. JetBrains Mono 字体族：未提供时降级为 FontFamily.Monospace
 * 4. 应用正文字体族（app）：优先使用 Noto Sans SC（覆盖中英文），
 *    若不可用则使用 Inter（英文为主，中文由系统回退），再降级为系统默认
 *
 * @return 字体族集合
 */
@Composable
fun rememberAppFontFamilies(): AppFontFamilies {
    val context = LocalContext.current
    return remember(context) {
        // Noto Sans SC 字体族（中文）
        val notoSansSC = buildFontFamily(
            safeFont(context, FONT_NOTO_SANS_SC_REGULAR, FontWeight.Normal),
            safeFont(context, FONT_NOTO_SANS_SC_MEDIUM, FontWeight.Medium),
            safeFont(context, FONT_NOTO_SANS_SC_BOLD, FontWeight.Bold),
            fallback = FontFamily.Default
        )
        // Inter 字体族（英文/数字）
        val inter = buildFontFamily(
            safeFont(context, FONT_INTER_REGULAR, FontWeight.Normal),
            safeFont(context, FONT_INTER_MEDIUM, FontWeight.Medium),
            safeFont(context, FONT_INTER_BOLD, FontWeight.Bold),
            fallback = FontFamily.Default
        )
        // JetBrains Mono 字体族（等宽，用于数据展示）
        val mono = buildFontFamily(
            safeFont(context, FONT_JETBRAINS_MONO_REGULAR, FontWeight.Normal),
            safeFont(context, FONT_JETBRAINS_MONO_BOLD, FontWeight.Bold),
            fallback = FontFamily.Monospace
        )
        // 应用正文字体族：中文用 Noto Sans SC，英文/数字用 Inter
        // Noto Sans SC 本身覆盖中英文，优先使用；不可用时降级到 Inter（中文由系统回退）
        val app = when {
            notoSansSC != FontFamily.Default -> notoSansSC
            inter != FontFamily.Default -> inter
            else -> FontFamily.Default
        }
        AppFontFamilies(app = app, mono = mono, notoSansSC = notoSansSC, inter = inter)
    }
}

/**
 * Task 38.1: Noto Sans SC 字体族（Composable，支持降级）
 *
 * 引用 R.font.noto_sans_sc_* 资源。若字体文件未提供，降级为 FontFamily.Default。
 */
@Composable
fun rememberNotoSansSCFontFamily(): FontFamily = rememberAppFontFamilies().notoSansSC

/**
 * Task 38.1: Inter 字体族（Composable，支持降级）
 *
 * 引用 R.font.inter_* 资源。若字体文件未提供，降级为 FontFamily.Default。
 */
@Composable
fun rememberInterFontFamily(): FontFamily = rememberAppFontFamilies().inter

/**
 * Task 38.1: JetBrains Mono 字体族（Composable，支持降级）
 *
 * 引用 R.font.jetbrains_mono_* 资源。若字体文件未提供，降级为 FontFamily.Monospace。
 * 用于数据展示（统计数字、代码等）。
 */
@Composable
fun rememberJetBrainsMonoFontFamily(): FontFamily = rememberAppFontFamilies().mono

/**
 * 字体层级系统
 *
 * 定义 11 级 Material 3 字体样式：
 * - Display：大号展示文字（标题页/数字）
 * - Headline：标题文字
 * - Title：卡片/区块标题
 * - Body：正文
 * - Label：标签/按钮文字
 *
 * 字号从 57sp 到 11sp，行高均为字号的 1.4 倍左右，保证可读性。
 *
 * Task 38.1: 此 Typography 使用系统默认字体（FontFamily.Default）。
 * 实际应用字体通过 [adaptiveTypography] 在主题中注入。
 */
val Typography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Bold, lineHeight = 64.sp),
    displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Bold, lineHeight = 52.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp)
)

/**
 * Task 37.2: 自适应排版
 *
 * 根据字体缩放比例生成对应的 Typography，支持三档：
 * - 1.0x：标准（默认）
 * - 1.15x：大号
 * - 1.3x：超大号（同时增大行高，避免文字拥挤）
 *
 * 超大字体（>=1.3x）时行高额外增加 1.2 倍系数，保证多行文字可读性。
 *
 * Task 38.1: 新增 [appFontFamily] 与 [monoFontFamily] 参数，将自定义字体注入 Typography。
 * - appFontFamily 应用于全部样式（Display/Headline/Title/Body/Label）
 * - monoFontFamily 应用于 DisplayLarge/DisplayMedium（大号数字展示，如统计页）
 *
 * @param scale 字体缩放比例，范围 1.0-1.3
 * @param appFontFamily 应用正文字体族（默认 FontFamily.Default，即系统字体）
 * @param monoFontFamily 等宽字体族（默认 FontFamily.Monospace，用于数据展示）
 * @return 缩放后的 Typography
 */
fun adaptiveTypography(
    scale: Float,
    appFontFamily: FontFamily = FontFamily.Default,
    monoFontFamily: FontFamily = FontFamily.Monospace
): Typography {
    // 限制缩放范围
    val s = scale.coerceIn(1.0f, 1.3f)
    // 超大字体时行高额外增加系数
    val lineHeightFactor = if (s >= 1.3f) 1.2f else 1.0f

    return Typography(
        // Display：大号数字展示使用等宽字体（统计数字、计时器等）
        displayLarge = TextStyle(
            fontSize = (57 * s).sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (64 * s * lineHeightFactor).sp,
            fontFamily = monoFontFamily
        ),
        displayMedium = TextStyle(
            fontSize = (45 * s).sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (52 * s * lineHeightFactor).sp,
            fontFamily = monoFontFamily
        ),
        headlineLarge = TextStyle(
            fontSize = (32 * s).sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (40 * s * lineHeightFactor).sp,
            fontFamily = appFontFamily
        ),
        headlineMedium = TextStyle(
            fontSize = (28 * s).sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (36 * s * lineHeightFactor).sp,
            fontFamily = appFontFamily
        ),
        titleLarge = TextStyle(
            fontSize = (22 * s).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (28 * s * lineHeightFactor).sp,
            fontFamily = appFontFamily
        ),
        titleMedium = TextStyle(
            fontSize = (16 * s).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (24 * s * lineHeightFactor).sp,
            fontFamily = appFontFamily
        ),
        bodyLarge = TextStyle(
            fontSize = (16 * s).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (24 * s * lineHeightFactor).sp,
            fontFamily = appFontFamily
        ),
        bodyMedium = TextStyle(
            fontSize = (14 * s).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (20 * s * lineHeightFactor).sp,
            fontFamily = appFontFamily
        ),
        labelLarge = TextStyle(
            fontSize = (14 * s).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (20 * s * lineHeightFactor).sp,
            fontFamily = appFontFamily
        ),
        labelMedium = TextStyle(
            fontSize = (12 * s).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (16 * s * lineHeightFactor).sp,
            fontFamily = appFontFamily
        ),
        labelSmall = TextStyle(
            fontSize = (11 * s).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (16 * s * lineHeightFactor).sp,
            fontFamily = appFontFamily
        )
    )
}

/**
 * Task 38.1: 等宽数据展示 TextStyle
 *
 * 供统计页、代码展示等需要等宽字体的场景使用。
 * 使用 JetBrains Mono 字体族（降级为系统等宽）。
 *
 * @param monoFontFamily 等宽字体族
 * @param fontSize 字号（sp）
 * @param fontWeight 字重
 * @return 等宽 TextStyle
 */
fun monoTextStyle(
    monoFontFamily: FontFamily = FontFamily.Monospace,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal
): TextStyle = TextStyle(
    fontFamily = monoFontFamily,
    fontSize = fontSize,
    fontWeight = fontWeight
)
