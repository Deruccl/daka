package com.timemark.app.di

import android.content.Context
import androidx.room.Room
import com.timemark.app.data.db.DatabaseKeyProvider
import com.timemark.app.data.db.TimeMarkDatabase
import com.timemark.app.data.db.dao.AIConfigDao
import com.timemark.app.data.db.dao.AIUsageDao
import com.timemark.app.data.db.dao.ChatHistoryDao
import com.timemark.app.data.db.dao.RecordDao
import com.timemark.app.data.db.dao.StatsDao
import com.timemark.app.data.db.dao.TrackerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.zetetic.android.database.sqlcipher.SupportOpenHelperFactory

/**
 * Room 数据库 Hilt 模块（Task 32.3 增强）。
 *
 * 提供 TimeMarkDatabase 单例及各 DAO 的依赖。
 *
 * Task 32.3: 根据 SettingsDataStore 中的 databaseEncryptionEnabled 设置决定是否启用 SQLCipher 加密。
 * - 加密时使用 SupportOpenHelperFactory(passphrase) 作为 Room 的 openHelperFactory
 * - 密码从 DatabaseKeyProvider 获取（经 Keystore 加密存储）
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTimeMarkDatabase(
        @ApplicationContext context: Context,
        keyProvider: DatabaseKeyProvider
    ): TimeMarkDatabase {
        val builder = Room.databaseBuilder(
            context,
            TimeMarkDatabase::class.java,
            TimeMarkDatabase.DATABASE_NAME
        )
            // Task 33.2: v1 -> v2 迁移，新增 chat_history 表
            // Task 36.2: v2 -> v3 迁移，ai_configs 新增 proxyConfig 列
            .addMigrations(TimeMarkDatabase.MIGRATION_1_2, TimeMarkDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()

        // Task 32.3: 根据设置决定是否启用 SQLCipher 加密
        if (keyProvider.isEncryptionEnabled()) {
            val passphrase = keyProvider.getOrCreatePassphrase()
            // 使用 SQLCipher 的 SupportOpenHelperFactory 包装 SQLiteOpenHelper
            val factory = SupportOpenHelperFactory(passphrase)
            builder.openHelperFactory(factory)
        }

        return builder.build()
    }

    @Provides
    fun provideTrackerDao(db: TimeMarkDatabase): TrackerDao = db.trackerDao()

    @Provides
    fun provideRecordDao(db: TimeMarkDatabase): RecordDao = db.recordDao()

    @Provides
    fun provideStatsDao(db: TimeMarkDatabase): StatsDao = db.statsDao()

    @Provides
    fun provideAIConfigDao(db: TimeMarkDatabase): AIConfigDao = db.aiConfigDao()

    @Provides
    fun provideAIUsageDao(db: TimeMarkDatabase): AIUsageDao = db.aiUsageDao()

    // Task 33.2: ChatHistoryDao
    @Provides
    fun provideChatHistoryDao(db: TimeMarkDatabase): ChatHistoryDao = db.chatHistoryDao()
}
