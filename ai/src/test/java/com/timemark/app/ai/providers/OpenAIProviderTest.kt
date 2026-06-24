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
 * OpenAIProvider 单元测试
 *
 * 使用 MockWebServer 模拟 HTTP 响应，验证请求构建与响应解析。
 */
class OpenAIProviderTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var provider: OpenAIProvider
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
        provider = OpenAIProvider(client, json)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun providerType_返回OPENAI() {
        assertEquals(AIProvider.OPENAI, provider.providerType)
    }

    @Test
    fun chat_成功响应_解析内容() = runTest {
        // 模拟 OpenAI 成功响应
        val responseBody = """
            {
                "id": "chatcmpl-123",
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": "你好，我是 AI 助手"
                        },
                        "finish_reason": "stop"
                    }
                ],
                "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 20,
                    "total_tokens": 30
                }
            }
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "你好")),
            model = "gpt-4"
        )
        val response = provider.chat(request, config)

        assertTrue(response.success)
        assertEquals("你好，我是 AI 助手", response.content)
        assertEquals(10, response.tokensInput)
        assertEquals(20, response.tokensOutput)
        assertEquals("gpt-4", response.model)
    }

    @Test
    fun chat_HTTP错误_返回失败响应() = runTest {
        mockServer.enqueue(MockResponse().setResponseCode(401).setBody("Unauthorized"))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        val response = provider.chat(request, config)

        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("401") == true)
    }

    @Test
    fun chat_空content_正确处理() = runTest {
        val responseBody = """
            {
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": ""
                        }
                    }
                ],
                "usage": {
                    "prompt_tokens": 5,
                    "completion_tokens": 0
                }
            }
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        val response = provider.chat(request, config)

        assertTrue(response.success)
        assertEquals("", response.content)
    }

    @Test
    fun chat_无usage字段_默认为0() = runTest {
        val responseBody = """
            {
                "choices": [
                    {
                        "message": {
                            "content": "回复"
                        }
                    }
                ]
            }
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        val response = provider.chat(request, config)

        assertTrue(response.success)
        assertEquals(0, response.tokensInput)
        assertEquals(0, response.tokensOutput)
    }

    @Test
    fun chat_无效JSON_返回解析失败() = runTest {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("invalid json"))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        val response = provider.chat(request, config)

        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("解析响应失败") == true)
    }

    @Test
    fun chat_请求URL正确拼接() = runTest {
        val responseBody = """{"choices":[{"message":{"content":"ok"}}],"usage":{}}"""
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        provider.chat(request, config)

        val recordedRequest = mockServer.takeRequest()
        assertEquals("/v1/chat/completions", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
    }

    @Test
    fun chat_请求头包含Authorization() = runTest {
        val responseBody = """{"choices":[{"message":{"content":"ok"}}],"usage":{}}"""
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(
            baseUrl = mockServer.url("/").toString().trimEnd('/'),
            apiKey = "sk-test-key"
        )
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        provider.chat(request, config)

        val recordedRequest = mockServer.takeRequest()
        assertEquals("Bearer sk-test-key", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun chat_默认baseUrl_使用OpenAI官方地址() = runTest {
        val responseBody = """{"choices":[{"message":{"content":"ok"}}],"usage":{}}"""
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        // baseUrl 为空时使用默认地址
        val config = createConfig(baseUrl = "")
        val request = ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = "test")),
            model = "gpt-4"
        )
        // 由于使用默认地址，实际会请求 api.openai.com，这里预期会失败
        val response = provider.chat(request, config)
        // 网络请求失败，应返回失败响应
        assertFalse(response.success)
    }

    @Test
    fun testConnection_成功_返回true() = runTest {
        val responseBody = """{"choices":[{"message":{"content":"ok"}}],"usage":{}}"""
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val result = provider.testConnection(config)
        assertTrue(result)
    }

    @Test
    fun testConnection_失败_返回false() = runTest {
        mockServer.enqueue(MockResponse().setResponseCode(401).setBody("Unauthorized"))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val result = provider.testConnection(config)
        assertFalse(result)
    }

    @Test
    fun recognizeImage_成功响应_解析内容() = runTest {
        val responseBody = """
            {
                "choices": [
                    {
                        "message": {
                            "content": "这是一碗米饭"
                        }
                    }
                ],
                "usage": {
                    "prompt_tokens": 50,
                    "completion_tokens": 10
                }
            }
        """.trimIndent()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val response = provider.recognizeImage("base64imagedata", "识别食物", config)

        assertTrue(response.success)
        assertEquals("这是一碗米饭", response.content)
        assertEquals(50, response.tokensInput)
        assertEquals(10, response.tokensOutput)
    }

    @Test
    fun recognizeImage_HTTP错误_返回失败() = runTest {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Server Error"))

        val config = createConfig(baseUrl = mockServer.url("/").toString().trimEnd('/'))
        val response = provider.recognizeImage("base64data", "识别", config)

        assertFalse(response.success)
        assertTrue(response.errorMessage?.contains("500") == true)
    }

    /** 辅助方法：创建测试用 AIConfig */
    private fun createConfig(
        baseUrl: String = "https://api.openai.com",
        apiKey: String = "test-key"
    ): AIConfig = AIConfig(
        id = 1L,
        name = "测试配置",
        provider = AIProvider.OPENAI,
        apiKey = apiKey,
        baseUrl = baseUrl,
        model = "gpt-4",
        modelType = AIModelType.TEXT,
        maxTokens = 2048
    )
}
