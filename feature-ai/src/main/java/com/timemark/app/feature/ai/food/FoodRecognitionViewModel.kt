package com.timemark.app.feature.ai.food

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.Result
import com.timemark.app.domain.model.FoodRecognitionResult
import com.timemark.app.domain.usecase.ai.RecognizeFoodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 食物识别 ViewModel
 *
 * 管理图片选择、识别状态与识别结果。
 */
@HiltViewModel
class FoodRecognitionViewModel @Inject constructor(
    private val recognizeFoodUseCase: RecognizeFoodUseCase
) : ViewModel() {

    /** 选中的图片路径 */
    private val _imagePath = MutableStateFlow<String?>(null)
    val imagePath: StateFlow<String?> = _imagePath.asStateFlow()

    /** 识别状态 */
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()

    /** 选择图片 */
    fun selectImage(path: String) {
        _imagePath.value = path
        _recognitionState.value = RecognitionState.Idle
    }

    /** 清除图片 */
    fun clearImage() {
        _imagePath.value = null
        _recognitionState.value = RecognitionState.Idle
    }

    /** 开始识别 */
    fun recognize() {
        val path = _imagePath.value ?: return
        _recognitionState.value = RecognitionState.Loading

        viewModelScope.launch {
            val result = recognizeFoodUseCase(path)
            _recognitionState.value = when (result) {
                is Result.Success -> RecognitionState.Success(result.data)
                is Result.Error -> RecognitionState.Error(result.message)
                is Result.Loading -> RecognitionState.Loading
            }
        }
    }

    /** 重置识别状态 */
    fun reset() {
        _recognitionState.value = RecognitionState.Idle
    }

    /** 识别状态 */
    sealed class RecognitionState {
        object Idle : RecognitionState()
        object Loading : RecognitionState()
        data class Success(val result: FoodRecognitionResult) : RecognitionState()
        data class Error(val message: String) : RecognitionState()
    }
}
