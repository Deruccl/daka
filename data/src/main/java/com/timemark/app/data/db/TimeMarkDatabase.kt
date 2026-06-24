package com.timemark.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.timemark.app.data.db.converters.TypeConverters
import com.timemark.app.data.db.dao.AIConfigDao
import com.timemark.app.data.db.dao.AIUsageDao
import com.timemark.app.data.db.dao.ChatHistoryDao
import com.timemark.app.data.db.dao.RecordDao
import com.timemark.app.data.db.dao.StatsDao
import com.timemark.app.data.db.dao.TrackerDao
import com.timemark.app.data.db.entity.AIConfigEntity
import com.timemark.app.data.db.entity.AIUsageEntity
import com.timemark.app.data.db.entity.ChatHistoryEntity
import com.timemark.app.data.db.entity.DailyStatsEntity
import com.timemark.app.data.db.entity.RecordEntity
import com.timemark.app.data.db.entity.TrackerEntity

/**
 * TimeMark 应用主数据库。
 *
 * 包含 6 张表：
 * - trackers、records、daily_stats、ai_configs、ai_usage（v1）
 * - chat_history（v2，Task 33.2 新增）
 *
 * v1 -> v2 迁移：新增 chat_history 表用于本地保存 AI 对话历史。
 */
@Database(
    entities = [
        TrackerEntity::class,
        RecordEntity::class,
        DailyStatsEntity::class,
        AIConfigEntity::class,
        AIUsageEntity::class,
        ChatHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(TypeConverters::class)
abstract class TimeMarkDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDao
    abstract fun recordDao(): RecordDao
    abstract fun statsDao(): StatsDao
    abstract fun aiConfigDao(): AIConfigDao
    abstract fun aiUsageDao(): AIUsageDao
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        const val DATABASE_NAME = "timemark.db"

        /**
         * v1 -> v2 迁移：创建 chat_history 表
         *
         * 字段：
         * - id INTEGER PRIMARY KEY AUTOINCREMENT
         * - provider TEXT NOT NULL
         * - role TEXT NOT NULL
         * - content TEXT NOT NULL
         * - timestamp INTEGER NOT NULL
         * - token_count INTEGER NOT NULL DEFAULT 0
         *
         * 索引：timestamp、provider、role
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chat_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        provider TEXT NOT NULL,
                        role TEXT NOT NULL,
                        content TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        tokenCount INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_history_timestamp ON chat_history(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_history_provider ON chat_history(provider)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_history_role ON chat_history(role)")
            }
        }
    }
}
