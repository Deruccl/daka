package com.timemark.app.core.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

/**
 * 敏感数据文本组件（Task 32.2）
 *
 * 默认以模糊效果显示文本内容，点击后切换为明文显示。
 * 适用于显示密码、Token、密钥等敏感信息。
 *
 * 模糊实现策略：
 * - API 31+（Android 12+）：使用 RenderEffect.createBlurEffect 硬件加速模糊
 * - API 31 以下：使用半透明遮罩覆盖文本，仅显示占位符效果
 *
 * @param text 待显示的文本内容
 * @param blurred 是否处于模糊状态（默认 true）
 * @param modifier 修饰符
 * @param maskChar 遮罩字符（API 31 以下使用），默认为 "•"
 */
@Composable
fun SensitiveText(
    text: String,
    blurred: Boolean = true,
    modifier: Modifier = Modifier,
    maskChar: String = "•"
) {
    var isBlurred by remember { mutableStateOf(blurred) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isBlurred = !isBlurred },
        contentAlignment = Alignment.CenterStart
    ) {
        if (isBlurred) {
            // 模糊状态
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // API 31+：使用 RenderEffect 硬件加速模糊
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer {
                        val blurRadius = 16f
                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                            blurRadius,
                            blurRadius,
                            android.graphics.Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                )
            } else {
                // API 31 以下：显示遮罩字符 + 半透明背景
                Box {
                    // 原始文本（不可见，仅占位）
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Transparent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // 半透明遮罩覆盖
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = maskChar.repeat(text.length.coerceAtMost(16)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // 明文显示
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(name = "SensitiveText - Blurred", showBackground = true)
@Composable
private fun SensitiveTextBlurredPreview() {
    MaterialTheme {
        SensitiveText(
            text = "sk-1234567890abcdef",
            blurred = true,
            modifier = Modifier.background(Color.White)
        )
    }
}

@Preview(name = "SensitiveText - Visible", showBackground = true)
@Composable
private fun SensitiveTextVisiblePreview() {
    MaterialTheme {
        SensitiveText(
            text = "sk-1234567890abcdef",
            blurred = false,
            modifier = Modifier.background(Color.White)
        )
    }
}
