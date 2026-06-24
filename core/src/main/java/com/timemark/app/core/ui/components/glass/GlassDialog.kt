package com.timemark.app.core.ui.components.glass

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.timemark.app.core.ui.theme.DarkSurface
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.TimeMarkTheme

/**
 * 液态玻璃弹窗
 *
 * 厚重玻璃质感的对话框，出现时有弹性缩放动画。
 * 适用于确认操作、信息展示等场景。
 *
 * @param onDismissRequest 关闭弹窗回调
 * @param title 标题文字
 * @param modifier 修饰符
 * @param content 弹窗内容（位于标题下方）
 * @param confirmButton 确认按钮
 * @param dismissButton 取消按钮
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
    confirmButton: @Composable () -> Unit = {},
    dismissButton: @Composable () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    // 弹性出现动画
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialogScale"
    )

    val backgroundColors = if (isDark) {
        listOf(Color(0xE61E293B), Color(0xB31E293B))
    } else {
        listOf(Color(0xE6FFFFFF), Color(0xB3FFFFFF))
    }
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }

    // 点击遮罩关闭弹窗的交互源
    val dismissInteractionSource = remember { MutableInteractionSource() }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(
                    interactionSource = dismissInteractionSource,
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = modifier
                    .padding(horizontal = 32.dp)
                    .scale(scale)
                    .blur(30.dp)
                    .background(
                        brush = Brush.verticalGradient(backgroundColors),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 标题
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    // 内容
                    content()

                    // 按钮区
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        dismissButton()
                        confirmButton()
                    }
                }
            }
        }
    }
}

@Preview(name = "GlassDialog", showBackground = true)
@Composable
private fun GlassDialogPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            GlassDialog(
                onDismissRequest = {},
                title = "确认删除",
                content = {
                    Text(
                        text = "确定要删除这个打卡项吗？此操作不可撤销。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    GlassButton(
                        text = "删除",
                        onClick = {},
                        type = GlassButtonType.PRIMARY
                    )
                },
                dismissButton = {
                    GlassButton(
                        text = "取消",
                        onClick = {},
                        type = GlassButtonType.SECONDARY
                    )
                }
            )
        }
    }
}

@Preview(name = "GlassDialog - Dark", showBackground = true)
@Composable
private fun GlassDialogDarkPreview() {
    TimeMarkTheme(darkTheme = true) {
        Surface(color = DarkSurface) {
            GlassDialog(
                onDismissRequest = {},
                title = "完成打卡"
            )
        }
    }
}
