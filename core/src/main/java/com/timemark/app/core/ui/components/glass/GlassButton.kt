package com.timemark.app.core.ui.components.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.DarkSurface
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.PrimaryGradient
import com.timemark.app.core.ui.theme.TimeMarkTheme
import com.timemark.app.core.utils.HapticLevel
import com.timemark.app.core.utils.rememberHapticFeedback

/**
 * 玻璃按钮类型
 *
 * - PRIMARY：主按钮，渐变填充 + 玻璃质感，用于主要操作
 * - SECONDARY：次按钮，纯玻璃效果，用于次要操作
 * - SMALL：小按钮，轻薄玻璃，用于辅助操作
 */
enum class GlassButtonType {
    PRIMARY,   // 主按钮：渐变填充 + 玻璃质感
    SECONDARY, // 次按钮：纯玻璃效果
    SMALL      // 小按钮：轻薄玻璃
}

/**
 * 液态玻璃按钮
 *
 * 根据类型呈现不同视觉风格：
 * - PRIMARY：主色渐变背景 + 玻璃高光，文字为白色
 * - SECONDARY：纯玻璃背景（半透明），文字跟随主题
 * - SMALL：轻薄玻璃，尺寸更小
 *
 * 按压时有缩放回弹动画（液态动效）。
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param type 按钮类型
 * @param enabled 是否启用
 * @param icon 可选的前置图标
 * @param contentDescription 无障碍内容描述，为 null 时使用 [text]
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: GlassButtonType = GlassButtonType.PRIMARY,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // 触觉反馈控制器（用于按钮点击反馈）
    val haptic = rememberHapticFeedback()

    // 按压缩放动画（液态回弹）
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "buttonScale"
    )

    val shape = when (type) {
        GlassButtonType.SMALL -> RoundedCornerShape(12.dp)
        else -> RoundedCornerShape(16.dp)
    }

    val contentPadding = when (type) {
        GlassButtonType.SMALL -> PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        else -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    }

    // 背景与文字颜色
    val backgroundColors = when (type) {
        GlassButtonType.PRIMARY -> PrimaryGradient
        GlassButtonType.SECONDARY -> if (isDark) {
            listOf(Color(0xCC1E293B), Color(0x661E293B))
        } else {
            listOf(Color(0xCCFFFFFF), Color(0x66FFFFFF))
        }
        GlassButtonType.SMALL -> if (isDark) {
            listOf(Color(0x991E293B), Color(0x4D1E293B))
        } else {
            listOf(Color(0x99FFFFFF), Color(0x4DFFFFFF))
        }
    }

    val textColor = when (type) {
        GlassButtonType.PRIMARY -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when (type) {
        GlassButtonType.PRIMARY -> Color.White.copy(alpha = 0.3f)
        else -> if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.4f)
    }

    val alpha = if (enabled) 1f else 0.4f

    Box(
        modifier = modifier
            // 无障碍语义：设置内容描述与按钮角色，便于 TalkBack 识别
            .semantics {
                this.contentDescription = contentDescription ?: text
                role = Role.Button
            }
            .scale(scale)
            .blur(if (type == GlassButtonType.PRIMARY) 0.dp else 10.dp)
            .background(
                brush = Brush.horizontalGradient(backgroundColors),
                shape = shape,
                alpha = alpha
            )
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = alpha),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    // 触发轻微触觉反馈
                    haptic.performHaptic(HapticLevel.LIGHT)
                    onClick()
                }
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.invoke()
            Text(
                text = text,
                color = textColor.copy(alpha = alpha),
                style = if (type == GlassButtonType.SMALL) {
                    MaterialTheme.typography.labelMedium
                } else {
                    MaterialTheme.typography.labelLarge
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "GlassButton - Primary", showBackground = true)
@Composable
private fun GlassButtonPrimaryPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            GlassButton(
                text = "主按钮",
                onClick = {},
                type = GlassButtonType.PRIMARY,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "GlassButton - Secondary", showBackground = true)
@Composable
private fun GlassButtonSecondaryPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            GlassButton(
                text = "次按钮",
                onClick = {},
                type = GlassButtonType.SECONDARY,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "GlassButton - Small", showBackground = true)
@Composable
private fun GlassButtonSmallPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            GlassButton(
                text = "小按钮",
                onClick = {},
                type = GlassButtonType.SMALL,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "GlassButton - Dark", showBackground = true)
@Composable
private fun GlassButtonDarkPreview() {
    TimeMarkTheme(darkTheme = true) {
        Surface(color = DarkSurface) {
            GlassButton(
                text = "深色按钮",
                onClick = {},
                type = GlassButtonType.PRIMARY,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
