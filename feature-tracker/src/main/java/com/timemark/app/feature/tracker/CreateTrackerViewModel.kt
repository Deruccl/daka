package com.timemark.app.feature.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.model.ReminderFrequency
import com.timemark.app.domain.model.TimePeriod
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.model.TrackerTemplate
import com.timemark.app.domain.model.TrackerType
import com.timemark.app.domain.usecase.tracker.CreateTrackerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 创建打卡项 ViewModel
 *
 * 采用分步引导式创建（4 步）：
 * - Step 0: 类型选择/模板
 * - Step 1: 基础设置
 * - Step 2: 高级设置
 * - Step 3: 预览确认
 *
 * 通过 [TrackerDraft] 暂存表单数据，最终调用 [CreateTrackerUseCase] 持久化。
 */
@HiltViewModel
class CreateTrackerViewModel @Inject constructor(
    private val createTrackerUseCase: CreateTrackerUseCase
) : ViewModel(), TrackerFormActions {

    /** 当前步骤索引（0-3） */
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep

    /** 打卡项草稿数据 */
    private val _trackerDraft = MutableStateFlow(TrackerDraft())
    val trackerDraft: StateFlow<TrackerDraft> = _trackerDraft

    override fun selectType(type: TrackerType) {
        _trackerDraft.value = _trackerDraft.value.copy(type = type)
    }

    override fun selectTemplate(template: TrackerTemplate) {
        _trackerDraft.value = TrackerDraft(
            name = template.name,
            icon = template.icon,
            color = template.color,
            type = template.type,
            unit = template.unit,
            targetValue = template.targetValue,
            description = template.description
        )
    }

    override fun updateName(name: String) {
        _trackerDraft.value = _trackerDraft.value.copy(name = name)
    }

    override fun updateIcon(icon: String) {
        _trackerDraft.value = _trackerDraft.value.copy(icon = icon)
    }

    override fun updateColor(color: String) {
        _trackerDraft.value = _trackerDraft.value.copy(color = color)
    }

    override fun updateUnit(unit: String) {
        _trackerDraft.value = _trackerDraft.value.copy(unit = unit)
    }

    override fun updateTargetValue(value: Double) {
        _trackerDraft.value = _trackerDraft.value.copy(targetValue = value)
    }

    override fun updateDescription(desc: String) {
        _trackerDraft.value = _trackerDraft.value.copy(description = desc)
    }

    override fun updateTimePeriod(period: TimePeriod) {
        _trackerDraft.value = _trackerDraft.value.copy(timePeriod = period)
    }

    override fun updateReminderEnabled(enabled: Boolean) {
        _trackerDraft.value = _trackerDraft.value.copy(reminderEnabled = enabled)
    }

    override fun updateReminderTime(time: String) {
        _trackerDraft.value = _trackerDraft.value.copy(reminderTime = time)
    }

    override fun updateReminderFrequency(freq: ReminderFrequency) {
        _trackerDraft.value = _trackerDraft.value.copy(reminderFrequency = freq)
    }

    override fun updateReminderInterval(hours: Int) {
        _trackerDraft.value = _trackerDraft.value.copy(reminderIntervalHours = hours)
    }

    override fun updateAiEnabled(enabled: Boolean) {
        _trackerDraft.value = _trackerDraft.value.copy(aiEnabled = enabled)
    }

    override fun updateVisible(visible: Boolean) {
        _trackerDraft.value = _trackerDraft.value.copy(isVisible = visible)
    }

    /** 进入下一步（最多到第 3 步） */
    fun nextStep() {
        if (_currentStep.value < 3) _currentStep.value++
    }

    /** 返回上一步（最少到第 0 步） */
    fun previousStep() {
        if (_currentStep.value > 0) _currentStep.value--
    }

    /** 跳转到指定步骤 */
    fun goToStep(step: Int) {
        _currentStep.value = step.coerceIn(0, 3)
    }

    /**
     * 保存打卡项
     * @return 名称非空时返回 true 并触发创建；名称为空返回 false
     */
    fun save(): Boolean {
        val draft = _trackerDraft.value
        if (draft.name.isBlank()) return false

        viewModelScope.launch {
            val tracker = Tracker(
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
                isVisible = draft.isVisible
            )
            createTrackerUseCase(tracker)
        }
        return true
    }
}

/**
 * 打卡项草稿数据
 *
 * 用于在分步创建流程中暂存用户输入，最终转换为 [Tracker] 持久化。
 */
data class TrackerDraft(
    val name: String = "",
    val icon: String = "📝",
    val color: String = "#6366F1",
    val type: TrackerType = TrackerType.COUNT,
    val unit: String = "",
    val targetValue: Double = 0.0,
    val description: String = "",
    val timePeriod: TimePeriod = TimePeriod.ALL_DAY,
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null,
    val reminderFrequency: ReminderFrequency = ReminderFrequency.DAILY,
    val reminderIntervalHours: Int = 2,
    val aiEnabled: Boolean = false,
    val isVisible: Boolean = true
)
