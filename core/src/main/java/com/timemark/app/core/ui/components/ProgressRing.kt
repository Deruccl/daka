package com.timemark.app.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.Primary
import com.timemark.app.core.ui.theme.TimeMarkTheme

/**
 * 环形进度条
 *
 * 带动画的环形进度指示器，可在中心显示文字（如百分比）。
 * 进度变化时有平滑过渡动画。
 *
 * @param progress 进度值 0..1
 * @param modifier 修饰符
 * @param strokeWidth 描边宽度
 * @param color 进度颜色
 * @param trackColor 轨道颜色
 * @param size 整体尺寸
 * @param text 中心文字（可选）
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.1f),
    size: Dp = 64.dp,
    text: String? = null
) {
    // 进度动画
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = this.size.minDimension
            val arcSize = Size(
                width = diameter - strokeWidthPx,
                height = diameter - strokeWidthPx
            )
            val arcTopLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)

            // 轨道
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // 进度弧（从顶部 -90° 开始顺时针）
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // 中心文字
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = (size.value / 3f).sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(name = "ProgressRing - 0%", showBackground = true)
@Composable
private fun ProgressRingEmptyPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            ProgressRing(progress = 0f, text = "0%")
        }
    }
}

@Preview(name = "ProgressRing - 60%", showBackground = true)
@Composable
private fun ProgressRingPartialPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            ProgressRing(progress = 0.6f, text = "60%", size = 80.dp)
        }
    }
}

@Preview(name = "ProgressRing - 100%", showBackground = true)
@Composable
private fun ProgressRingFullPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            ProgressRing(progress = 1f, text = "100%", size = 96.dp, color = Primary)
        }
    }
}
