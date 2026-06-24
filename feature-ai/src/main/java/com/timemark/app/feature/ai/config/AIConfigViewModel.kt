package com.timemark.app.feature.ai.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIModelType
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.AIUsage
import com.timemark.app.domain.repository.AIService
import com.timemark.app.domain.usecase.ai.DeleteAIConfigUseCase
import com.timemark.app.domain.usecase.ai.GetAIConfigsUseCase
import com.timemark.app.domain.usecase.ai.GetAIUsageUseCase
import com.timemark.app.domain.usecase.ai.SaveAIConfigUseCase
import com.timemark.app.domain.usecase.settings.GetSettingsUseCase
import com.timemark.app.domain.usecase.settings.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI 配置 ViewModel
 *
 * 管理 AI 配置列表、Token 用量统计、全局开关与协同模式开关。
 * 提供保存、删除、上下移动（调整优先级）、测试连接等操作。
 */
@HiltViewModel
class AIConfigViewModel @Inject constructor(
    private val getAIConfigsUseCase: GetAIConfigsUseCase,
    private val saveAIConfigUseCase: SaveAIConfigUseCase,
    private val deleteAIConfigUseCase: DeleteAIConfigUseCase,
    private val getAIUsageUseCase: GetAIUsageUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val aiService: AIService
) : ViewModel() {

    /** 全部 AI 配置列表 */
    val configs: StateFlow<List<AIConfig>> = getAIConfigsUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /** 今日 Token 用量 */
    val todayUsage: StateFlow<List<AIUsage>> = getAIUsageUseCase.today()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /** 本周 Token 用量 */
    val weekUsage: StateFlow<List<AIUsage>> = getAIUsageUseCase.week()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /** 本月 Token 用量 */
    val monthUsage: StateFlow<List<AIUsage>> = getAIUsageUseCase.month()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /** AI 全局开关 */
    val aiGlobalEnabled: StateFlow<Boolean> = getSettingsUseCase.aiGlobalEnabled()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true
        )

    /** 协同模式开关 */
    val collaborativeMode: StateFlow<Boolean> = getSettingsUseCase.collaborativeMode()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    /** 今日 Token 总量（输入+输出） */
    val todayTokenTotal: StateFlow<Int> = todayUsage
        .map { list -> list.sumOf { it.tokensInput + it.tokensOutput } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** 今日费用估算 */
    val todayCost: StateFlow<Double> = todayUsage
        .map { list -> list.sumOf { it.cost } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** 本月费用估算 */
    val monthCost: StateFlow<Double> = monthUsage
        .map { list -> list.sumOf { it.cost } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** 测试连接状态：null 未测试 / true 成功 / false 失败 */
    private val _testStatus = MutableStateFlow<TestStatus>(TestStatus.Idle)
    val testStatus: StateFlow<TestStatus> = _testStatus

    /** 保存配置（新增或更新） */
    fun saveConfig(config: AIConfig) {
        viewModelScope.launch {
            saveAIConfigUseCase(config)
        }
    }

    /** 删除配置 */
    fun deleteConfig(id: Long) {
        viewModelScope.launch {
            deleteAIConfigUseCase(id)
        }
    }

    /** 切换全局 AI 开关 */
    fun setAIGlobalEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.aiGlobalEnabled(enabled)
        }
    }

    /** 切换协同模式开关 */
    fun setCollaborativeMode(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.collaborativeMode(enabled)
        }
    }

    /** 上移（降低 priority 数值） */
    fun moveUp(config: AIConfig) {
        val list = configs.value
        val sorted = list.sortedBy { it.priority }
        val index = sorted.indexOfFirst { it.id == config.id }
        if (index <= 0) return
        viewModelScope.launch {
            val prev = sorted[index - 1]
            saveAIConfigUseCase(prev.copy(priority = config.priority, updatedAt = System.currentTimeMillis()))
            saveAIConfigUseCase(config.copy(priority = prev.priority, updatedAt = System.currentTimeMillis()))
        }
    }

    /** 下移（升高 priority 数值） */
    fun moveDown(config: AIConfig) {
        val list = configs.value
        val sorted = list.sortedBy { it.priority }
        val index = sorted.indexOfFirst { it.id == config.id }
        if (index < 0 || index >= sorted.size - 1) return
        viewModelScope.launch {
            val next = sorted[index + 1]
            saveAIConfigUseCase(next.copy(priority = config.priority, updatedAt = System.currentTimeMillis()))
            saveAIConfigUseCase(config.copy(priority = next.priority, updatedAt = System.currentTimeMillis()))
        }
    }

    /** 切换启用状态 */
    fun toggleEnabled(config: AIConfig) {
        viewModelScope.launch {
            saveAIConfigUseCase(config.copy(enabled = !config.enabled, updatedAt = System.currentTimeMillis()))
        }
    }

    /** 测试连接 */
    fun testConnection(config: AIConfig) {
        viewModelScope.launch {
            _testStatus.value = TestStatus.Testing
            val ok = runCatching { aiService.testConnection(config) }.getOrDefault(false)
            _testStatus.value = if (ok) TestStatus.Success else TestStatus.Failed("连接失败")
        }
    }

    /** 重置测试状态 */
    fun resetTestStatus() {
        _testStatus.value = TestStatus.Idle
    }

    /** 测试状态 */
    sealed class TestStatus {
        object Idle : TestStatus()
        object Testing : TestStatus()
        data class Success(val message: String = "连接成功") : TestStatus()
        data class Failed(val message: String) : TestStatus()
    }

    /** 厂商显示名 */
    fun providerDisplayName(provider: AIProvider): String = when (provider) {
        AIProvider.OPENAI -> "OpenAI"
        AIProvider.ANTHROPIC -> "Anthropic"
        AIProvider.GEMINI -> "Google Gemini"
        AIProvider.BAIDU -> "百度文心"
        AIProvider.ALIBABA -> "阿里通义"
        AIProvider.BYTEDANCE -> "字节豆包"
        AIProvider.ZHIPU -> "智谱 GLM"
        AIProvider.MOONSHOT -> "Moonshot Kimi"
        AIProvider.OLLAMA -> "Ollama 本地"
        AIProvider.CUSTOM -> "自定义"
    }

    /** 模型类型显示名 */
    fun modelTypeDisplayName(type: AIModelType): String = when (type) {
        AIModelType.TEXT -> "文本"
        AIModelType.MULTIMODAL -> "多模态"
        AIModelType.VOICE -> "语音"
    }

    /** 功能显示名 */
    fun featureDisplayName(feature: AIFeature): String = when (feature) {
        AIFeature.FOOD_RECOGNITION -> "食物识别"
        AIFeature.NUTRITION_ANALYSIS -> "营养分析"
        AIFeature.WATER_ANALYSIS -> "饮水分析"
        AIFeature.EXERCISE_ANALYSIS -> "运动分析"
        AIFeature.SLEEP_ANALYSIS -> "睡眠分析"
        AIFeature.HABIT_ANALYSIS -> "习惯分析"
        AIFeature.CHAT -> "AI 聊天"
        AIFeature.REPORT -> "报告生成"
    }
}
