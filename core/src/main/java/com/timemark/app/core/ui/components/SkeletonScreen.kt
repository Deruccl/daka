package com.timemark.app.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.TimeMarkTheme

/**
 * 骨架屏组件
 *
 * 加载中占位视图，有微光流动动画（shimmer）。
 * 模拟列表项的布局结构，提供视觉占位。
 *
 * @param modifier 修饰符
 * @param itemCount 骨架项数量
 */
@Composable
fun SkeletonScreen(
    modifier: Modifier = Modifier,
    itemCount: Int = 3
) {
    // 微光流动动画（1.5s 循环，光从左到右流动）
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = baseColor.copy(alpha = 0.3f)

    // 微光渐变画笔（光从左到右流动）
    val shimmerWidth = 600f
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        ),
        start = Offset(
            x = -shimmerWidth + shimmerProgress * shimmerWidth * 2f,
            y = 0f
        ),
        end = Offset(
            x = shimmerProgress * shimmerWidth * 2f,
            y = 100f
        )
    )

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(itemCount) {
            SkeletonItem(brush = shimmerBrush)
        }
    }
}

/**
 * 单个骨架项
 *
 * 模拟列表项布局：圆形头像 + 两行文字。
 */
@Composable
private fun SkeletonItem(brush: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 圆形头像占位
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(brush)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题占位
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            // 副标题占位
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Preview(name = "SkeletonScreen", showBackground = true)
@Composable
private fun SkeletonScreenPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            SkeletonScreen(itemCount = 4)
        }
    }
}

@Preview(name = "SkeletonScreen - Dark", showBackground = true)
@Composable
private fun SkeletonScreenDarkPreview() {
    TimeMarkTheme(darkTheme = true) {
        Surface(color = LightSurface) {
            SkeletonScreen(itemCount = 3)
        }
    }
}
