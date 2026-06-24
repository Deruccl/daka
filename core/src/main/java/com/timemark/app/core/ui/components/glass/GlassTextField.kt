package com.timemark.app.core.ui.components.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.DarkSurface
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.Primary
import com.timemark.app.core.ui.theme.TimeMarkTheme

/**
 * 液态玻璃输入框
 *
 * 玻璃质感的文本输入框，聚焦时边框发光（主色光晕）。
 * 适用于表单输入、搜索等场景。
 *
 * @param value 当前文本
 * @param onValueChange 文本变化回调
 * @param modifier 修饰符
 * @param placeholder 占位提示文字
 * @param enabled 是否启用
 * @param singleLine 是否单行
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // 聚焦时光晕动画
    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "focusGlow"
    )

    val backgroundColors = if (isDark) {
        listOf(Color(0x991E293B), Color(0x4D1E293B))
    } else {
        listOf(Color(0x99FFFFFF), Color(0x4DFFFFFF))
    }
    val borderColor = if (isFocused) {
        Primary.copy(alpha = 0.6f)
    } else if (isDark) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }
    val glowColor = Primary.copy(alpha = 0.4f * glowAlpha)

    Box(
        modifier = modifier
            .fillMaxWidth()
            // 聚焦光晕（绘制在背景层）
            .drawBehind {
                if (glowAlpha > 0f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                            radius = size.maxDimension
                        )
                    )
                }
            }
            .blur(10.dp)
            .background(
                brush = Brush.verticalGradient(backgroundColors),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
            ),
            cursorBrush = SolidColor(Primary),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Preview(name = "GlassTextField - 默认", showBackground = true)
@Composable
private fun GlassTextFieldDefaultPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            Column(modifier = Modifier.padding(16.dp)) {
                GlassTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "输入打卡名称..."
                )
            }
        }
    }
}

@Preview(name = "GlassTextField - 有内容", showBackground = true)
@Composable
private fun GlassTextFieldFilledPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            Column(modifier = Modifier.padding(16.dp)) {
                GlassTextField(
                    value = "每日阅读",
                    onValueChange = {},
                    placeholder = "输入打卡名称..."
                )
            }
        }
    }
}

@Preview(name = "GlassTextField - Dark", showBackground = true)
@Composable
private fun GlassTextFieldDarkPreview() {
    TimeMarkTheme(darkTheme = true) {
        Surface(color = DarkSurface) {
            Column(modifier = Modifier.padding(16.dp)) {
                GlassTextField(
                    value = "每日运动",
                    onValueChange = {},
                    placeholder = "输入打卡名称..."
                )
            }
        }
    }
}
