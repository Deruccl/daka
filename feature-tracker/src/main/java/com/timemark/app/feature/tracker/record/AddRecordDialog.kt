package com.timemark.app.feature.tracker.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassDialog
import com.timemark.app.core.ui.components.glass.GlassTextField

/**
 * 添加记录对话框
 *
 * 输入字段：
 * - 数值（计数/数值型，默认 1.0）
 * - 时间（HH:mm）
 * - 备注（可选）
 *
 * 图片上传与标签选择为简化实现，暂不在此对话框中提供。
 *
 * @param onDismiss 关闭回调
 * @param onConfirm 确认回调，传入数值与备注
 * @param defaultValue 默认数值
 * @param defaultTime 默认时间
 */
@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (value: Double, time: String, note: String) -> Unit,
    defaultValue: Double = 1.0,
    defaultTime: String = ""
) {
    var value by remember { mutableStateOf(formatInputValue(defaultValue)) }
    var time by remember { mutableStateOf(defaultTime) }
    var note by remember { mutableStateOf("") }

    GlassDialog(
        onDismissRequest = onDismiss,
        title = "添加记录",
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 数值输入
                GlassTextField(
                    value = value,
                    onValueChange = { value = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    placeholder = "数值（如 1.0）",
                    modifier = Modifier.fillMaxWidth()
                )

                // 时间输入
                GlassTextField(
                    value = time,
                    onValueChange = { time = it },
                    placeholder = "时间（HH:mm）",
                    modifier = Modifier.fillMaxWidth()
                )

                // 备注输入
                GlassTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "备注（可选）",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        confirmButton = {
            GlassButton(
                text = "确认",
                onClick = {
                    val parsedValue = value.toDoubleOrNull() ?: 1.0
                    onConfirm(parsedValue, time, note)
                }
            )
        },
        dismissButton = {
            GlassButton(
                text = "取消",
                onClick = onDismiss,
                type = GlassButtonType.SECONDARY
            )
        }
    )
}

/** 格式化默认数值为输入框初始值：整数去小数 */
private fun formatInputValue(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format("%.1f", value)
}
