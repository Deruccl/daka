package com.timemark.app.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.model.RangeStats
import com.timemark.app.domain.model.TimeViewLevel
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.usecase.stats.GetDailyStatsUseCase
import com.timemark.app.domain.usecase.stats.GetRangeStatsUseCase
import com.timemark.app.domain.usecase.tracker.GetTrackersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 统计页面 ViewModel
 *
 * 管理统计页面的全部状态：
 * - 当前选中的打卡项目（null 表示全部）
 * - 当前时间视图级别（分钟/小时/日/周/月/年）
 * - 当前选中的日期
 * - 当前时间范围对应的范围统计
 * - 双指缩放级别（用于视图粒度切换与视觉反馈）
 * - 导航历史（用于返回上一级）
 *
 * 提供切换项目、切换视图级别、上一段/下一段时间导航、
 * 双指缩放切换粒度、点击进入下一级、返回上一级等操作。
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getTrackersUseCase: GetTrackersUseCase,
    private val getRangeStatsUseCase: GetRangeStatsUseCase,
    private val getDailyStatsUseCase: GetDailyStatsUseCase
) : ViewModel() {

    /** 当前选中的打卡项目 id，null 表示全部项目 */
    private val _selectedTrackerId = MutableStateFlow<Long?>(null)
    val selectedTrackerId: StateFlow<Long?> = _selectedTrackerId

    /** 当前时间视图级别，默认日视图 */
    private val _viewLevel = MutableStateFlow(TimeViewLevel.DAY)
    val viewLevel: StateFlow<TimeViewLevel> = _viewLevel

    /** 当前选中的日期，默认今天 */
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    /**
     * 双指缩放级别，1.0f 为基准
     * - > 1.3f 触发放大（进入更细粒度）
     * - < 0.7f 触发缩小（进入更粗粒度）
     * 切换后自动重置为 1.0f
     */
    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel

    /**
     * 视图级别切换方向标记
     * - true：进入下一级（放大方向），用于动画方向判断
     * - false：返回上一级（缩小方向）
     */
    private val _isZoomingDeeper = MutableStateFlow(true)
    val isZoomingDeeper: StateFlow<Boolean> = _isZoomingDeeper

    /** 全部打卡项目列表 */
    val trackers: StateFlow<List<Tracker>> = getTrackersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 当前视图级别 + 日期对应的日期范围（起始/结束 yyyy-MM-dd） */
    val dateRange: StateFlow<Pair<String, String>> = combine(_viewLevel, _selectedDate) { level, date ->
        calculateDateRange(level, date)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        calculateDateRange(_viewLevel.value, _selectedDate.value)
    )

    /** 当前选中项目 + 日期范围对应的范围统计；未选择项目时为 null */
    val rangeStats: StateFlow<RangeStats?> = combine(
        _selectedTrackerId,
        dateRange
    ) { trackerId, range ->
        if (trackerId != null) {
            getRangeStatsUseCase(trackerId, range.first, range.second).first()
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** 是否可以进入更细粒度（非 MINUTE 级别时为 true） */
    val canGoDeeper: StateFlow<Boolean> = _viewLevel.map { it != TimeViewLevel.MINUTE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** 是否可以返回更粗粒度（非 YEAR 级别时为 true） */
    val canGoBroader: StateFlow<Boolean> = _viewLevel.map { it != TimeViewLevel.YEAR }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** 缩放切换防抖时间戳（毫秒），防止同一手势内连续切换 */
    private var lastZoomSwitchTimeMs: Long = 0L

    /** 缩放切换防抖间隔（毫秒） */
    private val ZOOM_SWITCH_DEBOUNCE_MS = 400L

    /** 放大阈值：缩放因子超过此值时进入更细粒度 */
    private val ZOOM_IN_THRESHOLD = 1.3f

    /** 缩小阈值：缩放因子低于此值时进入更粗粒度 */
    private val ZOOM_OUT_THRESHOLD = 0.7f

    /**
     * 视图层级历史栈（用于返回上一级）。
     * 每次进入更细粒度时压入 (级别, 日期)，返回上一级时弹出。
     */
    private val viewLevelHistory = ArrayDeque<Pair<TimeViewLevel, LocalDate>>()

    /** 选择打卡项目，传 null 表示查看全部 */
    fun selectTracker(id: Long?) {
        _selectedTrackerId.value = id
    }

    /** 切换时间视图级别 */
    fun setViewLevel(level: TimeViewLevel) {
        _viewLevel.value = level
    }

    /** 跳转到指定日期 */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /** 上一段时间（根据视图级别减一段时间） */
    fun previousPeriod() {
        _selectedDate.value = shiftByLevel(_selectedDate.value, _viewLevel.value, -1)
    }

    /** 下一段时间（根据视图级别加一段时间） */
    fun nextPeriod() {
        _selectedDate.value = shiftByLevel(_selectedDate.value, _viewLevel.value, 1)
    }

    /** 回到今天 */
    fun goToToday() {
        _selectedDate.value = LocalDate.now()
    }

    /**
     * 双指缩放回调：根据缩放因子切换 TimeViewLevel
     *
     * - 放大（factor > 1.3）：进入更细粒度（YEAR → MONTH → WEEK → DAY → HOUR → MINUTE）
     * - 缩小（factor < 0.7）：进入更粗粒度（MINUTE → HOUR → DAY → WEEK → MONTH → YEAR）
     * - 边界不超出范围
     * - 带防抖：同一手势内 400ms 内不重复切换
     *
     * @param factor 当前累积缩放因子（0.5 ~ 3.0）
     */
    fun onZoom(factor: Float) {
        val now = System.currentTimeMillis()
        val isDebounced = now - lastZoomSwitchTimeMs > ZOOM_SWITCH_DEBOUNCE_MS

        when {
            // 放大：进入更细粒度
            factor > ZOOM_IN_THRESHOLD && isDebounced && canGoDeeper.value -> {
                _isZoomingDeeper.value = true
                zoomIn()
                lastZoomSwitchTimeMs = now
                _zoomLevel.value = 1.0f
            }
            // 缩小：进入更粗粒度
            factor < ZOOM_OUT_THRESHOLD && isDebounced && canGoBroader.value -> {
                _isZoomingDeeper.value = false
                zoomOut()
                lastZoomSwitchTimeMs = now
                _zoomLevel.value = 1.0f
            }
            // 仅更新缩放级别用于视觉反馈
            else -> {
                _zoomLevel.value = factor
            }
        }
    }

    /**
     * 切换到更细粒度视图（双指外扩触发）。
     * 使用 TimeViewLevel.finer()，已在最细粒度时不切换。
     */
    fun zoomIn() {
        val next = _viewLevel.value.finer()
        if (next != null) {
            _isZoomingDeeper.value = true
            _viewLevel.value = next
        }
    }

    /**
     * 切换到更粗粒度视图（双指内收触发）。
     * 使用 TimeViewLevel.coarser()，已在最粗粒度时不切换。
     */
    fun zoomOut() {
        val prev = _viewLevel.value.coarser()
        if (prev != null) {
            _isZoomingDeeper.value = false
            _viewLevel.value = prev
        }
    }

    /**
     * 进入更细粒度视图
     * YEAR → MONTH → WEEK → DAY → HOUR → MINUTE
     */
    private fun goToFinerLevel() {
        _viewLevel.value.finer()?.let { _viewLevel.value = it }
    }

    /**
     * 进入更粗粒度视图
     * MINUTE → HOUR → DAY → WEEK → MONTH → YEAR
     */
    private fun goToCoarserLevel() {
        _viewLevel.value.coarser()?.let { _viewLevel.value = it }
    }

    /**
     * 点击进入下一级视图
     *
     * 根据当前级别和点击的日期，切换到更细粒度并定位到对应日期：
     * - YEAR 点击月份 → MONTH 视图，日期为该月
     * - MONTH 点击日期 → DAY 视图
     * - WEEK 点击日期 → DAY 视图
     * - DAY 点击小时 → HOUR 视图
     * - HOUR 点击 → MINUTE 视图
     *
     * @param date 被点击的日期
     * @param currentLevel 当前视图级别
     */
    fun onItemClicked(date: LocalDate, currentLevel: TimeViewLevel) {
        enterFinerView(date, currentLevel)
    }

    /**
     * 点击进入下一级（更细粒度）视图。
     *
     * - 记录当前 (级别, 日期) 到历史栈，供 [returnToCoarserView] 返回
     * - 切换到更细粒度并设置目标日期
     * - 已在最细粒度时不切换
     *
     * @param targetDate 被点击的目标日期
     * @param currentLevel 当前视图级别（默认取当前 viewLevel）
     */
    fun enterFinerView(targetDate: LocalDate, currentLevel: TimeViewLevel = _viewLevel.value) {
        val next = currentLevel.finer() ?: return
        // 压入历史栈，便于返回
        viewLevelHistory.addLast(_viewLevel.value to _selectedDate.value)
        _isZoomingDeeper.value = true
        _selectedDate.value = targetDate
        _viewLevel.value = next
    }

    /**
     * 返回上一级视图（更粗粒度）。
     *
     * - 优先从历史栈恢复 (级别, 日期)
     * - 历史栈为空时退回到 [coarser] 级别
     * - 已在最粗粒度时不切换
     */
    fun returnToCoarserView() {
        if (viewLevelHistory.isNotEmpty()) {
            val (level, date) = viewLevelHistory.removeLast()
            _isZoomingDeeper.value = false
            _viewLevel.value = level
            _selectedDate.value = date
        } else {
            zoomOut()
        }
    }

    /**
     * 返回上一级视图（更粗粒度）
     *
     * MINUTE → HOUR → DAY → WEEK → MONTH → YEAR
     */
    fun goBack() {
        returnToCoarserView()
    }

    /**
     * 根据视图级别计算日期范围
     * - 分钟/小时/日：当天
     * - 周：周一到周日
     * - 月：1 日到月末
     * - 年：1 月 1 日到 12 月 31 日
     */
    private fun calculateDateRange(level: TimeViewLevel, date: LocalDate): Pair<String, String> {
        val formatter = DateTimeFormatter.ISO_DATE
        return when (level) {
            TimeViewLevel.MINUTE, TimeViewLevel.HOUR, TimeViewLevel.DAY -> {
                val d = date.format(formatter)
                Pair(d, d)
            }
            TimeViewLevel.WEEK -> {
                val start = date.with(DayOfWeek.MONDAY)
                val end = start.plusDays(6)
                Pair(start.format(formatter), end.format(formatter))
            }
            TimeViewLevel.MONTH -> {
                val start = date.withDayOfMonth(1)
                val end = date.withDayOfMonth(date.lengthOfMonth())
                Pair(start.format(formatter), end.format(formatter))
            }
            TimeViewLevel.YEAR -> {
                val start = date.withDayOfYear(1)
                val end = date.withDayOfYear(date.lengthOfYear())
                Pair(start.format(formatter), end.format(formatter))
            }
        }
    }

    /**
     * 按视图级别对日期进行增减
     * @param steps 步数，正数向后，负数向前
     */
    private fun shiftByLevel(date: LocalDate, level: TimeViewLevel, steps: Int): LocalDate {
        return when (level) {
            TimeViewLevel.MINUTE, TimeViewLevel.HOUR, TimeViewLevel.DAY -> date.plusDays(steps.toLong())
            TimeViewLevel.WEEK -> date.plusWeeks(steps.toLong())
            TimeViewLevel.MONTH -> date.plusMonths(steps.toLong())
            TimeViewLevel.YEAR -> date.plusYears(steps.toLong())
        }
    }
}
