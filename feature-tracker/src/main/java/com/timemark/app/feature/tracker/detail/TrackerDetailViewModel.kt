package com.timemark.app.feature.tracker.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.model.DailyStats
import com.timemark.app.domain.model.RangeStats
import com.timemark.app.domain.model.Record
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.usecase.record.AddRecordUseCase
import com.timemark.app.domain.usecase.record.DeleteRecordUseCase
import com.timemark.app.domain.usecase.record.GetRecordsUseCase
import com.timemark.app.domain.usecase.record.UpdateRecordUseCase
import com.timemark.app.domain.usecase.stats.GetDailyStatsUseCase
import com.timemark.app.domain.usecase.stats.GetRangeStatsUseCase
import com.timemark.app.domain.usecase.stats.GetStreakUseCase
import com.timemark.app.domain.usecase.tracker.GetTrackersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 详情页视图模式
 * - DAY：按日查看记录
 * - WEEK：按周查看记录
 * - MONTH：按月查看记录
 */
enum class DetailViewMode { DAY, WEEK, MONTH }

/**
 * 打卡详情页 ViewModel
 *
 * 管理详情页 UI 状态：
 * - 当前打卡项信息
 * - 选中日期与视图模式（日/周/月）
 * - 当日统计、当日记录列表、连续打卡天数
 * - 最近 7 天范围统计
 *
 * 提供日期切换、视图模式切换、快速打卡、添加/更新/删除记录等操作。
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrackerDetailViewModel @Inject constructor(
    private val getTrackersUseCase: GetTrackersUseCase,
    private val getRecordsUseCase: GetRecordsUseCase,
    private val getDailyStatsUseCase: GetDailyStatsUseCase,
    private val getRangeStatsUseCase: GetRangeStatsUseCase,
    private val getStreakUseCase: GetStreakUseCase,
    private val addRecordUseCase: AddRecordUseCase,
    private val updateRecordUseCase: UpdateRecordUseCase,
    private val deleteRecordUseCase: DeleteRecordUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** 从导航参数获取的打卡项 ID */
    val trackerId: Long = savedStateHandle.get<Long>("trackerId") ?: 0

    /** 当前打卡项 */
    val tracker: StateFlow<Tracker?> = getTrackersUseCase.byId(trackerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** 当前选中的日期，默认今天 */
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    /** 当前视图模式（日/周/月） */
    private val _viewMode = MutableStateFlow(DetailViewMode.DAY)
    val viewMode: StateFlow<DetailViewMode> = _viewMode

    /** 选中日期的当日统计 */
    val todayStats: StateFlow<DailyStats?> = combine(_selectedDate, tracker) { date, t ->
        if (t != null) getDailyStatsUseCase(t.id, date.format(DateTimeFormatter.ISO_DATE)).first()
        else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** 选中日期的当日记录列表 */
    val todayRecords: StateFlow<List<Record>> = combine(_selectedDate, tracker) { date, t ->
        if (t != null) getRecordsUseCase.byTrackerAndDate(t.id, date.format(DateTimeFormatter.ISO_DATE)).first()
        else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 连续打卡天数 */
    val streak: StateFlow<Int> = tracker.flatMapLatest { t ->
        if (t != null) getStreakUseCase(t.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** 最近 7 天范围统计 */
    val weekStats: StateFlow<RangeStats?> = combine(_selectedDate, tracker) { date, t ->
        if (t != null) {
            val end = date.format(DateTimeFormatter.ISO_DATE)
            val start = date.minusDays(6).format(DateTimeFormatter.ISO_DATE)
            getRangeStatsUseCase(t.id, start, end).first()
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** 切换到指定日期 */
    fun selectDate(date: LocalDate) { _selectedDate.value = date }

    /** 切换到上一天 */
    fun previousDay() { _selectedDate.value = _selectedDate.value.minusDays(1) }

    /** 切换到下一天 */
    fun nextDay() { _selectedDate.value = _selectedDate.value.plusDays(1) }

    /** 切换视图模式 */
    fun setViewMode(mode: DetailViewMode) { _viewMode.value = mode }

    /** 快速添加一条记录（默认值 1.0） */
    fun quickAddRecord(value: Double = 1.0, note: String = "") {
        viewModelScope.launch {
            val date = _selectedDate.value.format(DateTimeFormatter.ISO_DATE)
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            addRecordUseCase(trackerId, value = value, date = date, time = time, note = note)
        }
    }

    /** 添加一条完整记录 */
    fun addRecord(
        value: Double,
        date: String,
        time: String,
        note: String,
        images: List<String>,
        tags: List<String>
    ) {
        viewModelScope.launch {
            addRecordUseCase(trackerId, value, date, time, note, images, tags)
        }
    }

    /** 更新记录 */
    fun updateRecord(record: Record) {
        viewModelScope.launch { updateRecordUseCase(record) }
    }

    /** 删除记录 */
    fun deleteRecord(id: Long) {
        viewModelScope.launch { deleteRecordUseCase(id) }
    }
}
