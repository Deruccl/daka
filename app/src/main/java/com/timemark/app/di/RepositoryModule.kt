package com.timemark.app.di

import com.timemark.app.data.repository.AIConfigRepositoryImpl
import com.timemark.app.data.repository.AIUsageRepositoryImpl
import com.timemark.app.data.repository.ChatHistoryRepositoryImpl
import com.timemark.app.data.repository.RecordRepositoryImpl
import com.timemark.app.data.repository.SettingsRepositoryImpl
import com.timemark.app.data.repository.StatsRepositoryImpl
import com.timemark.app.data.repository.TrackerRepositoryImpl
import com.timemark.app.domain.repository.AIConfigRepository
import com.timemark.app.domain.repository.AIUsageRepository
import com.timemark.app.domain.repository.ChatHistoryRepository
import com.timemark.app.domain.repository.RecordRepository
import com.timemark.app.domain.repository.SettingsRepository
import com.timemark.app.domain.repository.StatsRepository
import com.timemark.app.domain.repository.TrackerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Hilt 模块。
 * 将各 RepositoryImpl 实现绑定到对应的 Repository 接口。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTrackerRepository(impl: TrackerRepositoryImpl): TrackerRepository

    @Binds
    @Singleton
    abstract fun bindRecordRepository(impl: RecordRepositoryImpl): RecordRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds
    @Singleton
    abstract fun bindAIConfigRepository(impl: AIConfigRepositoryImpl): AIConfigRepository

    @Binds
    @Singleton
    abstract fun bindAIUsageRepository(impl: AIUsageRepositoryImpl): AIUsageRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    // Task 33.2: ChatHistoryRepository
    @Binds
    @Singleton
    abstract fun bindChatHistoryRepository(impl: ChatHistoryRepositoryImpl): ChatHistoryRepository
}
