package com.timemark.app.core.ui.components.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.DarkSurface
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.TimeMarkTheme

/**
 * 液态玻璃顶部栏
 *
 * 轻薄玻璃质感的应用栏，滚动时逐渐变得不透明。
 * 适用于页面顶部导航，支持返回按钮、标题与右侧操作。
 *
 * @param title 标题文字
 * @param modifier 修饰符
 * @param scrollProgress 滚动进度 0..1，0 为顶部（透明），1 为滚动后（不透明）
 * @param onBackClick 返回按钮点击回调，null 时不显示返回按钮
 * @param actions 右侧操作区内容
 */
@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    scrollProgress: Float = 0f,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    // 滚动进度驱动的不透明度（0 -> 0.95）
    val animatedOpacity by animateFloatAsState(
        targetValue = scrollProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 200),
        label = "topBarOpacity"
    )

    val baseAlpha = 0.5f + animatedOpacity * 0.45f  // 0.5 -> 0.95
    val backgroundColors = if (isDark) {
        listOf(Color(0xCC1E293B), Color(0x661E293B))
    } else {
        listOf(Color(0xCCFFFFFF), Color(0x66FFFFFF))
    }
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.1f + animatedOpacity * 0.05f)
    } else {
        Color.White.copy(alpha = 0.3f + animatedOpacity * 0.1f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .blur(20.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = backgroundColors.map { it.copy(alpha = it.alpha * baseAlpha / 0.8f) }
                )
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 返回按钮
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 标题
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // 右侧操作
            actions()
        }
    }
}

@Preview(name = "GlassTopBar - 顶部", showBackground = true)
@Composable
private fun GlassTopBarTopPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            GlassTopBar(
                title = "首页",
                scrollProgress = 0f,
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "GlassTopBar - 滚动后", showBackground = true)
@Composable
private fun GlassTopBarScrolledPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            GlassTopBar(
                title = "首页",
                scrollProgress = 1f,
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "GlassTopBar - Dark", showBackground = true)
@Composable
private fun GlassTopBarDarkPreview() {
    TimeMarkTheme(darkTheme = true) {
        Surface(color = DarkSurface) {
            GlassTopBar(
                title = "统计",
                scrollProgress = 1f
            )
        }
    }
}
