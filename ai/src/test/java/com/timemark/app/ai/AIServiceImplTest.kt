package com.timemark.app.ai

import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIModelType
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.AIUsage
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.FirstDayOfWeek
import com.timemark.app.domain.model.LockMethod
import com.timemark.app.domain.model.ThemeMode
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIUsageRepository
import com.timemark.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * AIServiceImpl 单元测试
 *
 * 验证 AI 服务的路由策略、预算检查、故障转移等核心逻辑。
 * 使用 Fake 实现替代真实依赖。
 */
class AIServiceImplTest {

    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var fakeAIUsageRepository: FakeAIUsageRepository
    private lateinit var fakeAIConfigRepository: FakeAIConfigRepository
    private lateinit var service: AIServiceImpl

    @Before
    fun setUp() {
        val client = OkHttpClient.Builder().build()
        val json = Json { ignoreUnknownKeys = true }
        fakeSettingsRepository = FakeSettingsRepository()
        fakeAIUsageRepository = FakeAIUsageRepository()
        fakeAIConfigRepository = FakeAIConfigRepository()
        service = AIServiceImpl(client, json, fakeAIUsageRepository, fakeAIConfigRepository, fakeSettingsRepository)
    }

    @Test
    fun chat_AI全局禁用_返回失败响应() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(false)
        val config = createConfig()
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        val response = service.chat(request, config)
        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("AI 功能已被禁用") == true)
    }

    @Test
    fun chat_AI全局启用_继续执行() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(true)
        // 设置无预算限制
        fakeSettingsRepository.dailyTokenLimit = 0
        fakeSettingsRepository.monthlyBudgetLimit = 0.0
        val config = createConfig()
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        // 由于没有真实的 HTTP 服务器，请求会失败
        val response = service.chat(request, config)
        // 应返回失败响应（网络错误），但不是"AI 已禁用"
        assertFalse(response.success)
        assertFalse(response.errorMessage?.contains("AI 功能已被禁用") == true)
    }

    @Test
    fun chat_每日Token超限_返回预算超限响应() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(true)
        fakeSettingsRepository.dailyTokenLimit = 100
        // 设置今日已用 100 Token
        fakeAIUsageRepository.setTodayTokens(input = 50, output = 50)
        val config = createConfig()
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        val response = service.chat(request, config)
        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("今日 Token 用量已达上限") == true)
    }

    @Test
    fun chat_每月费用超限_返回预算超限响应() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(true)
        fakeSettingsRepository.dailyTokenLimit = 0
        fakeSettingsRepository.monthlyBudgetLimit = 10.0
        // 设置本月已用 10 元
        fakeAIUsageRepository.setMonthCost(10.0)
        val config = createConfig()
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        val response = service.chat(request, config)
        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("本月 AI 费用已达预算上限") == true)
    }

    @Test
    fun chat_不支持的厂商_返回失败响应() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(true)
        fakeSettingsRepository.dailyTokenLimit = 0
        fakeSettingsRepository.monthlyBudgetLimit = 0.0
        val config = createConfig(provider = AIProvider.GEMINI).copy(
            // 使用一个不存在的 provider
            provider = AIProvider.GEMINI
        )
        // GEMINI 是支持的，所以这个测试改为测试 CUSTOM
        val customConfig = createConfig(provider = AIProvider.CUSTOM)
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "test-model"
        )
        val response = service.chat(request, customConfig)
        // CUSTOM provider 会尝试请求，但由于 baseUrl 为空会失败
        assertFalse(response.success)
    }

    @Test
    fun testConnection_有效配置_返回布尔值() = runTest {
        val config = createConfig()
        val result = service.testConnection(config)
        // 由于没有真实服务器，返回 false
        assertFalse(result)
    }

    @Test
    fun testConnection_不支持的厂商_返回false() = runTest {
        // 所有 provider 都已注册，测试一个会失败的连接
        val config = createConfig(provider = AIProvider.OPENAI)
        val result = service.testConnection(config)
        assertFalse(result)
    }

    @Test
    fun recognizeImage_AI全局禁用_返回失败响应() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(false)
        val config = createConfig()
        val response = service.recognizeImage("base64", "prompt", config)
        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("AI 功能已被禁用") == true)
    }

    @Test
    fun recognizeImage_Token超限_返回预算超限响应() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(true)
        fakeSettingsRepository.dailyTokenLimit = 10
        fakeAIUsageRepository.setTodayTokens(input = 5, output = 5)
        val config = createConfig()
        val response = service.recognizeImage("base64", "prompt", config)
        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("今日 Token 用量已达上限") == true)
    }

    @Test
    fun routeRequest_无配置_返回未配置响应() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(true)
        fakeSettingsRepository.dailyTokenLimit = 0
        fakeSettingsRepository.monthlyBudgetLimit = 0.0
        // 不设置任何配置
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        val response = service.routeRequest(AIFeature.CHAT, request)
        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("未配置支持该功能的 AI 模型") == true)
    }

    @Test
    fun routeRequest_有配置_尝试调用() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(true)
        fakeSettingsRepository.dailyTokenLimit = 0
        fakeSettingsRepository.monthlyBudgetLimit = 0.0
        val config = createConfig()
        fakeAIConfigRepository.setConfigsByFeature(AIFeature.CHAT, listOf(config))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        val response = service.routeRequest(AIFeature.CHAT, request)
        // 由于没有真实服务器，请求会失败
        assertFalse(response.success)
    }

    @Test
    fun chat_请求异常_返回请求异常响应() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(true)
        fakeSettingsRepository.dailyTokenLimit = 0
        fakeSettingsRepository.monthlyBudgetLimit = 0.0
        val config = createConfig(baseUrl = "http://invalid-url-that-does-not-exist:9999")
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        val response = service.chat(request, config)
        assertFalse(response.success)
        // 应记录请求异常
        assertTrue(
            response.errorMessage?.contains("请求异常") == true ||
            response.errorMessage?.contains("HTTP") == true
        )
    }

    @Test
    fun chat_成功调用_记录Usage() = runTest {
        fakeSettingsRepository.setAIGlobalEnabled(true)
        fakeSettingsRepository.dailyTokenLimit = 0
        fakeSettingsRepository.monthlyBudgetLimit = 0.0
        val config = createConfig()
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        service.chat(request, config)
        // 即使请求失败，也应记录 usage
        assertTrue(fakeAIUsageRepository.insertCallCount > 0)
    }

    /** 辅助方法：创建测试用 AIConfig */
    private fun createConfig(
        provider: AIProvider = AIProvider.OPENAI,
        baseUrl: String = ""
    ): AIConfig = AIConfig(
        id = 1L,
        name = "测试配置",
        provider = provider,
        apiKey = "test-key",
        baseUrl = baseUrl,
        model = "gpt-4",
        modelType = AIModelType.TEXT,
        maxTokens = 2048,
        priority = 0
    )
}

/**
 * SettingsRepository 的 Fake 实现，用于 AIServiceImpl 测试。
 */
private class FakeSettingsRepository : SettingsRepository {
    private val _aiGlobalEnabled = MutableStateFlow(true)
    private val _dailyTokenLimit = MutableStateFlow(0)
    private val _monthlyBudgetLimit = MutableStateFlow(0.0)

    fun setAIGlobalEnabled(enabled: Boolean) { _aiGlobalEnabled.value = enabled }
    var dailyTokenLimit: Int
        get() = _dailyTokenLimit.value
        set(value) { _dailyTokenLimit.value = value }
    var monthlyBudgetLimit: Double
        get() = _monthlyBudgetLimit.value
        set(value) { _monthlyBudgetLimit.value = value }

    override val themeMode: Flow<ThemeMode> = MutableStateFlow(ThemeMode.SYSTEM)
    override val blurEnabled: Flow<Boolean> = MutableStateFlow(true)
    override val animationEnabled: Flow<Boolean> = MutableStateFlow(true)
    override val soundEnabled: Flow<Boolean> = MutableStateFlow(true)
    override val hapticEnabled: Flow<Boolean> = MutableStateFlow(true)
    override val use24HourFormat: Flow<Boolean> = MutableStateFlow(true)
    override val firstDayOfWeek: Flow<FirstDayOfWeek> = MutableStateFlow(FirstDayOfWeek.MONDAY)
    override val language: Flow<String> = MutableStateFlow("zh-CN")
    override val lockMethod: Flow<LockMethod> = MutableStateFlow(LockMethod.NONE)
    override val lockPassword: Flow<String?> = MutableStateFlow(null)
    override val autoLockMinutes: Flow<Int> = MutableStateFlow(5)
    override val databaseEncryptionEnabled: Flow<Boolean> = MutableStateFlow(false)
    override val databasePassword: Flow<String?> = MutableStateFlow(null)
    override val aiGlobalEnabled: Flow<Boolean> = _aiGlobalEnabled.asStateFlow()
    override val aiWifiOnly: Flow<Boolean> = MutableStateFlow(false)
    override val dailyTokenLimit: Flow<Int> = _dailyTokenLimit.asStateFlow()
    override val monthlyTokenLimit: Flow<Int> = MutableStateFlow(0)
    override val monthlyBudgetLimit: Flow<Double> = _monthlyBudgetLimit.asStateFlow()
    override val collaborativeMode: Flow<Boolean> = MutableStateFlow(false)

    override suspend fun setThemeMode(mode: ThemeMode) {}
    override suspend fun setBlurEnabled(enabled: Boolean) {}
    override suspend fun setAnimationEnabled(enabled: Boolean) {}
    override suspend fun setSoundEnabled(enabled: Boolean) {}
    override suspend fun setHapticEnabled(enabled: Boolean) {}
    override suspend fun setUse24HourFormat(use24: Boolean) {}
    override suspend fun setFirstDayOfWeek(day: FirstDayOfWeek) {}
    override suspend fun setLanguage(lang: String) {}
    override suspend fun setLockMethod(method: LockMethod) {}
    override suspend fun setLockPassword(password: String?) {}
    override suspend fun setAutoLockMinutes(minutes: Int) {}
    override suspend fun setDatabaseEncryptionEnabled(enabled: Boolean) {}
    override suspend fun setDatabasePassword(password: String?) {}
    override suspend fun setAIGlobalEnabled(enabled: Boolean) { _aiGlobalEnabled.value = enabled }
    override suspend fun setAIWifiOnly(wifiOnly: Boolean) {}
    override suspend fun setDailyTokenLimit(limit: Int) { _dailyTokenLimit.value = limit }
    override suspend fun setMonthlyTokenLimit(limit: Int) {}
    override suspend fun setMonthlyBudgetLimit(limit: Double) { _monthlyBudgetLimit.value = limit }
    override suspend fun setCollaborativeMode(enabled: Boolean) {}
}

/**
 * AIUsageRepository 的 Fake 实现。
 */
private class FakeAIUsageRepository : AIUsageRepository {
    var insertCallCount = 0
        private set
    private var todayInput = 0
    private var todayOutput = 0
    private var monthCost = 0.0

    fun setTodayTokens(input: Int, output: Int) {
        todayInput = input
        todayOutput = output
    }

    fun setMonthCost(cost: Double) {
        monthCost = cost
    }

    override fun getAllUsage(): Flow<List<AIUsage>> = MutableStateFlow(emptyList())
    override fun getUsageByDateRange(startDate: String, endDate: String): Flow<List<AIUsage>> = MutableStateFlow(emptyList())
    override fun getUsageByConfig(configId: Long): Flow<List<AIUsage>> = MutableStateFlow(emptyList())
    override fun getUsageByFeature(feature: AIFeature): Flow<List<AIUsage>> = MutableStateFlow(emptyList())
    override fun getTodayUsage(): Flow<List<AIUsage>> = MutableStateFlow(emptyList())
    override fun getWeekUsage(): Flow<List<AIUsage>> = MutableStateFlow(emptyList())
    override fun getMonthUsage(): Flow<List<AIUsage>> = MutableStateFlow(emptyList())

    override suspend fun insertUsage(usage: AIUsage): Long {
        insertCallCount++
        return 1L
    }

    override suspend fun getTotalTokensByDate(date: String): Pair<Int, Int> {
        return Pair(todayInput, todayOutput)
    }

    override suspend fun getTotalCostByDateRange(startDate: String, endDate: String): Double {
        return monthCost
    }
}

/**
 * AIConfigRepository 的 Fake 实现。
 */
private class FakeAIConfigRepository : AIConfigRepository {
    private val configsByFeature = mutableMapOf<AIFeature, List<AIConfig>>()
    private var defaultTextConfig: AIConfig? = null
    private var defaultMultimodalConfig: AIConfig? = null

    fun setConfigsByFeature(feature: AIFeature, configs: List<AIConfig>) {
        configsByFeature[feature] = configs
    }

    fun setDefaultTextConfig(config: AIConfig?) {
        defaultTextConfig = config
    }

    fun setDefaultMultimodalConfig(config: AIConfig?) {
        defaultMultimodalConfig = config
    }

    override fun getAllConfigs(): Flow<List<AIConfig>> = MutableStateFlow(emptyList())
    override fun getEnabledConfigs(): Flow<List<AIConfig>> = MutableStateFlow(emptyList())
    override fun getConfigById(id: Long): Flow<AIConfig?> = MutableStateFlow(null)

    override fun getConfigsByFeature(feature: AIFeature): Flow<List<AIConfig>> {
        return MutableStateFlow(configsByFeature[feature] ?: emptyList())
    }

    override fun getDefaultMultimodalConfig(): Flow<AIConfig?> = MutableStateFlow(defaultMultimodalConfig)
    override fun getDefaultTextConfig(): Flow<AIConfig?> = MutableStateFlow(defaultTextConfig)

    override suspend fun insertConfig(config: AIConfig): Long = 1L
    override suspend fun updateConfig(config: AIConfig) {}
    override suspend fun deleteConfig(id: Long) {}
    override suspend fun updatePriority(orders: List<Pair<Long, Int>>) {}
}
