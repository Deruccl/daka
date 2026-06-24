package com.timemark.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.Primary

/**
 * 密码输入框（Task 36.1）
 *
 * 基于 GlassTextField 样式，支持：
 * - 显示/隐藏密码切换（眼睛图标）
 * - 默认隐藏（KeyboardType.Password）
 * - 占位符模式（显示 ••••••，仅当 [placeholderMode] 为 true 时）
 *
 * 占位符模式适用于编辑场景：API Key 默认显示为 ••••••••，
 * 用户点击修改后才显示实际内容，避免敏感信息泄露。
 *
 * @param value 当前文本值
 * @param onValueChange 文本变化回调
 * @param modifier 修饰符
 * @param placeholder 占位提示文字
 * @param placeholderMode 是否为占位符模式（true 时显示 ••••••，点击后切换为编辑模式）
 * @param enabled 是否启用
 * @param singleLine 是否单行
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    placeholderMode: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    // 密码可见性状态（默认隐藏）
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    // 占位符模式下，是否已切换到编辑模式
    var editMode by rememberSaveable { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

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

    // 占位符模式下显示的掩码字符
    val maskedValue = "••••••••"

    // 实际显示的值：占位符模式且未进入编辑模式时显示掩码
    val displayValue = if (placeholderMode && !editMode) maskedValue else value

    // 是否以密码形式显示（不可见）
    val showAsPassword = !passwordVisible && (!placeholderMode || editMode)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                if (isFocused) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Primary.copy(alpha = 0.4f), Color.Transparent),
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
            .clickable(
                // 占位符模式下点击进入编辑模式
                enabled = placeholderMode && !editMode,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                editMode = true
                passwordVisible = true
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = displayValue,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                enabled = enabled && (!placeholderMode || editMode),
                singleLine = singleLine,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                ),
                cursorBrush = SolidColor(Primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (showAsPassword) KeyboardType.Password else KeyboardType.Text
                ),
                visualTransformation = if (showAsPassword) PasswordVisualTransformation() else VisualTransformation.None,
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (displayValue.isEmpty()) {
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
            // 显示/隐藏密码切换图标（仅在编辑模式或非占位符模式下显示）
            if (!placeholderMode || editMode) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
