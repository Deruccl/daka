package com.timemark.app.feature.tracker.record

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.timemark.app.domain.model.Record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 删除并支持撤销的状态持有者
 *
 * 工作流程：
 * 1. 调用 [delete] 时，先将记录加入待删除列表（用于 UI 隐藏）；
 * 2. 弹出 Snackbar，5 秒内可点击"撤销"；
 * 3. 撤销则从待删除列表移除（记录恢复显示），不调用真实删除；
 * 4. 未撤销则调用 [onDelete] 执行真实删除，并从待删除列表移除。
 */
class DeleteWithUndoState {

    /** 待删除的记录列表（UI 应隐藏这些记录） */
    private val _pendingDelete = mutableStateListOf<Record>()
    val pendingDelete: List<Record> get() = _pendingDelete

    /** 记录是否处于待删除状态 */
    fun isPending(record: Record): Boolean = _pendingDelete.any { it.id == record.id }

    /**
     * 触发删除（带撤销）
     *
     * @param record 待删除记录
     * @param snackbarHostState Snackbar 宿主
     * @param scope 协程作用域
     * @param onDelete 确认删除时调用，传入记录 id
     */
    fun delete(
        record: Record,
        snackbarHostState: SnackbarHostState,
        scope: CoroutineScope,
        onDelete: (Long) -> Unit
    ) {
        // 加入待删除列表，UI 立即隐藏
        _pendingDelete.add(record)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "记录已删除",
                actionLabel = "撤销",
                duration = SnackbarDuration.Short // 约 5 秒
            )
            if (result == SnackbarResult.ActionPerformed) {
                // 撤销：恢复记录显示，不执行真实删除
                _pendingDelete.remove(record)
            } else {
                // 超时确认删除
                onDelete(record.id)
                _pendingDelete.remove(record)
            }
        }
    }
}

/**
 * 创建并记住 [DeleteWithUndoState]
 */
@Composable
fun rememberDeleteWithUndoState(): DeleteWithUndoState {
    return remember { DeleteWithUndoState() }
}
