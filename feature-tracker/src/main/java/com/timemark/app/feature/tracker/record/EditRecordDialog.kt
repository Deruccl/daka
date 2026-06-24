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
import com.timemark.app.domain.model.Record

/**
 * 编辑记录对话框
 *
 * 可修改：
 * - 数值
 * - 时间（HH:mm）
 * - 备注
 *
 * 保留原记录的 id、trackerId、date、timestamp、images、tags、mood、duration、createdAt。
 *
 * @param record 待编辑的记录
 * @param onDismiss 关闭回调
 * @param onConfirm 确认回调，传入更新后的 Record
 */
@Composable
fun EditRecordDialog(
    record: Record,
    onDismiss: () -> Unit,
    onConfirm: (Record) -> Unit
) {
    var value by remember(record.id) { mutableStateOf(formatInputValue(record.value)) }
    var time by remember(record.id) { mutableStateOf(record.time) }
    var note by remember(record.id) { mutableStateOf(record.note) }

    GlassDialog(
        onDismissRequest = onDismiss,
        title = "编辑记录",
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 数值
                GlassTextField(
                    value = value,
                    onValueChange = { value = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    placeholder = "数值",
                    modifier = Modifier.fillMaxWidth()
                )

                // 时间
                GlassTextField(
                    value = time,
                    onValueChange = { time = it },
                    placeholder = "时间（HH:mm）",
                    modifier = Modifier.fillMaxWidth()
                )

                // 备注
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
                text = "保存",
                onClick = {
                    val updated = record.copy(
                        value = value.toDoubleOrNull() ?: record.value,
                        time = time.ifBlank { record.time },
                        note = note,
                        updatedAt = System.currentTimeMillis()
                    )
                    onConfirm(updated)
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

/** 格式化数值为输入框初始值：整数去小数 */
private fun formatInputValue(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format("%.1f", value)
}
