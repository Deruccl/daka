package com.timemark.app.feature.ai.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.Result
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.ExerciseAnalysis
import com.timemark.app.domain.model.HabitAnalysis
import com.timemark.app.domain.model.SleepAnalysis
import com.timemark.app.domain.model.WaterIntakeAnalysis
import com.timemark.app.domain.repository.RecordRepository
import com.timemark.app.domain.repository.TrackerRepository
import com.timemark.app.domain.usecase.ai.AnalyzeExerciseUseCase
import com.timemark.app.domain.usecase.ai.AnalyzeHabitUseCase
import com.timemark.app.domain.usecase.ai.AnalyzeSleepUseCase
import com.timemark.app.domain.usecase.ai.AnalyzeWaterIntakeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * AI 分析 ViewModel
 *
 * 管理分析类型选择、分析请求与结果展示。
 * 自动从本地数据库读取最近 7 天的打卡记录作为分析输入。
 */
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analyzeWaterIntakeUseCase: AnalyzeWaterIntakeUseCase,
    private val analyzeExerciseUseCase: AnalyzeExerciseUseCase,
    private val analyzeSleepUseCase: AnalyzeSleepUseCase,
    private val analyzeHabitUseCase: AnalyzeHabitUseCase,
    private val recordRepository: RecordRepository,
    private val trackerRepository: TrackerRepository
) : ViewModel() {

    /** 分析状态 */
    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    /** 最近一次分析结果（用于结果页展示） */
    private val _lastResult = MutableStateFlow<AnalysisResultData?>(null)
    val lastResult: StateFlow<AnalysisResultData?> = _lastResult.asStateFlow()

    /**
     * 触发指定类型的分析
     */
    fun analyze(feature: AIFeature) {
        if (_analysisState.value is AnalysisState.Loading) return
        _analysisState.value = AnalysisState.Loading
        viewModelScope.launch {
            // 收集最近 7 天的打卡记录作为分析输入
            val records = collectRecentRecords()
            if (records.isEmpty()) {
                _analysisState.value = AnalysisState.Error("最近 7 天没有打卡记录，无法分析")
                return@launch
            }

            val result: Result<*> = when (feature) {
                AIFeature.WATER_ANALYSIS -> analyzeWaterIntakeUseCase(records)
                AIFeature.EXERCISE_ANALYSIS -> analyzeExerciseUseCase(records)
                AIFeature.SLEEP_ANALYSIS -> analyzeSleepUseCase(records)
                AIFeature.HABIT_ANALYSIS -> analyzeHabitUseCase(records)
                else -> {
                    _analysisState.value = AnalysisState.Error("不支持的分析类型")
                    return@launch
                }
            }

            when (result) {
                is Result.Success -> {
                    val data = AnalysisResultData(feature, result.data)
                    _lastResult.value = data
                    _analysisState.value = AnalysisState.Success(data)
                }
                is Result.Error -> {
                    _analysisState.value = AnalysisState.Error(result.message)
                }
                is Result.Loading -> {
                    // 不会发生
                }
            }
        }
    }

    /** 重置状态 */
    fun reset() {
        _analysisState.value = AnalysisState.Idle
    }

    /**
     * 收集最近 7 天的打卡记录，转为字符串列表
     * 格式："yyyy-MM-dd HH:mm 习惯名 值 单位"
     */
    private suspend fun collectRecentRecords(): List<String> {
        val today = LocalDate.now()
        val startDate = today.minusDays(6)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val trackers = trackerRepository.getAllTrackers().first().associateBy { it.id }
        val records = recordRepository.getAllRecordsByDateRange(
            startDate.format(formatter),
            today.format(formatter)
        ).first()

        return records.mapNotNull { record ->
            val tracker = trackers[record.trackerId] ?: return@mapNotNull null
            "${record.date} ${record.time} ${tracker.name} ${record.value} ${tracker.unit}".trim()
        }
    }

    /** 分析状态密封类 */
    sealed class AnalysisState {
        /** 空闲 */
        object Idle : AnalysisState()
        /** 加载中 */
        object Loading : AnalysisState()
        /** 成功 */
        data class Success(val data: AnalysisResultData) : AnalysisState()
        /** 失败 */
        data class Error(val message: String) : AnalysisState()
    }

    /** 分析结果数据包装 */
    data class AnalysisResultData(
        val feature: AIFeature,
        val result: Any
    )
}
