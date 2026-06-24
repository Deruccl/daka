package com.timemark.app.di

import com.timemark.app.BuildConfig
import com.timemark.app.ai.NetworkLogger
import com.timemark.app.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络 Hilt 模块（Task 32.6 增强）。
 *
 * 提供 OkHttpClient 与 Json 单例依赖。
 *
 * Task 32.6 增强：
 * - 根据设置添加 NetworkLogger Interceptor（记录 AI 网络请求）
 * - 根据设置禁用网络访问（使用空拦截器返回错误响应）
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        settingsRepository: SettingsRepository
    ): OkHttpClient {
        // 同步读取设置（OkHttpClient 在应用启动时创建一次）
        val networkLogEnabled = runBlocking { settingsRepository.networkLogEnabled.first() }
        val disableNetworkAccess = runBlocking { settingsRepository.disableNetworkAccess.first() }

        // 更新 NetworkLogger 的启用状态
        NetworkLogger.enabled = networkLogEnabled

        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            })

        // Task 32.6: 添加网络请求日志拦截器
        if (networkLogEnabled) {
            builder.addInterceptor(NetworkLogger)
        }

        // Task 32.6: 禁用网络访问拦截器
        if (disableNetworkAccess) {
            builder.addInterceptor(NetworkDisableInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
}

/**
 * 网络禁用拦截器（Task 32.6）
 *
 * 当用户启用"禁用网络访问"时，所有网络请求直接返回错误响应，
 * 不实际发起网络连接，确保应用完全离线。
 */
private object NetworkDisableInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 返回 403 Forbidden 响应，不发起实际网络请求
        return Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(403)
            .message("Network access disabled by user")
            .body("{\"error\":\"网络访问已被用户禁用\"}".toResponseBody("application/json".toMediaType()))
            .build()
    }
}
