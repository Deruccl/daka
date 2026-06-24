package com.timemark.app.di

import com.timemark.app.ai.AIServiceImpl
import com.timemark.app.domain.repository.AIService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AI Hilt 模块
 *
 * 将 [AIServiceImpl] 绑定到 domain 模块定义的 [AIService] 接口，
 * 供 domain 层 UseCase 通过构造注入使用。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AIModule {

    @Binds
    @Singleton
    abstract fun bindAIService(impl: AIServiceImpl): AIService
}
