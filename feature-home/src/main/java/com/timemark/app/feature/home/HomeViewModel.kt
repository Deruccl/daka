package com.timemark.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.model.DailyStats
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.usecase.record.AddRecordUseCase
import com.timemark.app.domain.usecase.stats.GetDailyStatsUseCase
import com.timemark.app.domain.usecase.stats.GetStreakUseCase
import com.timemark.app.domain.usecase.tracker.GetTrackersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 首页 ViewModel
 *
 * 管理首页 UI 状态：
 * - 当前选中的日期
 * - 打卡项目列表（含当日统计与连续天数）
 * - 整体完成进度
 *
 * 提供日期切换与快速打卡操作。
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrackersUseCase: GetTrackersUseCase,
    private val getDailyStatsUseCase: GetDailyStatsUseCase,
    private val getStreakUseCase: GetStreakUseCase,
    private val addRecordUseCase: AddRecordUseCase
) : ViewModel() {

    /** 当前选中的日期，默认今天 */
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    /** 首页 UI 状态流：日期 + 可见打卡项目 -> 聚合统计 */
    val uiState: StateFlow<HomeUiState> = combine(
        _selectedDate,
        getTrackersUseCase.visible()
    ) { date, trackers ->
        // 加载每个 tracker 的当日统计与连续天数
        val trackerWithStats = trackers.map { tracker ->
            TrackerWithStats(
                tracker = tracker,
                dailyStats = getDailyStatsUseCase(
                    tracker.id,
                    date.format(DateTimeFormatter.ISO_DATE)
                ).first(),
                streak = getStreakUseCase(tracker.id).first()
            )
        }
        HomeUiState.Loaded(
            date = date,
            trackers = trackerWithStats,
            totalCompleted = trackerWithStats.count { it.isCompleted },
            totalCount = trackerWithStats.size,
            completionRate = if (trackerWithStats.isNotEmpty())
                trackerWithStats.count { it.isCompleted }.toFloat() / trackerWithStats.size
            else 0f
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState.Loading
    )

    /** 切换到指定日期 */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /** 切换到上一天 */
    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    /** 切换到下一天 */
    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    /** 回到今天 */
    fun goToToday() {
        _selectedDate.value = LocalDate.now()
    }

    /** 快速打卡（+1） */
    fun quickCheckIn(tracker: Tracker) {
        viewModelScope.launch {
            val date = _selectedDate.value.format(DateTimeFormatter.ISO_DATE)
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            addRecordUseCase(
                trackerId = tracker.id,
                value = 1.0,
                date = date,
                time = time
            )
        }
    }
}

/**
 * 首页 UI 状态
 */
sealed class HomeUiState {
    /** 加载中 */
    object Loading : HomeUiState()

    /** 加载完成 */
    data class Loaded(
        val date: LocalDate,
        val trackers: List<TrackerWithStats>,
        val totalCompleted: Int,
        val totalCount: Int,
        val completionRate: Float
    ) : HomeUiState()
}

/**
 * 打卡项目及其统计信息
 */
data class TrackerWithStats(
    val tracker: Tracker,
    val dailyStats: DailyStats?,
    val streak: Int
) {
    /** 当日当前值 */
    val currentValue: Double get() = dailyStats?.totalValue ?: 0.0

    /** 是否已完成目标 */
    val isCompleted: Boolean get() = dailyStats?.completed == true

    /** 进度（0..1） */
    val progress: Float get() = if (tracker.hasTarget)
        (currentValue / tracker.targetValue).toFloat().coerceIn(0f, 1f)
    else 0f
}
