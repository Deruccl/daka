package com.timemark.app.feature.tracker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.model.ReminderFrequency
import com.timemark.app.domain.model.TimePeriod
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.model.TrackerTemplate
import com.timemark.app.domain.model.TrackerType
import com.timemark.app.domain.usecase.tracker.DeleteTrackerUseCase
import com.timemark.app.domain.usecase.tracker.GetTrackersUseCase
import com.timemark.app.domain.usecase.tracker.UpdateTrackerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 编辑打卡项 ViewModel
 *
 * 从 [SavedStateHandle] 读取 trackerId，加载对应 [Tracker] 数据并预填表单。
 * 复用 [TrackerFormActions] 接口，使编辑页与创建页共享同一套表单组件。
 *
 * 提供 [save] 更新与 [delete] 删除操作。
 */
@HiltViewModel
class EditTrackerViewModel @Inject constructor(
    private val getTrackersUseCase: GetTrackersUseCase,
    private val updateTrackerUseCase: UpdateTrackerUseCase,
    private val deleteTrackerUseCase: DeleteTrackerUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), TrackerFormActions {

    /** 待编辑的打卡项 ID */
    val trackerId: Long = savedStateHandle.get<Long>("trackerId") ?: 0L

    /** 原始 Tracker 数据（用于判断是否加载完成） */
    val tracker: StateFlow<Tracker?> = getTrackersUseCase.byId(trackerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** 表单草稿（由原始 Tracker 初始化） */
    private val _trackerDraft = MutableStateFlow<TrackerDraft?>(null)
    val trackerDraft: StateFlow<TrackerDraft?> = _trackerDraft

    /** 删除完成事件（一次性消费） */
    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted

    init {
        // 监听原始数据，首次加载时初始化草稿
        viewModelScope.launch {
            tracker.collect { t ->
                if (t != null && _trackerDraft.value == null) {
                    _trackerDraft.value = t.toDraft()
                }
            }
        }
    }

    /** Tracker 转 TrackerDraft */
    private fun Tracker.toDraft(): TrackerDraft = TrackerDraft(
        name = name,
        icon = icon,
        color = color,
        type = type,
        unit = unit,
        targetValue = targetValue,
        description = description,
        timePeriod = timePeriod,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        reminderFrequency = reminderFrequency,
        reminderIntervalHours = reminderIntervalHours,
        aiEnabled = aiEnabled,
        isVisible = isVisible
    )

    private fun mutate(block: (TrackerDraft) -> TrackerDraft) {
        _trackerDraft.value = _trackerDraft.value?.let(block)
    }

    override fun selectType(type: TrackerType) {
        mutate { it.copy(type = type) }
    }

    override fun selectTemplate(template: TrackerTemplate) {
        mutate {
            it.copy(
                name = template.name,
                icon = template.icon,
                color = template.color,
                type = template.type,
                unit = template.unit,
                targetValue = template.targetValue,
                description = template.description
            )
        }
    }

    override fun updateName(name: String) = mutate { it.copy(name = name) }
    override fun updateIcon(icon: String) = mutate { it.copy(icon = icon) }
    override fun updateColor(color: String) = mutate { it.copy(color = color) }
    override fun updateUnit(unit: String) = mutate { it.copy(unit = unit) }
    override fun updateTargetValue(value: Double) = mutate { it.copy(targetValue = value) }
    override fun updateDescription(desc: String) = mutate { it.copy(description = desc) }
    override fun updateTimePeriod(period: TimePeriod) = mutate { it.copy(timePeriod = period) }
    override fun updateReminderEnabled(enabled: Boolean) = mutate { it.copy(reminderEnabled = enabled) }
    override fun updateReminderTime(time: String) = mutate { it.copy(reminderTime = time) }
    override fun updateReminderFrequency(freq: ReminderFrequency) = mutate { it.copy(reminderFrequency = freq) }
    override fun updateReminderInterval(hours: Int) = mutate { it.copy(reminderIntervalHours = hours) }
    override fun updateAiEnabled(enabled: Boolean) = mutate { it.copy(aiEnabled = enabled) }
    override fun updateVisible(visible: Boolean) = mutate { it.copy(isVisible = visible) }

    /**
     * 保存修改
     * @return 名称非空时返回 true 并触发更新；名称为空返回 false
     */
    fun save(): Boolean {
        val draft = _trackerDraft.value ?: return false
        if (draft.name.isBlank()) return false
        val original = tracker.value ?: return false

        viewModelScope.launch {
            val updated = original.copy(
                name = draft.name,
                icon = draft.icon,
                color = draft.color,
                type = draft.type,
                unit = draft.unit,
                targetValue = draft.targetValue,
                description = draft.description,
                timePeriod = draft.timePeriod,
                reminderEnabled = draft.reminderEnabled,
                reminderTime = draft.reminderTime,
                reminderFrequency = draft.reminderFrequency,
                reminderIntervalHours = draft.reminderIntervalHours,
                aiEnabled = draft.aiEnabled,
                isVisible = draft.isVisible,
                updatedAt = System.currentTimeMillis()
            )
            updateTrackerUseCase(updated)
        }
        return true
    }

    /** 删除当前打卡项 */
    fun delete() {
        viewModelScope.launch {
            deleteTrackerUseCase(trackerId)
            _deleted.value = true
        }
    }
}
