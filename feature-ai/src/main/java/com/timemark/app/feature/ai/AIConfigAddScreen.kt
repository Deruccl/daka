package com.timemark.app.feature.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIModelType
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.feature.ai.config.AIConfigViewModel

/**
 * 新增 AI 配置页面
 *
 * 分步流程：
 * - Step 1: 选择厂商
 * - Step 2: 填写 API 信息（Key、Base URL、模型名、类型）
 * - Step 3: 测试连接
 * - Step 4: 高级设置（价格、速率限制、最大 Token）
 */
@Composable
fun AIConfigAddScreen(navController: NavController) {
    val viewModel: AIConfigViewModel = hiltViewModel()
    val testStatus by viewModel.testStatus.collectAsStateWithLifecycle()

    var currentStep by remember { mutableStateOf(0) }

    // 表单字段
    var name by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf(AIProvider.OPENAI) }
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

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "新增 AI 配置 (${currentStep + 1}/4)",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 步骤内容
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (currentStep) {
                    0 -> StepProviderSelect(
                        provider = provider,
                        onSelect = { provider = it }
                    )
                    1 -> StepApiInfo(
                        name = name, onNameChange = { name = it },
                        apiKey = apiKey, onApiKeyChange = { apiKey = it },
                        baseUrl = baseUrl, onBaseUrlChange = { baseUrl = it },
                        model = model, onModelChange = { model = it },
                        modelType = modelType, onModelTypeChange = { modelType = it }
                    )
                    2 -> StepTestConnection(
                        provider = provider,
                        apiKey = apiKey,
                        baseUrl = baseUrl,
                        model = model,
                        testStatus = testStatus,
                        onTest = {
                            val config = AIConfig(
                                id = 0,
                                name = name,
                                provider = provider,
                                apiKey = apiKey,
                                baseUrl = baseUrl,
                                model = model,
                                modelType = modelType
                            )
                            viewModel.testConnection(config)
                        }
                    )
                    3 -> StepAdvanced(
                        priceInput = priceInput, onPriceInputChange = { priceInput = it },
                        priceOutput = priceOutput, onPriceOutputChange = { priceOutput = it },
                        rateLimit = rateLimit, onRateLimitChange = { rateLimit = it },
                        maxTokens = maxTokens, onMaxTokensChange = { maxTokens = it },
                        enabled = enabled, onEnabledChange = { enabled = it },
                        applicableFeatures = applicableFeatures.value,
                        onFeatureToggle = { feature ->
                            applicableFeatures.value = if (feature in applicableFeatures.value) {
                                applicableFeatures.value - feature
                            } else {
                                applicableFeatures.value + feature
                            }
                        },
                        viewModel = viewModel
                    )
                }
            }

            // 底部导航
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    GlassButton(
                        text = "上一步",
                        onClick = {
                            viewModel.resetTestStatus()
                            currentStep--
                        },
                        type = GlassButtonType.SECONDARY
                    )
                }
                if (currentStep < 3) {
                    GlassButton(
                        text = "下一步",
                        onClick = { currentStep++ }
                    )
                } else {
                    GlassButton(
                        text = "保存配置",
                        onClick = {
                            val config = AIConfig(
                                id = 0,
                                name = name.ifBlank { provider.name },
                                provider = provider,
                                apiKey = apiKey,
                                baseUrl = baseUrl,
                                model = model,
                                modelType = modelType,
                                priceInput = priceInput.toDoubleOrNull() ?: 0.0,
                                priceOutput = priceOutput.toDoubleOrNull() ?: 0.0,
                                rateLimitPerMinute = rateLimit.toIntOrNull() ?: 0,
                                maxTokens = maxTokens.toIntOrNull() ?: 4096,
                                enabled = enabled,
                                priority = 0,
                                applicableFeatures = applicableFeatures.value.toList()
                            )
                            viewModel.saveConfig(config)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

/** Step 1: 选择厂商 */
@Composable
private fun StepProviderSelect(
    provider: AIProvider,
    onSelect: (AIProvider) -> Unit
) {
    Text(
        text = "选择 AI 厂商",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    val providers = listOf(
        AIProvider.OPENAI to "OpenAI GPT",
        AIProvider.ANTHROPIC to "Anthropic Claude",
        AIProvider.GEMINI to "Google Gemini",
        AIProvider.BAIDU to "百度文心一言",
        AIProvider.ALIBABA to "阿里通义千问",
        AIProvider.BYTEDANCE to "字节豆包",
        AIProvider.ZHIPU to "智谱 GLM",
        AIProvider.MOONSHOT to "Moonshot Kimi",
        AIProvider.OLLAMA to "Ollama 本地",
        AIProvider.CUSTOM to "自定义（OpenAI 兼容）"
    )
    providers.forEach { (p, label) ->
        GlassCard(
            level = if (provider == p) GlassLevel.THICK else GlassLevel.LIGHT,
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSelect(p) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (provider == p) FontWeight.SemiBold else FontWeight.Normal
                )
                if (provider == p) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/** Step 2: 填写 API 信息 */
@Composable
private fun StepApiInfo(
    name: String, onNameChange: (String) -> Unit,
    apiKey: String, onApiKeyChange: (String) -> Unit,
    baseUrl: String, onBaseUrlChange: (String) -> Unit,
    model: String, onModelChange: (String) -> Unit,
    modelType: AIModelType, onModelTypeChange: (AIModelType) -> Unit
) {
    Text(
        text = "API 信息",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("配置名称", style = MaterialTheme.typography.labelMedium)
            GlassTextField(value = name, onValueChange = onNameChange, placeholder = "如：我的 OpenAI")

            Text("API Key", style = MaterialTheme.typography.labelMedium)
            PasswordTextField(value = apiKey, onValueChange = onApiKeyChange, placeholder = "sk-...")

            Text("Base URL（可选）", style = MaterialTheme.typography.labelMedium)
            GlassTextField(value = baseUrl, onValueChange = onBaseUrlChange, placeholder = "https://api.openai.com")

            Text("模型名称", style = MaterialTheme.typography.labelMedium)
            GlassTextField(value = model, onValueChange = onModelChange, placeholder = "如：gpt-4o-mini")

            Text("模型类型", style = MaterialTheme.typography.labelMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AIModelType.values().forEach { type ->
                    GlassButton(
                        text = when (type) {
                            AIModelType.TEXT -> "文本"
                            AIModelType.MULTIMODAL -> "多模态"
                            AIModelType.VOICE -> "语音"
                        },
                        onClick = { onModelTypeChange(type) },
                        type = if (modelType == type) GlassButtonType.PRIMARY else GlassButtonType.SECONDARY
                    )
                }
            }
        }
    }
}

/** Step 3: 测试连接 */
@Composable
private fun StepTestConnection(
    provider: AIProvider,
    apiKey: String,
    baseUrl: String,
    model: String,
    testStatus: AIConfigViewModel.TestStatus,
    onTest: () -> Unit
) {
    Text(
        text = "测试连接",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("厂商: $provider")
            Text("模型: $model")
            if (baseUrl.isNotBlank()) Text("Base URL: $baseUrl")
            Spacer(modifier = Modifier.height(8.dp))
            when (testStatus) {
                is AIConfigViewModel.TestStatus.Idle -> {
                    GlassButton(text = "开始测试", onClick = onTest)
                }
                is AIConfigViewModel.TestStatus.Testing -> {
                    CircularProgressIndicator()
                    Text("测试中...")
                }
                is AIConfigViewModel.TestStatus.Success -> {
                    Text("✓ ${testStatus.message}", color = MaterialTheme.colorScheme.primary)
                    GlassButton(text = "重新测试", onClick = onTest, type = GlassButtonType.SECONDARY)
                }
                is AIConfigViewModel.TestStatus.Failed -> {
                    Text("✗ ${testStatus.message}", color = MaterialTheme.colorScheme.error)
                    GlassButton(text = "重新测试", onClick = onTest, type = GlassButtonType.SECONDARY)
                }
            }
        }
    }
}

/** Step 4: 高级设置 */
@Composable
private fun StepAdvanced(
    priceInput: String, onPriceInputChange: (String) -> Unit,
    priceOutput: String, onPriceOutputChange: (String) -> Unit,
    rateLimit: String, onRateLimitChange: (String) -> Unit,
    maxTokens: String, onMaxTokensChange: (String) -> Unit,
    enabled: Boolean, onEnabledChange: (Boolean) -> Unit,
    applicableFeatures: Set<AIFeature>,
    onFeatureToggle: (AIFeature) -> Unit,
    viewModel: AIConfigViewModel
) {
    Text(
        text = "高级设置",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("输入价格（每 1K Token，美元）", style = MaterialTheme.typography.labelMedium)
            GlassTextField(value = priceInput, onValueChange = onPriceInputChange, placeholder = "0.0")

            Text("输出价格（每 1K Token，美元）", style = MaterialTheme.typography.labelMedium)
            GlassTextField(value = priceOutput, onValueChange = onPriceOutputChange, placeholder = "0.0")

            Text("速率限制（每分钟请求数）", style = MaterialTheme.typography.labelMedium)
            GlassTextField(value = rateLimit, onValueChange = onRateLimitChange, placeholder = "0")

            Text("最大 Token 数", style = MaterialTheme.typography.labelMedium)
            GlassTextField(value = maxTokens, onValueChange = onMaxTokensChange, placeholder = "4096")

            Text("启用状态", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (enabled) "已启用" else "已禁用")
                GlassButton(
                    text = if (enabled) "禁用" else "启用",
                    onClick = { onEnabledChange(!enabled) },
                    type = GlassButtonType.SMALL
                )
            }
        }
    }

    Text(
        text = "适用功能",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    GlassCard(level = GlassLevel.STANDARD, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AIFeature.values().forEach { feature ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(viewModel.featureDisplayName(feature))
                    GlassButton(
                        text = if (feature in applicableFeatures) "已选" else "选择",
                        onClick = { onFeatureToggle(feature) },
                        type = if (feature in applicableFeatures) GlassButtonType.PRIMARY else GlassButtonType.SECONDARY
                    )
                }
            }
        }
    }
}
