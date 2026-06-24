package com.timemark.app.feature.tracker

import com.timemark.app.domain.model.ReminderFrequency
import com.timemark.app.domain.model.TimePeriod
import com.timemark.app.domain.model.TrackerTemplate
import com.timemark.app.domain.model.TrackerType

/**
 * 打卡表单操作接口
 *
 * 抽象出创建/编辑打卡项时共有的表单操作方法，
 * 使 [CreateTrackerViewModel] 与 [EditTrackerViewModel] 可以复用同一套步骤 UI。
 */
interface TrackerFormActions {

    /** 选择打卡类型 */
    fun selectType(type: TrackerType)

    /** 选择模板并填充表单 */
    fun selectTemplate(template: TrackerTemplate)

    /** 更新名称 */
    fun updateName(name: String)

    /** 更新图标 */
    fun updateIcon(icon: String)

    /** 更新颜色 */
    fun updateColor(color: String)

    /** 更新单位 */
    fun updateUnit(unit: String)

    /** 更新每日目标值 */
    fun updateTargetValue(value: Double)

    /** 更新描述 */
    fun updateDescription(desc: String)

    /** 更新打卡时间段 */
    fun updateTimePeriod(period: TimePeriod)

    /** 更新提醒开关 */
    fun updateReminderEnabled(enabled: Boolean)

    /** 更新提醒时间 */
    fun updateReminderTime(time: String)

    /** 更新提醒频率 */
    fun updateReminderFrequency(freq: ReminderFrequency)

    /** 更新提醒间隔（小时） */
    fun updateReminderInterval(hours: Int)

    /** 更新是否启用 AI 分析 */
    fun updateAiEnabled(enabled: Boolean)

    /** 更新是否在首页可见 */
    fun updateVisible(visible: Boolean)
}
