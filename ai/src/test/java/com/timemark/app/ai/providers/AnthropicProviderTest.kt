package com.timemark.app.ai.provider

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIModelType
import com.timemark.app.domain.model.AIProvider
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.model.ChatRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * AnthropicProvider 单元测试
 *
 * 使用 MockWebServer 验证请求构建与响应解析。
 * Anthropic 使用 x-api-key 鉴权，与 OpenAI 的 Bearer Token 不同。
 */
class AnthropicProviderTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var provider: AnthropicProvider
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
        provider = AnthropicProvider(client, json)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun providerType_返回ANTHROPIC() {
        assertEquals(AIProvider.ANTHROPIC, provider.providerType)
    }

    @Test
    fun chat_成功响应_解析内容() = runTest {
        val responseBody = """
            {
                "id": "msg_123",
                "type": "message",
                "role": "assistant",
                "content": [
                    {
                        "type": "text",
                        "text": "你好，我是 Claude"
                    }
                ],
                "usage": {
                    "input_tokens": 15,
                    "output_tokens": 25
                }
            }
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "你好")),
            model = "claude-3-opus"
        )
        val response = provider.chat(request, config)

        assertTrue(response.success)
        assertEquals("你好，我是 Claude", response.content)
        assertEquals(15, response.tokensInput)
        assertEquals(25, response.tokensOutput)
        assertEquals("claude-3-opus", response.model)
    }

    @Test
    fun chat_HTTP错误_返回失败响应() = runTest {
        mockServer.enqueue(MockResponse().setResponseCode(401).setBody("Unauthorized"))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "claude-3-opus"
        )
        val response = provider.chat(request, config)

        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("401") == true)
    }

    @Test
    fun chat_请求URL正确拼接() = runTest {
        val responseBody = """
            {
                "content": [{"type": "text", "text": "ok"}],
                "usage": {"input_tokens": 1, "output_tokens": 1}
            }
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "claude-3-opus"
        )
        provider.chat(request, config)

        val recordedRequest = mockServer.takeRequest()
        assertEquals("/v1/messages", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
    }

    @Test
    fun chat_请求头包含x_api_key() = runTest {
        val responseBody = """
            {
                "content": [{"type": "text", "text": "ok"}],
                "usage": {"input_tokens": 1, "output_tokens": 1}
            }
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(
            baseUrl = mockServer.url("/").toString().trimEnd('/'),
            apiKey = "anthropic-key-123"
        )
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "claude-3-opus"
        )
        provider.chat(request, config)

        val recordedRequest = mockServer.takeRequest()
        assertEquals("anthropic-key-123", recordedRequest.getHeader("x-api-key"))
    }

    @Test
    fun chat_请求头包含anthropic_version() = runTest {
        val responseBody = """
            {
                "content": [{"type": "text", "text": "ok"}],
                "usage": {"input_tokens": 1, "output_tokens": 1}
            }
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "claude-3-opus"
        )
        provider.chat(request, config)

        val recordedRequest = mockServer.takeRequest()
        assertEquals("2023-06-01", recordedRequest.getHeader("anthropic-version"))
    }

    @Test
    fun chat_无content字段_返回空字符串() = runTest {
        val responseBody = """{"usage": {"input_tokens": 5, "output_tokens": 0}}"""
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "claude-3-opus"
        )
        val response = provider.chat(request, config)

        assertTrue(response.success)
        assertEquals("", response.content)
    }

    @Test
    fun chat_无usage字段_默认为0() = runTest {
        val responseBody = """
            {"content": [{"type": "text", "text": "回复"}]}
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "claude-3-opus"
        )
        val response = provider.chat(request, config)

        assertTrue(response.success)
        assertEquals(0, response.tokensInput)
        assertEquals(0, response.tokensOutput)
    }

    @Test
    fun chat_无效JSON_返回解析失败() = runTest {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("not json"))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "claude-3-opus"
        )
        val response = provider.chat(request, config)

        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("解析响应失败") == true)
    }

    @Test
    fun testConnection_成功_返回true() = runTest {
        val responseBody = """
            {"content": [{"type": "text", "text": "ok"}], "usage": {}}
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val result = provider.testConnection(config)
        assertTrue(result)
    }

    @Test
    fun testConnection_失败_返回false() = runTest {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Error"))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val result = provider.testConnection(config)
        assertFalse(result)
    }

    @Test
    fun recognizeImage_成功响应_解析内容() = runTest {
        val responseBody = """
            {
                "content": [{"type": "text", "text": "这是苹果"}],
                "usage": {"input_tokens": 30, "output_tokens": 5}
            }
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val response = provider.recognizeImage("base64image", "识别水果", config)

        assertTrue(response.success)
        assertEquals("这是苹果", response.content)
        assertEquals(30, response.tokensInput)
    }

    @Test
    fun recognizeImage_HTTP错误_返回失败() = runTest {
        mockServer.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request"))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val response = provider.recognizeImage("base64", "识别", config)

        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("400") == true)
    }

    /** 辅助方法：创建测试用 AIConfig */
    private fun createConfig(
        baseUrl: String = "https://api.anthropic.com",
        apiKey: String = "test-key"
    ): AIConfig = AIConfig(
        id = 1L,
        name = "Anthropic 测试",
        provider = AIProvider.ANTHROPIC,
        apiKey = apiKey,
        baseUrl = baseUrl,
        model = "claude-3-opus",
        modelType = AIModelType.MULTIMODAL,
        maxTokens = 4096
    )
}
