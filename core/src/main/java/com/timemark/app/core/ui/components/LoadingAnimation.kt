package com.timemark.app.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.Primary
import com.timemark.app.core.ui.theme.TimeMarkTheme
import kotlin.math.PI
import kotlin.math.sin

/**
 * 加载动画样式
 *
 * - DROPLET_ROTATION：水滴旋转，多个水滴围绕中心旋转 + 缩放呼吸
 * - LIQUID_FLOW：液体流动，波浪形状上下流动
 * - DEFAULT：默认加载，简单的旋转圆环
 */
enum class LoadingStyle {
    DROPLET_ROTATION, // 水滴旋转
    LIQUID_FLOW,      // 液体流动
    DEFAULT           // 默认（旋转圆环）
}

/**
 * 液态加载动画
 *
 * 根据样式播放不同的加载动画：
 * - DROPLET_ROTATION：水滴围绕中心旋转，同时有缩放呼吸动画
 * - LIQUID_FLOW：波浪形状上下流动，模拟液体波动
 * - DEFAULT：简单的旋转圆环
 *
 * @param modifier 修饰符
 * @param size 动画整体尺寸
 * @param color 动画颜色
 * @param style 加载样式，默认 DROPLET_ROTATION
 */
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    style: LoadingStyle = LoadingStyle.DROPLET_ROTATION
) {
    when (style) {
        LoadingStyle.DROPLET_ROTATION -> DropletRotationLoading(modifier, size, color)
        LoadingStyle.LIQUID_FLOW -> LiquidFlowLoading(modifier, size, color)
        LoadingStyle.DEFAULT -> DefaultLoading(modifier, size, color)
    }
}

/**
 * 水滴旋转加载动画
 *
 * 多个水滴围绕中心旋转，同时有缩放呼吸动画。
 * 适用于页面加载、数据请求中等场景。
 *
 * @param modifier 修饰符
 * @param size 动画整体尺寸
 * @param color 水滴颜色
 */
@Composable
fun DropletRotationLoading(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition()

    // 旋转角度
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // 呼吸缩放
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val dropCount = 3
    val dropSize = size * 0.3f

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val orbitRadius = (size.toPx() - dropSize.toPx()) / 2f

        rotate(degrees = rotation, pivot = center) {
            for (i in 0 until dropCount) {
                val angle = (360f / dropCount) * i
                val radians = Math.toRadians(angle.toDouble()).toFloat()
                val dropCenter = Offset(
                    x = center.x + orbitRadius * kotlin.math.cos(radians),
                    y = center.y + orbitRadius * kotlin.math.sin(radians)
                )

                // 绘制水滴形状
                val dropPath = Path().apply {
                    val dropWidth = dropSize.toPx() * scale
                    val dropHeight = dropWidth * 1.3f
                    // 水滴形状：上尖下圆
                    moveTo(dropCenter.x, dropCenter.y - dropHeight / 2f)
                    cubicTo(
                        dropCenter.x + dropWidth / 2f, dropCenter.y - dropHeight / 4f,
                        dropCenter.x + dropWidth / 2f, dropCenter.y + dropHeight / 2f,
                        dropCenter.x, dropCenter.y + dropHeight / 2f
                    )
                    cubicTo(
                        dropCenter.x - dropWidth / 2f, dropCenter.y + dropHeight / 2f,
                        dropCenter.x - dropWidth / 2f, dropCenter.y - dropHeight / 4f,
                        dropCenter.x, dropCenter.y - dropHeight / 2f
                    )
                    close()
                }

                // 颜色渐变：每个水滴透明度不同
                val dropAlpha = 0.4f + (0.6f / dropCount) * (dropCount - i)
                drawPath(
                    path = dropPath,
                    color = color.copy(alpha = dropAlpha)
                )
            }
        }

        // 中心圆点
        drawCircle(
            color = color.copy(alpha = 0.3f * scale),
            radius = (size.toPx() * 0.1f) * scale,
            center = center
        )
    }
}

/**
 * 液体流动加载动画
 *
 * 使用 Canvas 绘制波浪形状，上下流动，模拟液体波动。
 * 两层波浪以不同速度和相位流动，产生层次感。
 *
 * @param modifier 修饰符
 * @param size 动画整体尺寸
 * @param color 液体颜色
 */
@Composable
fun LiquidFlowLoading(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition()

    // 波浪相位1（主波浪）
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase1"
    )

    // 波浪相位2（次波浪，速度不同）
    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase2"
    )

    // 液面高度动画（上下流动）
    val liquidLevel by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liquidLevel"
    )

    Canvas(modifier = modifier.size(size)) {
        val width = this.size.width
        val height = this.size.height
        val centerY = height * liquidLevel

        // 绘制容器圆形背景（半透明）
        drawCircle(
            color = color.copy(alpha = 0.1f),
            radius = width / 2f,
            center = Offset(width / 2f, height / 2f)
        )

        // 第一层波浪（主波浪，较深）
        val wave1Path = Path().apply {
            val amplitude = height * 0.08f
            val wavelength = width * 0.8f
            moveTo(0f, height)
            // 从左到右绘制波浪上边缘
            var x = 0f
            while (x <= width) {
                val y = centerY + amplitude * sin((x / wavelength) * 2 * PI + wavePhase1).toFloat()
                lineTo(x, y)
                x += 2f
            }
            lineTo(width, height)
            close()
        }
        drawPath(
            path = wave1Path,
            color = color.copy(alpha = 0.6f)
        )

        // 第二层波浪（次波浪，较浅，相位偏移）
        val wave2Path = Path().apply {
            val amplitude = height * 0.06f
            val wavelength = width * 0.6f
            moveTo(0f, height)
            var x = 0f
            while (x <= width) {
                val y = centerY + amplitude * sin((x / wavelength) * 2 * PI + wavePhase2 + PI).toFloat() + height * 0.05f
                lineTo(x, y)
                x += 2f
            }
            lineTo(width, height)
            close()
        }
        drawPath(
            path = wave2Path,
            color = color.copy(alpha = 0.4f)
        )

        // 顶部气泡（增加液态感）
        val bubbleRadius = width * 0.03f
        val bubbleY = centerY - height * 0.15f * (1f - liquidLevel)
        drawCircle(
            color = color.copy(alpha = 0.5f),
            radius = bubbleRadius,
            center = Offset(width * 0.35f, bubbleY)
        )
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = bubbleRadius * 0.7f,
            center = Offset(width * 0.65f, bubbleY + height * 0.05f)
        )
    }
}

/**
 * 默认加载动画
 *
 * 简单的旋转圆环，适用于性能较低的设备或简洁场景。
 *
 * @param modifier 修饰符
 * @param size 动画整体尺寸
 * @param color 圆环颜色
 */
@Composable
fun DefaultLoading(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition()

    // 旋转角度
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "defaultRotation"
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = size.toPx() / 2f - 4.dp.toPx()

        // 背景圆环（淡色）
        drawCircle(
            color = color.copy(alpha = 0.15f),
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )

        // 旋转的弧线
        rotate(degrees = rotation, pivot = center) {
            // 绘制 3/4 圆弧（留出 1/4 缺口）
            val sweepAngle = 270f
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
    }
}

@Preview(name = "LoadingAnimation - Droplet", showBackground = true)
@Composable
private fun LoadingAnimationPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation(style = LoadingStyle.DROPLET_ROTATION)
            }
        }
    }
}

@Preview(name = "LoadingAnimation - Liquid Flow", showBackground = true)
@Composable
private fun LoadingAnimationLiquidPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation(style = LoadingStyle.LIQUID_FLOW)
            }
        }
    }
}

@Preview(name = "LoadingAnimation - Default", showBackground = true)
@Composable
private fun LoadingAnimationDefaultPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation(style = LoadingStyle.DEFAULT)
            }
        }
    }
}

@Preview(name = "LoadingAnimation - 大尺寸", showBackground = true)
@Composable
private fun LoadingAnimationLargePreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation(size = 80.dp, color = Primary, style = LoadingStyle.DROPLET_ROTATION)
            }
        }
    }
}
