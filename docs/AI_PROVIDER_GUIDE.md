# AI Provider 扩展指南

> 本文档介绍「时光印记 TimeMark」的 AI Provider 架构、已支持的 10 个厂商、以及如何新增自定义 AI Provider。

## 目录

- [AI Provider 架构说明](#ai-provider-架构说明)
- [已支持 Provider 列表](#已支持-provider-列表)
- [新增 Provider 步骤](#新增-provider-步骤)
- [请求/响应格式说明](#请求响应格式说明)
- [多模态支持说明](#多模态支持说明)
- [错误处理规范](#错误处理规范)
- [测试 Provider 的方法](#测试-provider-的方法)
- [配置示例](#配置示例)

---

## AI Provider 架构说明

### 设计理念

时光印记采用 **Provider 模式** 统一不同 AI 厂商的接口差异。每个厂商实现统一的 `AIProvider` 接口，将应用内部的 `ChatRequest` 转换为厂商特定的 HTTP 请求，并将响应解析为统一的 `ChatResponse`。

### 架构图

```
┌─────────────────────────────────────────────────────┐
│              domain 层                               │
│  AIService 接口 (chat/recognizeImage/testConnection)│
│  ChatRequest / ChatResponse / AIConfig              │
└───────────────────────┬─────────────────────────────┘
                        │ @Binds (Hilt)
                        ▼
┌─────────────────────────────────────────────────────┐
│              ai 层                                   │
│              AIServiceImpl                          │
│  ┌─────────────────────────────────────────────┐   │
│  │  providers: Map<AIProvider, AIProvider>     │   │
│  │  - 检查全局开关                              │   │
│  │  - 检查预算限制                              │   │
│  │  - 调用对应 Provider                         │   │
│  │  - 记录 Token 用量                           │   │
│  │  - 故障转移                                  │   │
│  └──────────────────┬──────────────────────────┘   │
└─────────────────────┼───────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        ▼             ▼             ▼
   ┌─────────┐  ┌─────────┐  ┌─────────┐
   │BaseProvider│ │OpenAI  │  │Anthropic│  ...
   │(抽象基类) │  │Provider│  │Provider │
   └─────────┘  └─────────┘  └─────────┘
```

### 核心接口

```kotlin
// ai/provider/AIProvider.kt
interface AIProvider {

    /** 厂商类型 */
    val providerType: AIProvider

    /** 文本对话 */
    suspend fun chat(request: ChatRequest, config: AIConfig): ChatResponse

    /** 图片识别（多模态） */
    suspend fun recognizeImage(imageBase64: String, prompt: String, config: AIConfig): ChatResponse

    /** 连接测试 */
    suspend fun testConnection(config: AIConfig): Boolean
}
```

### 基类 BaseProvider

`BaseProvider` 是抽象基类，提供通用的 HTTP 请求与 JSON 解析方法，各具体 Provider 复用：

| 方法 | 用途 |
|------|------|
| `postJson(url, apiKey, body, extraHeaders)` | 发送 POST 请求，返回 (状态码, 响应体) |
| `parseOpenAIResponse(raw, model, errorMessage?)` | 解析 OpenAI 兼容响应 |
| `parseAnthropicResponse(raw, model)` | 解析 Anthropic Messages 响应 |
| `parseGeminiResponse(raw, model)` | 解析 Gemini generateContent 响应 |
| `buildOpenAIRequestBody(request, imageBase64?, imagePrompt?)` | 构造 OpenAI 兼容请求体 |
| `buildAnthropicRequestBody(request, imageBase64?, imagePrompt?)` | 构造 Anthropic 请求体 |
| `buildGeminiRequestBody(request, imageBase64?, imagePrompt?)` | 构造 Gemini 请求体 |
| `normalizeBaseUrl(config)` | 规范化 baseUrl（去除末尾斜杠） |

---

## 已支持 Provider 列表

时光印记内置支持 10 个 AI 厂商：

| # | Provider | 枚举值 | 默认 baseUrl | 认证方式 | 特点 |
|---|----------|--------|--------------|----------|------|
| 1 | OpenAI | `OPENAI` | `https://api.openai.com` | `Authorization: Bearer {key}` | GPT 系列，多模态 GPT-4V |
| 2 | Anthropic | `ANTHROPIC` | `https://api.anthropic.com` | `x-api-key: {key}` + `anthropic-version` | Claude 系列，Claude 3 多模态 |
| 3 | Gemini | `GEMINI` | `https://generativelanguage.googleapis.com` | URL 参数 `?key={key}` | Google Gemini，多模态 |
| 4 | 百度文心 | `BAIDU` | `https://aip.baidubce.com` | `Authorization: Bearer {token}` | 文心一言，国内访问快 |
| 5 | 阿里通义 | `ALIBABA` | `https://dashscope.aliyuncs.com` | `Authorization: Bearer {key}` | 通义千问，DashScope |
| 6 | 字节豆包 | `BYTEDANCE` | `https://ark.cn-beijing.volces.com` | `Authorization: Bearer {key}` | 豆包大模型，火山引擎 |
| 7 | 智谱 GLM | `ZHIPU` | `https://open.bigmodel.cn` | `Authorization: Bearer {jwt}` | GLM 系列，JWT 鉴权 |
| 8 | Moonshot | `MOONSHOT` | `https://api.moonshot.cn` | `Authorization: Bearer {key}` | Kimi，长上下文 |
| 9 | Ollama | `OLLAMA` | `http://localhost:11434` | 无（本地服务） | 本地部署，完全离线 |
| 10 | Custom | `CUSTOM` | 用户自定义 | `Authorization: Bearer {key}` | 兼容 OpenAI 协议的任意服务 |

### Provider 文件位置

```
ai/src/main/java/com/timemark/app/ai/provider/
├── AIProvider.kt              # 接口
├── BaseProvider.kt            # 抽象基类
├── OpenAIProvider.kt
├── AnthropicProvider.kt
├── GeminiProvider.kt
├── BaiduProvider.kt
├── AlibabaProvider.kt
├── ByteDanceProvider.kt
├── ZhipuProvider.kt
├── MoonshotProvider.kt
├── OllamaProvider.kt
└── CustomProvider.kt
```

---

## 新增 Provider 步骤

以新增「DeepSeek」为例，演示完整的 Provider 扩展流程。

### 步骤 1：实现 `AIProvider` 接口

创建 `ai/src/main/java/com/timemark/app/ai/provider/DeepSeekProvider.kt`：

```kotlin
package com.timemark.app.ai.provider

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.ChatRequest
import com.timemark.app.domain.model.ChatResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/**
 * DeepSeek Provider
 *
 * 使用 DeepSeek API（兼容 OpenAI 协议）。
 * 默认 baseUrl: https://api.deepseek.com
 */
class DeepSeekProvider(
    okHttpClient: OkHttpClient,
    json: Json
) : BaseProvider(okHttpClient, json) {

    override val providerType: AIProvider = AIProvider.DEEPSEEK

    private val defaultBaseUrl = "https://api.deepseek.com"

    override suspend fun chat(request: ChatRequest, config: AIConfig): ChatResponse {
        val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
        val url = "$baseUrl/v1/chat/completions"
        val body = buildOpenAIRequestBody(request)
        val (code, raw) = postJson(url, config.apiKey, body)
        return if (code in 200..299) {
            parseOpenAIResponse(raw, request.model)
        } else {
            parseOpenAIResponse(raw, request.model, "HTTP $code: ${raw.take(200)}")
        }
    }

    override suspend fun recognizeImage(
        imageBase64: String,
        prompt: String,
        config: AIConfig
    ): ChatResponse {
        // DeepSeek 若不支持多模态，返回错误
        return ChatResponse(
            content = "",
            tokensInput = 0,
            tokensOutput = 0,
            model = config.model,
            success = false,
            errorMessage = "DeepSeek 暂不支持图片识别"
        )
    }

    override suspend fun testConnection(config: AIConfig): Boolean {
        return runCatching {
            val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
            val url = "$baseUrl/v1/chat/completions"
            val request = ChatRequest(
                messages = listOf(com.timemark.app.domain.model.ChatMessage("user", "ping")),
                model = config.model,
                maxTokens = 5
            )
            val body = buildOpenAIRequestBody(request)
            val (code, _) = postJson(url, config.apiKey, body)
            code in 200..299
        }.getOrDefault(false)
    }
}
```

### 步骤 2：注册到 `AIServiceImpl` 的 providers map

编辑 `ai/src/main/java/com/timemark/app/ai/AIServiceImpl.kt`，在 `providers` map 中添加：

```kotlin
private val providers: Map<com.timemark.app.domain.model.AIProvider, AIProviderInterface> = mapOf(
    com.timemark.app.domain.model.AIProvider.OPENAI to OpenAIProvider(okHttpClient, json),
    com.timemark.app.domain.model.AIProvider.ANTHROPIC to AnthropicProvider(okHttpClient, json),
    // ... 其他 Provider
    com.timemark.app.domain.model.AIProvider.DEEPSEEK to DeepSeekProvider(okHttpClient, json)  // 新增
)
```

### 步骤 3：添加到 `AIProvider` 枚举

编辑 `domain/src/main/java/com/timemark/app/domain/model/Enums.kt`：

```kotlin
enum class AIProvider {
    OPENAI,
    ANTHROPIC,
    GEMINI,
    BAIDU,
    ALIBABA,
    BYTEDANCE,
    ZHIPU,
    MOONSHOT,
    OLLAMA,
    CUSTOM,
    DEEPSEEK  // 新增
}
```

### 步骤 4：更新 UI 选项

在 AI 配置页面的厂商选择器中添加新选项。编辑 `feature-ai/src/main/java/com/timemark/app/feature/ai/AIConfigAddScreen.kt`（及 `AIConfigEditScreen.kt`），在厂商下拉选项中添加：

```kotlin
val providerOptions = listOf(
    AIProvider.OPENAI to "OpenAI",
    AIProvider.ANTHROPIC to "Anthropic",
    // ... 其他
    AIProvider.DEEPSEEK to "DeepSeek"  // 新增
)
```

### 步骤 5（可选）：添加单元测试

创建 `ai/src/test/java/com/timemark/app/ai/providers/DeepSeekProviderTest.kt`：

```kotlin
class DeepSeekProviderTest {

    private lateinit var provider: DeepSeekProvider
    private lateinit var mockClient: OkHttpClient

    @Before
    fun setup() {
        // 使用 MockWebServer 模拟 HTTP 响应
        mockClient = OkHttpClient.Builder().build()
        val json = Json { ignoreUnknownKeys = true }
        provider = DeepSeekProvider(mockClient, json)
    }

    @Test
    fun `providerType returns DEEPSEEK`() {
        assertEquals(AIProvider.DEEPSEEK, provider.providerType)
    }

    @Test
    fun `chat returns success on valid response`() = runTest {
        // 使用 MockWebServer 返回模拟响应，验证解析结果
    }

    @Test
    fun `testConnection returns false on error`() = runTest {
        // 验证错误处理
    }
}
```

---

## 请求/响应格式说明

### 统一请求格式 ChatRequest

```kotlin
@Serializable
data class ChatRequest(
    val messages: List<ChatMessage>,
    val model: String,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2048
)

@Serializable
data class ChatMessage(
    val role: String,       // "system" / "user" / "assistant"
    val content: String,
    val images: List<String> = emptyList()  // base64 图片（多模态）
)
```

### 统一响应格式 ChatResponse

```kotlin
@Serializable
data class ChatResponse(
    val content: String,         // AI 回复内容
    val tokensInput: Int,        // 输入 Token 数
    val tokensOutput: Int,       // 输出 Token 数
    val model: String,           // 模型名称
    val success: Boolean,        // 是否成功
    val errorMessage: String? = null  // 错误信息
)
```

### 各厂商请求体示例

#### OpenAI 兼容（OpenAI / Moonshot / DeepSeek / Custom）

```json
POST /v1/chat/completions
Authorization: Bearer {apiKey}

{
  "model": "gpt-4",
  "messages": [
    {"role": "user", "content": "你好"}
  ],
  "temperature": 0.7,
  "max_tokens": 2048
}
```

#### Anthropic

```json
POST /v1/messages
x-api-key: {apiKey}
anthropic-version: 2023-06-01

{
  "model": "claude-3-opus",
  "messages": [
    {"role": "user", "content": "你好"}
  ],
  "max_tokens": 2048,
  "system": "系统提示"
}
```

#### Gemini

```json
POST /v1beta/models/{model}:generateContent?key={apiKey}

{
  "contents": [
    {"role": "user", "parts": [{"text": "你好"}]}
  ],
  "generationConfig": {
    "temperature": 0.7,
    "maxOutputTokens": 2048
  }
}
```

---

## 多模态支持说明

### 图片识别流程

```
用户拍照/选择图片
    │
    ▼
图片转 Base64
    │
    ▼
构造 ChatRequest（含 imageBase64）
    │
    ▼
AIService.recognizeImage(imageBase64, prompt, config)
    │
    ▼
Provider.recognizeImage() 构造多模态请求体
    │
    ▼
发送 HTTP 请求
    │
    ▼
解析响应 → ChatResponse
```

### 各厂商多模态请求格式

#### OpenAI（GPT-4V）

```json
{
  "model": "gpt-4-vision-preview",
  "messages": [{
    "role": "user",
    "content": [
      {"type": "text", "text": "识别图片中的食物"},
      {"type": "image_url", "image_url": {"url": "data:image/jpeg;base64,{base64}"}}
    ]
  }]
}
```

#### Anthropic（Claude 3）

```json
{
  "model": "claude-3-opus",
  "messages": [{
    "role": "user",
    "content": [
      {"type": "image", "source": {"type": "base64", "media_type": "image/jpeg", "data": "{base64}"}},
      {"type": "text", "text": "识别图片中的食物"}
    ]
  }]
}
```

#### Gemini

```json
{
  "contents": [{
    "role": "user",
    "parts": [
      {"text": "识别图片中的食物"},
      {"inlineData": {"mimeType": "image/jpeg", "data": "{base64}"}}
    ]
  }]
}
```

### 多模态支持矩阵

| Provider | 文本对话 | 图片识别 | 备注 |
|----------|----------|----------|------|
| OpenAI | ✅ | ✅ | GPT-4V 系列 |
| Anthropic | ✅ | ✅ | Claude 3 系列 |
| Gemini | ✅ | ✅ | Gemini Pro Vision |
| 百度文心 | ✅ | ⚠️ | 部分模型支持 |
| 阿里通义 | ✅ | ✅ | Qwen-VL 系列 |
| 字节豆包 | ✅ | ⚠️ | 部分模型支持 |
| 智谱 GLM | ✅ | ✅ | GLM-4V |
| Moonshot | ✅ | ❌ | 仅文本 |
| Ollama | ✅ | ⚠️ | 取决于本地模型 |
| Custom | ✅ | ⚠️ | 取决于服务端 |

---

## 错误处理规范

### 错误分类

| 错误类型 | 处理方式 | 示例 |
|----------|----------|------|
| 网络错误 | 返回 `success=false`，记录错误信息 | 超时、无网络 |
| HTTP 错误 | 返回 `success=false`，包含状态码与响应体 | 401 未授权、429 限流、500 服务器错误 |
| 解析错误 | 返回 `success=false`，记录解析异常 | JSON 格式错误 |
| 业务错误 | 返回 `success=false`，包含错误消息 | 预算超限、AI 功能禁用 |

### 错误处理示例

```kotlin
override suspend fun chat(request: ChatRequest, config: AIConfig): ChatResponse {
    val baseUrl = if (config.baseUrl.isBlank()) defaultBaseUrl else normalizeBaseUrl(config)
    val url = "$baseUrl/v1/chat/completions"
    val body = buildOpenAIRequestBody(request)
    val (code, raw) = postJson(url, config.apiKey, body)
    return if (code in 200..299) {
        parseOpenAIResponse(raw, request.model)
    } else {
        // HTTP 错误：包含状态码与响应体前 200 字符
        parseOpenAIResponse(raw, request.model, "HTTP $code: ${raw.take(200)}")
    }
}
```

### AIServiceImpl 层错误处理

```kotlin
// 1. 检查全局开关
if (!settingsRepository.aiGlobalEnabled.first()) {
    return failureResponse(config.model, "AI 功能已被禁用")
}

// 2. 检查预算限制
val budgetCheck = checkBudget()
if (budgetCheck != null) {
    return failureResponse(config.model, budgetCheck)
}

// 3. 调用 Provider（捕获异常）
val response = runCatching { provider.chat(request, config) }
    .getOrElse { e ->
        failureResponse(config.model, "请求异常: ${e.message}")
    }

// 4. 记录用量（无论成功失败）
recordUsage(config, AIFeature.CHAT, response, responseTime)
```

### 常见 HTTP 错误码

| 状态码 | 含义 | 处理建议 |
|--------|------|----------|
| 401 | 未授权 | 检查 API Key |
| 403 | 禁止访问 | 检查权限/区域限制 |
| 429 | 请求过多 | 降低频率，检查配额 |
| 500 | 服务器错误 | 重试或切换 Provider |
| 503 | 服务不可用 | 重试或切换 Provider |

---

## 测试 Provider 的方法

### 单元测试

使用 **MockWebServer** 模拟 HTTP 响应，验证 Provider 的请求构造与响应解析。

```kotlin
class OpenAIProviderTest {

    private lateinit var server: MockWebServer
    private lateinit var provider: OpenAIProvider

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val client = OkHttpClient.Builder().build()
        val json = Json { ignoreUnknownKeys = true }
        provider = OpenAIProvider(client, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `chat returns success on valid response`() = runTest {
        // 模拟成功响应
        val mockResponse = """
            {
              "choices": [{
                "message": {"role": "assistant", "content": "你好！"}
              }],
              "usage": {"prompt_tokens": 10, "completion_tokens": 5}
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(mockResponse))

        val config = AIConfig(
            provider = AIProvider.OPENAI,
            apiKey = "test-key",
            baseUrl = server.url("/").toString(),
            model = "gpt-4",
            modelType = AIModelType.TEXT
        )
        val request = ChatRequest(
            messages = listOf(ChatMessage("user", "你好")),
            model = "gpt-4"
        )

        val response = provider.chat(request, config)

        assertTrue(response.success)
        assertEquals("你好！", response.content)
        assertEquals(10, response.tokensInput)
        assertEquals(5, response.tokensOutput)
    }

    @Test
    fun `chat returns failure on 401`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("Unauthorized"))

        val config = AIConfig(/* ... */)
        val response = provider.chat(ChatRequest(/* ... */), config)

        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("401") == true)
    }
}
```

### 运行测试

```bash
# 运行所有 AI 模块测试
gradlew.bat :ai:test

# 运行指定 Provider 测试
gradlew.bat :ai:test --tests "com.timemark.app.ai.providers.OpenAIProviderTest"
```

### 连接测试

在应用内 AI 配置页面点击「测试连接」按钮，会调用 `testConnection()` 发送一个最小请求验证配置是否正确。

---

## 配置示例

### 各 Provider 的 API Key 获取方式

| Provider | 获取地址 | 说明 |
|----------|----------|------|
| OpenAI | https://platform.openai.com/api-keys | 注册后创建 API Key |
| Anthropic | https://console.anthropic.com/ | 注册后创建 API Key |
| Gemini | https://aistudio.google.com/ | Google AI Studio 获取 |
| 百度文心 | https://console.bce.baidu.com/qianfan/ | 千帆大模型平台 |
| 阿里通义 | https://dashscope.console.aliyun.com/ | DashScope 控制台 |
| 字节豆包 | https://console.volcengine.com/ark | 火山引擎方舟 |
| 智谱 GLM | https://open.bigmodel.cn/ | 智谱开放平台 |
| Moonshot | https://platform.moonshot.cn/ | Moonshot 开放平台 |
| Ollama | 本地安装 | 无需 API Key，默认 http://localhost:11434 |
| Custom | 用户自定 | 兼容 OpenAI 协议的任意服务 |

### 配置示例

#### OpenAI 配置

```kotlin
AIConfig(
    name = "我的 OpenAI",
    provider = AIProvider.OPENAI,
    apiKey = "sk-xxxxxxxxxxxx",           // 加密存储
    baseUrl = "",                          // 留空使用默认
    model = "gpt-4o",
    modelType = AIModelType.MULTIMODAL,
    priceInput = 0.005,                    // 每 1K Token $0.005
    priceOutput = 0.015,                   // 每 1K Token $0.015
    maxTokens = 4096,
    enabled = true,
    priority = 1,
    applicableFeatures = listOf(AIFeature.CHAT, AIFeature.FOOD_RECOGNITION)
)
```

#### Ollama 配置（本地）

```kotlin
AIConfig(
    name = "本地 Ollama",
    provider = AIProvider.OLLAMA,
    apiKey = "",                            // 无需 API Key
    baseUrl = "http://localhost:11434",     // 本地地址
    model = "llama2",
    modelType = AIModelType.TEXT,
    priceInput = 0.0,                       // 免费
    priceOutput = 0.0,
    maxTokens = 2048,
    enabled = true,
    priority = 10
)
```

#### Custom 配置（兼容 OpenAI 协议）

```kotlin
AIConfig(
    name = "自建服务",
    provider = AIProvider.CUSTOM,
    apiKey = "my-secret-key",
    baseUrl = "https://my-ai-service.com",  // 自定义地址
    model = "my-model",
    modelType = AIModelType.TEXT,
    maxTokens = 2048,
    enabled = true,
    priority = 5
)
```

### 多配置协同示例

配置多个 Provider，按优先级故障转移：

```
优先级 1: OpenAI (gpt-4o)        - 主力模型
优先级 2: Anthropic (claude-3)   - 备用模型
优先级 3: Ollama (llama2)        - 本地兜底
```

当 OpenAI 请求失败时，自动切换到 Anthropic；若 Anthropic 也失败，则使用本地 Ollama。

---

## 相关文档

- [架构设计](ARCHITECTURE.md) - 整体架构与 AI 架构详情
- [README](../README.md) - 项目说明
- [部署运行说明](DEPLOYMENT.md) - 构建与部署
- [测试报告](TEST_REPORT.md) - AI 模块测试结果
