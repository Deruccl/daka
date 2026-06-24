package com.timemark.app.core.ui.animation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color

/**
 * Task 38.2: 主题切换动画工具集
 *
 * 提供主题切换时的平滑过渡效果：
 * - 颜色变化 600ms 过渡（EaseInOutCubic 缓动）
 * - 透明度变化 600ms 过渡
 * - Modifier.animateThemeChange() 主题切换过渡修饰符
 *
 * 设计目标：主题切换时避免突兀的颜色跳变，提升视觉体验。
 */

/** 主题切换动画时长（毫秒） */
const val THEME_TRANSITION_DURATION_MS = 600

/**
 * Task 38.2: EaseInOutCubic 缓动曲线
 *
 * 三次贝塞尔曲线 (0.65, 0, 0.35, 1)：
 * - 起始与结束缓慢，中间加速
 * - 适合颜色过渡，避免突变感
 */
val EaseInOutCubic: Easing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

/**
 * Task 38.2: 主题切换颜色动画规格
 *
 * 600ms 时长 + EaseInOutCubic 缓动，用于所有主题颜色过渡。
 */
fun <T> themeTransitionSpec(): FiniteAnimationSpec<T> = tween(
    durationMillis = THEME_TRANSITION_DURATION_MS,
    easing = EaseInOutCubic
)

/**
 * Task 38.2: 颜色平滑过渡（600ms）
 *
 * 包装 [animateColorAsState]，使用主题切换专用的 600ms EaseInOutCubic 动画规格。
 *
 * 示例：
 * ```
 * val animatedPrimary by animateThemeColor(targetColorScheme.primary)
 * ```
 *
 * @param targetValue 目标颜色
 * @param label 动画标签（用于调试）
 * @return 当前动画颜色值
 */
@Composable
fun animateThemeColor(
    targetValue: Color,
    label: String = "themeColor"
): Color {
    return animateColorAsState(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = THEME_TRANSITION_DURATION_MS,
            easing = EaseInOutCubic
        ),
        label = label
    ).value
}

/**
 * Task 38.2: 透明度平滑过渡（600ms）
 *
 * 包装 [animateFloatAsState]，使用主题切换专用的 600ms EaseInOutCubic 动画规格。
 *
 * @param targetValue 目标透明度（0f-1f）
 * @param label 动画标签
 * @return 当前动画透明度值
 */
@Composable
fun animateThemeFloat(
    targetValue: Float,
    label: String = "themeFloat"
): Float {
    return animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = THEME_TRANSITION_DURATION_MS,
            easing = EaseInOutCubic
        ),
        label = label
    ).value
}

/**
 * Task 38.2: 为 ColorScheme 的所有颜色应用 600ms 平滑过渡
 *
 * 遍历 ColorScheme 的全部颜色字段，逐一使用 [animateColorAsState] 包装，
 * 返回一个新的 ColorScheme，其颜色值会随主题切换平滑过渡。
 *
 * 用于 Theme.kt 中，使主题切换时所有颜色（primary/secondary/background/surface 等）
 * 同时以 600ms EaseInOutCubic 过渡，避免突兀跳变。
 *
 * @param targetScheme 目标颜色方案
 * @return 动画进行中的颜色方案
 */
@Composable
fun animateColorScheme(targetScheme: ColorScheme): ColorScheme {
    // 对 ColorScheme 的每个颜色字段应用 600ms 过渡动画
    val primary by animateColorAsState(
        targetValue = targetScheme.primary,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "primary"
    )
    val onPrimary by animateColorAsState(
        targetValue = targetScheme.onPrimary,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onPrimary"
    )
    val primaryContainer by animateColorAsState(
        targetValue = targetScheme.primaryContainer,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "primaryContainer"
    )
    val onPrimaryContainer by animateColorAsState(
        targetValue = targetScheme.onPrimaryContainer,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onPrimaryContainer"
    )
    val secondary by animateColorAsState(
        targetValue = targetScheme.secondary,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "secondary"
    )
    val onSecondary by animateColorAsState(
        targetValue = targetScheme.onSecondary,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onSecondary"
    )
    val secondaryContainer by animateColorAsState(
        targetValue = targetScheme.secondaryContainer,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "secondaryContainer"
    )
    val onSecondaryContainer by animateColorAsState(
        targetValue = targetScheme.onSecondaryContainer,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onSecondaryContainer"
    )
    val tertiary by animateColorAsState(
        targetValue = targetScheme.tertiary,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "tertiary"
    )
    val onTertiary by animateColorAsState(
        targetValue = targetScheme.onTertiary,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onTertiary"
    )
    val tertiaryContainer by animateColorAsState(
        targetValue = targetScheme.tertiaryContainer,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "tertiaryContainer"
    )
    val onTertiaryContainer by animateColorAsState(
        targetValue = targetScheme.onTertiaryContainer,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onTertiaryContainer"
    )
    val background by animateColorAsState(
        targetValue = targetScheme.background,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "background"
    )
    val onBackground by animateColorAsState(
        targetValue = targetScheme.onBackground,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onBackground"
    )
    val surface by animateColorAsState(
        targetValue = targetScheme.surface,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "surface"
    )
    val onSurface by animateColorAsState(
        targetValue = targetScheme.onSurface,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onSurface"
    )
    val surfaceVariant by animateColorAsState(
        targetValue = targetScheme.surfaceVariant,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "surfaceVariant"
    )
    val onSurfaceVariant by animateColorAsState(
        targetValue = targetScheme.onSurfaceVariant,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onSurfaceVariant"
    )
    val surfaceTint by animateColorAsState(
        targetValue = targetScheme.surfaceTint,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "surfaceTint"
    )
    val inverseSurface by animateColorAsState(
        targetValue = targetScheme.inverseSurface,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "inverseSurface"
    )
    val inverseOnSurface by animateColorAsState(
        targetValue = targetScheme.inverseOnSurface,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "inverseOnSurface"
    )
    val inversePrimary by animateColorAsState(
        targetValue = targetScheme.inversePrimary,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "inversePrimary"
    )
    val error by animateColorAsState(
        targetValue = targetScheme.error,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "error"
    )
    val onError by animateColorAsState(
        targetValue = targetScheme.onError,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onError"
    )
    val errorContainer by animateColorAsState(
        targetValue = targetScheme.errorContainer,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "errorContainer"
    )
    val onErrorContainer by animateColorAsState(
        targetValue = targetScheme.onErrorContainer,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "onErrorContainer"
    )
    val outline by animateColorAsState(
        targetValue = targetScheme.outline,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "outline"
    )
    val outlineVariant by animateColorAsState(
        targetValue = targetScheme.outlineVariant,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "outlineVariant"
    )
    val scrim by animateColorAsState(
        targetValue = targetScheme.scrim,
        animationSpec = tween(THEME_TRANSITION_DURATION_MS, easing = EaseInOutCubic),
        label = "scrim"
    )

    return targetScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        inversePrimary = inversePrimary,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim
    )
}

/**
 * Task 38.2: 主题切换过渡修饰符
 *
 * 在主题切换时为组件内容添加淡入淡出过渡效果。
 * 通过 [drawWithContent] 在绘制时叠加透明度动画，
 * 使内容在主题切换时平滑过渡，避免突兀的颜色跳变。
 *
 * 使用场景：可应用于整个 Surface 或特定组件，
 * 配合 [animateColorScheme] 实现双重过渡效果。
 *
 * @param transitionProgress 过渡进度（0f=旧主题, 1f=新主题）
 */
fun Modifier.animateThemeChange(
    transitionProgress: Float = 1f
): Modifier = this.drawWithContent {
    // 在过渡期间对内容施加轻微透明度变化，增强过渡感
    val alpha = transitionProgress.coerceIn(0f, 1f)
    drawContent()
    // 过渡未完成时叠加半透明遮罩，平滑视觉过渡
    if (alpha < 1f) {
        drawRect(color = Color.White.copy(alpha = (1f - alpha) * 0.3f))
    }
}
