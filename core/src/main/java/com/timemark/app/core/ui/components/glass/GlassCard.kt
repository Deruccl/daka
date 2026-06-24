package com.timemark.app.core.ui.components.glass

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.DarkSurface
import com.timemark.app.core.ui.theme.GlassGradientDark
import com.timemark.app.core.ui.theme.GlassGradientLight
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.TimeMarkTheme
import com.timemark.app.core.utils.PerformanceDetector
import com.timemark.app.core.utils.PerformanceLevel
import kotlinx.coroutines.launch

/**
 * 玻璃卡片层级
 *
 * 不同层级对应不同的不透明度与模糊半径：
 * - LIGHT：轻薄，适合叠加在浅色背景上
 * - STANDARD：标准，通用场景
 * - THICK：厚重，适合弹窗、底部栏等需要强对比的场景
 */
enum class GlassLevel {
    LIGHT,    // 轻薄：70% 不透明度，10dp 模糊
    STANDARD, // 标准：80% 不透明度，20dp 模糊
    THICK     // 厚重：90% 不透明度，30dp 模糊
}

/**
 * 液态玻璃卡片
 *
 * 通过多层绘制实现玻璃质感：
 * 1. 背景层：半透明渐变 + 模糊（API 31+ 使用 Modifier.blur，低版本降级为半透明纯色）
 * 2. 噪点层：极淡的随机噪点纹理（仅 HIGH 性能，3-5% 透明度）
 * 3. 折射层：边缘微妙位移变形（仅 HIGH 性能，API 31+）
 * 4. 高光层：顶部 1dp 白色高光（左右渐变，模拟光线反射）
 * 5. 边框层：1dp 半透明白色渐变边框（顶部亮底部暗，增强立体感）
 * 6. 阴影层：柔和阴影
 * 7. 水波纹层：点击时从点击位置扩散的半透明圆环（600ms，EaseOut）
 * 8. 内容层
 *
 * 性能降级策略：
 * - LOW：半透明纯色背景（无模糊），保留高光和边框，无噪点/折射/水波纹
 * - MEDIUM：降低模糊半径（固定 10dp），无噪点/折射
 * - HIGH：完整效果
 *
 * 手动设置优先：[blurEnabled] 为 false 时强制关闭模糊，为 true 时强制开启（受 API 限制），
 * 为 null 时根据设备性能自动检测。
 *
 * @param modifier 修饰符
 * @param level 玻璃层级，默认 STANDARD
 * @param shape 卡片形状，默认 16dp 圆角
 * @param onClick 点击回调，非 null 时启用点击与按压动效
 * @param blurEnabled 模糊开关，null 表示自动检测设备性能
 * @param rippleEnabled 水波纹开关，默认 true
 * @param content 卡片内容
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    level: GlassLevel = GlassLevel.STANDARD,
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    blurEnabled: Boolean? = null,
    rippleEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    // 检测设备性能等级（单例缓存，仅计算一次）
    val performanceLevel = remember { PerformanceDetector.detectDevicePerformance(context) }

    // 基础视觉参数
    val opacity = when (level) {
        GlassLevel.LIGHT -> 0.7f
        GlassLevel.STANDARD -> 0.8f
        GlassLevel.THICK -> 0.9f
    }
    val baseBlurRadius = when (level) {
        GlassLevel.LIGHT -> 10.dp
        GlassLevel.STANDARD -> 20.dp
        GlassLevel.THICK -> 30.dp
    }
    val elevation = when (level) {
        GlassLevel.LIGHT -> 2.dp
        GlassLevel.STANDARD -> 4.dp
        GlassLevel.THICK -> 8.dp
    }

    // 根据性能等级与手动设置确定最终模糊半径
    val supportsBlurApi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val effectiveBlurRadius = when {
        // 手动关闭模糊
        blurEnabled == false -> 0.dp
        // 手动开启模糊（但 API 不支持时降级）
        blurEnabled == true && !supportsBlurApi -> 0.dp
        // 自动检测：LOW 性能无模糊
        performanceLevel == PerformanceLevel.LOW -> 0.dp
        // 自动检测：MEDIUM 性能降低模糊
        performanceLevel == PerformanceLevel.MEDIUM -> 10.dp
        // 自动检测：HIGH 性能完整模糊（受 API 限制）
        !supportsBlurApi -> 0.dp
        else -> baseBlurRadius
    }
    val isBlurActive = effectiveBlurRadius > 0.dp

    // 噪点与折射仅在 HIGH 性能且模糊开启时启用
    val isNoiseEnabled = performanceLevel == PerformanceLevel.HIGH && isBlurActive
    val isRefractionEnabled = performanceLevel == PerformanceLevel.HIGH &&
        supportsBlurApi && isBlurActive
    // 水波纹在 MEDIUM 及以上性能启用（LOW 性能跳过以节省资源）
    val isRippleActive = rippleEnabled && onClick != null &&
        performanceLevel != PerformanceLevel.LOW

    // 玻璃渐变色（根据主题选择）
    val glassGradient = if (isDark) GlassGradientDark else GlassGradientLight
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.4f)
    val borderBottomColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.1f)
    val highlightColor = Color.White.copy(alpha = 0.3f)

    // 按压动画状态（手动管理，避免 clickable 与 detectTapGestures 冲突）
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "cardPressScale"
    )

    // 水波纹状态
    val rippleState = rememberRippleState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, clip = false)
            .then(if (isBlurActive) Modifier.blur(effectiveBlurRadius) else Modifier)
            .background(
                brush = Brush.linearGradient(
                    colors = glassGradient.map { it.copy(alpha = it.alpha * opacity) }
                ),
                shape = shape
            )
            // 折射效果：边缘微妙位移（仅 HIGH 性能，API 31+）
            .then(if (isRefractionEnabled) Modifier.drawRefractionEdges(shape) else Modifier)
            // 顶部高光线
            .drawHighlightLine(shape, highlightColor)
            // 渐变边框
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(borderColor, borderBottomColor)
                    )
                ),
                shape = shape
            )
            // 统一的手势处理：按压动画 + 水波纹 + 点击回调
            // 使用单一 pointerInput 避免 clickable 与 detectTapGestures 的事件消费冲突
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { offset ->
                                // 触发水波纹动画
                                if (isRippleActive) {
                                    scope.launch { rippleState.start(offset) }
                                }
                                // 执行点击回调
                                onClick()
                            }
                        )
                    }
                } else Modifier
            )
            .scale(pressScale),
        propagateMinConstraints = true
    ) {
        content()
        // 噪点纹理叠加层（仅 HIGH 性能）
        if (isNoiseEnabled) {
            NoiseOverlay(
                modifier = Modifier.matchParentSize(),
                alpha = 0.04f
            )
        }
        // 水波纹叠加层
        if (isRippleActive) {
            RippleEffectOverlay(
                state = rippleState,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

/**
 * 顶部高光绘制
 *
 * 在卡片顶部绘制一条 1dp 高的白色渐变线，
 * 模拟光线从顶部照射的反射效果。
 */
private fun Modifier.drawHighlightLine(
    shape: Shape,
    color: Color
): Modifier = this.drawBehind {
    val highlightHeight = 1.dp.toPx()
    val brush = Brush.horizontalGradient(
        colors = listOf(
            color.copy(alpha = 0f),
            color.copy(alpha = 0.5f),
            color.copy(alpha = 0f)
        )
    )
    drawRect(
        brush = brush,
        topLeft = Offset(0f, 0f),
        size = size.copy(height = highlightHeight)
    )
}

/**
 * 玻璃折射边缘效果
 *
 * 在卡片边缘绘制微妙的位移变形渐变，模拟光线穿过玻璃时的折射。
 * 通过在边缘绘制方向性渐变实现视觉上的"位移"感。
 */
private fun Modifier.drawRefractionEdges(shape: Shape): Modifier = this.drawBehind {
    val edgeWidth = 2.dp.toPx()
    // 顶部边缘折射（光线从顶部折射）
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.08f),
                Color.Transparent
            )
        ),
        topLeft = Offset(0f, 0f),
        size = size.copy(height = edgeWidth)
    )
    // 底部边缘折射
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.05f)
            )
        ),
        topLeft = Offset(0f, size.height - edgeWidth),
        size = size.copy(height = edgeWidth)
    )
}

@Preview(name = "GlassCard - Light", showBackground = true)
@Composable
private fun GlassCardLightPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            GlassCard(
                level = GlassLevel.LIGHT,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("Light Glass")
                }
            }
        }
    }
}

@Preview(name = "GlassCard - Standard", showBackground = true)
@Composable
private fun GlassCardStandardPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            GlassCard(
                level = GlassLevel.STANDARD,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("Standard Glass")
                }
            }
        }
    }
}

@Preview(name = "GlassCard - Dark", showBackground = true)
@Composable
private fun GlassCardDarkPreview() {
    TimeMarkTheme(darkTheme = true) {
        Surface(color = DarkSurface) {
            GlassCard(
                level = GlassLevel.THICK,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("Thick Glass")
                }
            }
        }
    }
}
