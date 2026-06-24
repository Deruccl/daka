package com.timemark.app.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.Primary
import com.timemark.app.core.ui.theme.PrimaryGradient
import com.timemark.app.core.ui.theme.Success
import com.timemark.app.core.ui.theme.TimeMarkTheme

/**
 * 带光效的进度条
 *
 * 线性进度条，进度填充使用渐变色，并有流动光效。
 * 达到 100% 时触发庆祝动画（光效加速 + 颜色变为成功色）。
 *
 * @param progress 进度值 0..1
 * @param modifier 修饰符
 * @param height 进度条高度
 * @param color 进度颜色
 * @param trackColor 轨道颜色
 */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    color: Color = Primary,
    trackColor: Color = color.copy(alpha = 0.1f)
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val isComplete = clampedProgress >= 1f

    // 进度动画
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    // 流动光效动画
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isComplete) 800 else 1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // 完成时颜色切换
    val fillColor = if (isComplete) Success else color

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val cornerRadius = canvasHeight / 2f

        // 轨道
        drawRoundRect(
            color = trackColor,
            size = Size(canvasWidth, canvasHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
        )

        // 进度填充
        val progressWidth = canvasWidth * animatedProgress
        if (progressWidth > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = if (isComplete) {
                        listOf(Success, Success.copy(alpha = 0.8f))
                    } else {
                        PrimaryGradient
                    }
                ),
                size = Size(progressWidth, canvasHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )

            // 流动光效（在进度区域内移动的高光）
            if (animatedProgress > 0.1f) {
                val shimmerWidth = progressWidth * 0.3f
                val shimmerStart = (progressWidth - shimmerWidth) * shimmerOffset
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0f)
                        ),
                        startX = shimmerStart,
                        endX = shimmerStart + shimmerWidth
                    ),
                    size = Size(progressWidth, canvasHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )
            }

            // 完成时的发光效果
            if (isComplete) {
                drawRoundRect(
                    color = Success.copy(alpha = 0.3f),
                    size = Size(canvasWidth, canvasHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Preview(name = "ProgressBar - 0%", showBackground = true)
@Composable
private fun ProgressBarEmptyPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            Box(modifier = Modifier.padding(16.dp)) {
                ProgressBar(progress = 0f)
            }
        }
    }
}

@Preview(name = "ProgressBar - 50%", showBackground = true)
@Composable
private fun ProgressBarHalfPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            Box(modifier = Modifier.padding(16.dp)) {
                ProgressBar(progress = 0.5f)
            }
        }
    }
}

@Preview(name = "ProgressBar - 100%", showBackground = true)
@Composable
private fun ProgressBarCompletePreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            Box(modifier = Modifier.padding(16.dp)) {
                ProgressBar(progress = 1f)
            }
        }
    }
}

@Preview(name = "ProgressBar - 粗版", showBackground = true)
@Composable
private fun ProgressBarThickPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            Box(modifier = Modifier.padding(16.dp)) {
                ProgressBar(progress = 0.7f, height = 16.dp)
            }
        }
    }
}
