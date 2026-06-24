package com.timemark.app.ai

import com.timemark.app.domain.model.ProxyConfig
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HTTP 代理管理器（Task 36.2）
 *
 * 职责：
 * 1. 根据 [ProxyConfig] 创建带代理配置的 OkHttpClient
 * 2. 支持 HTTP 代理与代理认证（Basic Auth）
 * 3. 缓存已创建的代理客户端，避免重复创建
 *
 * 代理配置作用于单个 AI Provider，当 Provider 配置了代理时，
 * [AIServiceImpl] 会通过本管理器获取带代理的 OkHttpClient 并创建对应的 Provider 实例。
 */
@Singleton
class ProxyManager @Inject constructor(
    private val baseClient: OkHttpClient
) {

    /** 代理客户端缓存：key 为代理配置的唯一标识（host:port:user） */
    private val clientCache = ConcurrentHashMap<String, OkHttpClient>()

    /**
     * 根据代理配置创建 OkHttpClient.Builder（Task 36.2）
     *
     * - proxyConfig 为 null 或未启用时，返回基于 baseClient 的 Builder（无代理）
     * - proxyConfig 启用时，配置 HTTP 代理与可选的代理认证
     *
     * @param proxyConfig 代理配置，null 表示不使用代理
     * @return 配置好代理的 OkHttpClient.Builder
     */
    fun createOkHttpClient(proxyConfig: ProxyConfig?): OkHttpClient.Builder {
        val builder = baseClient.newBuilder()

        // 无代理配置或未启用时，直接返回基础 Builder
        if (proxyConfig == null || !proxyConfig.enabled || proxyConfig.host.isBlank()) {
            return builder
        }

        // 配置 HTTP 代理
        val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyConfig.host, proxyConfig.port))
        builder.proxy(proxy)

        // 配置代理认证（当提供了用户名和密码时）
        if (!proxyConfig.username.isNullOrEmpty()) {
            builder.proxyAuthenticator(
                ProxyAuthenticator(proxyConfig.username, proxyConfig.password ?: "")
            )
        }

        return builder
    }

    /**
     * 获取带代理配置的 OkHttpClient（带缓存）
     *
     * 相同的代理配置复用同一个 OkHttpClient 实例，避免重复创建连接池。
     *
     * @param proxyConfig 代理配置，null 表示使用基础客户端
     * @return 配置好代理的 OkHttpClient
     */
    fun getProxiedClient(proxyConfig: ProxyConfig?): OkHttpClient {
        // 无代理配置时直接返回基础客户端
        if (proxyConfig == null || !proxyConfig.enabled || proxyConfig.host.isBlank()) {
            return baseClient
        }

        val cacheKey = buildCacheKey(proxyConfig)
        return clientCache.getOrPut(cacheKey) {
            createOkHttpClient(proxyConfig)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
        }
    }

    /** 构造代理客户端缓存 key */
    private fun buildCacheKey(config: ProxyConfig): String =
        "${config.host}:${config.port}:${config.username ?: ""}"

    /** 清空代理客户端缓存（配置变更时调用） */
    fun clearCache() {
        clientCache.clear()
    }
}

/**
 * 代理认证器（Task 36.2）
 *
 * 处理 HTTP 代理的 Basic Auth 认证。
 * 当代理服务器返回 407 Proxy Authentication Required 时，
 * 自动添加 Proxy-Authorization 请求头。
 */
private class ProxyAuthenticator(
    private val username: String,
    private val password: String
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 避免无限重试：若已认证过仍失败则放弃
        if (response.request.header("Proxy-Authorization") != null) {
            return null
        }

        // 构造 Basic Auth 凭证
        val credential = Credentials.basic(username, password)
        return response.request.newBuilder()
            .header("Proxy-Authorization", credential)
            .build()
    }
}
