package com.timemark.app.feature.ai

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.PasswordTextField
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTextField
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIModelType
import com.timemark.app.domain.model.ProxyConfig
import com.timemark.app.feature.ai.config.AIConfigViewModel

/**
 * 编辑 AI 配置页面
 *
 * 预填已有配置，支持修改所有字段并保存。
 * 通过 [AIConfigViewModel] 加载与持久化。
 *
 * @param navController 导航控制器
 * @param configId 待编辑的 AI 配置 ID
 */
@Composable
fun AIConfigEditScreen(navController: NavController, configId: Long) {
    val viewModel: AIConfigViewModel = hiltViewModel()
    val configs by viewModel.configs.collectAsStateWithLifecycle()
    val testStatus by viewModel.testStatus.collectAsStateWithLifecycle()

    // 找到对应配置
    val config = configs.firstOrNull { it.id == configId }

    // 表单字段（首次加载时初始化）
    var initialized by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var modelType by remember { mutableStateOf(AIModelType.TEXT) }
    var priceInput by remember { mutableStateOf("0.0") }
    var priceOutput by remember { mutableStateOf("0.0") }
    var rateLimit by remember { mutableStateOf("0") }
    var maxTokens by remember { mutableStateOf("4096") }
    var enabled by remember { mutableStateOf(true) }
    val applicableFeatures = remember { mutableStateOf<Set<AIFeature>>(emptySet()) }

    // Task 36.2: 代理配置状态
    var proxyEnabled by remember { mutableStateOf(false) }
    var proxyHost by remember { mutableStateOf("") }
    var proxyPort by remember { mutableStateOf("0") }
    var proxyUsername by remember { mutableStateOf("") }
    var proxyPassword by remember { mutableStateOf("") }

    // 配置加载后初始化表单
    LaunchedEffect(config) {
        if (config != null && !initialized) {
            name = config.name
            apiKey = config.apiKey
            baseUrl = config.baseUrl
            model = config.model
            modelType = config.modelType
            priceInput = config.priceInput.toString()
            priceOutput = config.priceOutput.toString()
            rateLimit = config.rateLimitPerMinute.toString()
            maxTokens = config.maxTokens.toString()
            enabled = config.enabled
            applicableFeatures.value = config.applicableFeatures.toSet()
            // Task 36.2: 初始化代理配置
            proxyEnabled = config.proxyConfig?.enabled ?: false
            proxyHost = config.proxyConfig?.host ?: ""
            proxyPort = (config.proxyConfig?.port ?: 0).toString()
            proxyUsername = config.proxyConfig?.username ?: ""
            proxyPassword = config.proxyConfig?.password ?: ""
            initialized = true
        }
    }

    /**
     * Task 36.2: 根据表单状态构造 ProxyConfig
     * 代理未启用或地址为空时返回 null
     */
    fun buildProxyConfig(): ProxyConfig? {
        if (!proxyEnabled || proxyHost.isBlank()) return null
        return ProxyConfig(
            host = proxyHost.trim(),
            port = proxyPort.toIntOrNull() ?: 0,
            username = proxyUsername.ifBlank { null },
            password = proxyPassword.ifBlank { null },
            enabled = true
        )
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "编辑 AI 配置",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        if (config == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 厂商信息（只读）
            GlassCard(level = GlassLevel.LIGHT, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "厂商",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.providerDisplayName(config.provider),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 基本信息
            GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("配置名称", style = MaterialTheme.typography.labelMedium)
                    GlassTextField(value = name, onValueChange = { name = it }, placeholder = "配置名称")

                    Text("API Key", style = MaterialTheme.typography.labelMedium)
                    // 编辑时默认显示占位符（••••••••），用户点击修改时才显示实际内容
                    PasswordTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        placeholder = "sk-...",
                        placeholderMode = true
                    )

                    Text("Base URL", style = MaterialTheme.typography.labelMedium)
                    GlassTextField(value = baseUrl, onValueChange = { baseUrl = it }, placeholder = "https://api.openai.com")

                    Text("模型名称", style = MaterialTheme.typography.labelMedium)
                    GlassTextField(value = model, onValueChange = { model = it }, placeholder = "模型名")

                    Text("模型类型", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AIModelType.values().forEach { type ->
                            GlassButton(
                                text = when (type) {
                                    AIModelType.TEXT -> "文本"
                                    AIModelType.MULTIMODAL -> "多模态"
                                    AIModelType.VOICE -> "语音"
                                },
                                onClick = { modelType = type },
                                type = if (modelType == type) GlassButtonType.PRIMARY else GlassButtonType.SECONDARY
                            )
                        }
                    }
                }
            }

            // 高级设置
            GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("输入价格（每 1K Token）", style = MaterialTheme.typography.labelMedium)
                    GlassTextField(value = priceInput, onValueChange = { priceInput = it }, placeholder = "0.0")

                    Text("输出价格（每 1K Token）", style = MaterialTheme.typography.labelMedium)
                    GlassTextField(value = priceOutput, onValueChange = { priceOutput = it }, placeholder = "0.0")

                    Text("速率限制（每分钟）", style = MaterialTheme.typography.labelMedium)
                    GlassTextField(value = rateLimit, onValueChange = { rateLimit = it }, placeholder = "0")

                    Text("最大 Token 数", style = MaterialTheme.typography.labelMedium)
                    GlassTextField(value = maxTokens, onValueChange = { maxTokens = it }, placeholder = "4096")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("启用状态")
                        GlassButton(
                            text = if (enabled) "已启用" else "已禁用",
                            onClick = { enabled = !enabled },
                            type = if (enabled) GlassButtonType.PRIMARY else GlassButtonType.SECONDARY
                        )
                    }
                }
            }

            // Task 36.2: HTTP 代理配置
            GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("HTTP 代理", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                    // 代理开关
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("启用代理", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "通过代理服务器访问 API",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = proxyEnabled,
                            onCheckedChange = { proxyEnabled = it }
                        )
                    }

                    // 代理详情表单（仅在启用时显示）
                    if (proxyEnabled) {
                        Text("代理地址", style = MaterialTheme.typography.labelMedium)
                        GlassTextField(value = proxyHost, onValueChange = { proxyHost = it }, placeholder = "如 127.0.0.1")

                        Text("代理端口", style = MaterialTheme.typography.labelMedium)
                        GlassTextField(value = proxyPort, onValueChange = { proxyPort = it }, placeholder = "如 7890")

                        Text("用户名（可选）", style = MaterialTheme.typography.labelMedium)
                        GlassTextField(value = proxyUsername, onValueChange = { proxyUsername = it }, placeholder = "代理认证用户名")

                        Text("密码（可选）", style = MaterialTheme.typography.labelMedium)
                        // Task 36.1: 代理密码也使用密码模式输入
                        PasswordTextField(
                            value = proxyPassword,
                            onValueChange = { proxyPassword = it },
                            placeholder = "代理认证密码"
                        )
                    }
                }
            }

            // 适用功能
            GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("适用功能", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    AIFeature.values().forEach { feature ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(viewModel.featureDisplayName(feature))
                            GlassButton(
                                text = if (feature in applicableFeatures.value) "已选" else "选择",
                                onClick = {
                                    applicableFeatures.value = if (feature in applicableFeatures.value) {
                                        applicableFeatures.value - feature
                                    } else {
                                        applicableFeatures.value + feature
                                    }
                                },
                                type = if (feature in applicableFeatures.value) GlassButtonType.PRIMARY else GlassButtonType.SECONDARY
                            )
                        }
                    }
                }
            }

            // 测试连接
            GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (testStatus) {
                        is AIConfigViewModel.TestStatus.Idle -> {
                            GlassButton(
                                text = "测试连接",
                                onClick = {
                                    val testConfig = config.copy(
                                        name = name,
                                        apiKey = apiKey,
                                        baseUrl = baseUrl,
                                        model = model,
                                        modelType = modelType,
                                        proxyConfig = buildProxyConfig()
                                    )
                                    viewModel.testConnection(testConfig)
                                }
                            )
                        }
                        is AIConfigViewModel.TestStatus.Testing -> {
                            CircularProgressIndicator()
                            Text("测试中...")
                        }
                        is AIConfigViewModel.TestStatus.Success -> {
                            Text("✓ ${testStatus.message}", color = MaterialTheme.colorScheme.primary)
                            GlassButton(
                                text = "重新测试",
                                onClick = {
                                    val testConfig = config.copy(
                                        name = name,
                                        apiKey = apiKey,
                                        baseUrl = baseUrl,
                                        model = model,
                                        modelType = modelType,
                                        proxyConfig = buildProxyConfig()
                                    )
                                    viewModel.testConnection(testConfig)
                                },
                                type = GlassButtonType.SECONDARY
                            )
                        }
                        is AIConfigViewModel.TestStatus.Failed -> {
                            Text("✗ ${testStatus.message}", color = MaterialTheme.colorScheme.error)
                            GlassButton(
                                text = "重新测试",
                                onClick = {
                                    val testConfig = config.copy(
                                        name = name,
                                        apiKey = apiKey,
                                        baseUrl = baseUrl,
                                        model = model,
                                        modelType = modelType,
                                        proxyConfig = buildProxyConfig()
                                    )
                                    viewModel.testConnection(testConfig)
                                },
                                type = GlassButtonType.SECONDARY
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 底部操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassButton(
                    text = "取消",
                    onClick = { navController.popBackStack() },
                    type = GlassButtonType.SECONDARY,
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "保存",
                    onClick = {
                        val updated = config.copy(
                            name = name,
                            apiKey = apiKey,
                            baseUrl = baseUrl,
                            model = model,
                            modelType = modelType,
                            priceInput = priceInput.toDoubleOrNull() ?: 0.0,
                            priceOutput = priceOutput.toDoubleOrNull() ?: 0.0,
                            rateLimitPerMinute = rateLimit.toIntOrNull() ?: 0,
                            maxTokens = maxTokens.toIntOrNull() ?: 4096,
                            enabled = enabled,
                            applicableFeatures = applicableFeatures.value.toList(),
                            proxyConfig = buildProxyConfig(),
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.saveConfig(updated)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
