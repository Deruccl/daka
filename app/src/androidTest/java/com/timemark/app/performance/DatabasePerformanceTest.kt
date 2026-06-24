package com.timemark.app.performance

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.timemark.app.data.db.TimeMarkDatabase
import com.timemark.app.data.db.entity.RecordEntity
import com.timemark.app.data.db.entity.TrackerEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureNanoTime

/**
 * 数据库性能测试
 *
 * 测试 Room 数据库的查询性能：
 * - 1000 条记录查询 < 100ms
 * - 批量插入性能
 * - 索引查询性能
 */
@RunWith(AndroidJUnit4::class)
class DatabasePerformanceTest {

    private lateinit var database: TimeMarkDatabase

    companion object {
        /** 查询性能阈值（毫秒） */
        private const val QUERY_THRESHOLD_MS = 100L

        /** 测试记录数量 */
        private const val RECORD_COUNT = 1000
    }

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, TimeMarkDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // 预插入一个 Tracker
        database.trackerDao().insert(
            TrackerEntity(
                id = 1L,
                name = "测试打卡",
                icon = "📝",
                color = "#2196F3",
                type = "COUNT",
                unit = "次",
                targetValue = 1.0,
                description = "",
                timePeriod = "ALL_DAY",
                customStartTime = null,
                customEndTime = null,
                isVisible = true,
                sortOrder = 0,
                aiEnabled = false,
                reminderEnabled = false,
                reminderTime = null,
                reminderFrequency = "DAILY",
                reminderIntervalHours = 2,
                reminderDays = "[]",
                createdAt = 0L,
                updatedAt = 0L
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    /**
     * 测试批量插入 1000 条记录的性能
     */
    @Test
    fun 批量插入1000条记录_应在合理时间内完成() = runTest {
        val records = List(RECORD_COUNT) { index ->
            RecordEntity(
                id = 0L,
                trackerId = 1L,
                value = index.toDouble(),
                date = "2024-${((index % 12) + 1).toString().padStart(2, '0')}-${((index % 28) + 1).toString().padStart(2, '0')}",
                time = "${(index % 24).toString().padStart(2, '0')}:00",
                timestamp = index.toLong(),
                note = "记录 $index",
                images = "[]",
                tags = "[]",
                mood = null,
                duration = 0L,
                createdAt = 0L,
                updatedAt = 0L
            )
        }

        val insertTimeMs = measureNanoTime {
            records.forEach { record ->
                database.recordDao().insert(record)
            }
        } / 1_000_000

        println("插入 $RECORD_COUNT 条记录耗时: ${insertTimeMs}ms")
        assertTrue(
            "批量插入时间 ${insertTimeMs}ms 过长",
            insertTimeMs < 30000 // 30s 内完成
        )
    }

    /**
     * 测试查询 1000 条记录的性能（按日期范围）
     */
    @Test
    fun 查询1000条记录_应小于100ms() = runTest {
        // 先插入 1000 条记录
        insertTestRecords(RECORD_COUNT)

        // 测量按日期范围查询的性能
        val queryTimeMs = measureNanoTime {
            database.recordDao().getRecordsByTrackerAndDateRange(
                trackerId = 1L,
                startDate = "2024-01-01",
                endDate = "2024-12-31"
            ).first()
        } / 1_000_000

        println("查询 $RECORD_COUNT 条记录耗时: ${queryTimeMs}ms")
        assertTrue(
            "查询时间 ${queryTimeMs}ms 超过阈值 ${QUERY_THRESHOLD_MS}ms",
            queryTimeMs < QUERY_THRESHOLD_MS
        )
    }

    /**
     * 测试按日期查询的性能
     */
    @Test
    fun 按日期查询_应小于100ms() = runTest {
        insertTestRecords(RECORD_COUNT)

        val queryTimeMs = measureNanoTime {
            database.recordDao().getRecordsByTrackerAndDate(
                trackerId = 1L,
                date = "2024-06-15"
            ).first()
        } / 1_000_000

        println("按日期查询耗时: ${queryTimeMs}ms")
        assertTrue(
            "按日期查询时间 ${queryTimeMs}ms 超过阈值 ${QUERY_THRESHOLD_MS}ms",
            queryTimeMs < QUERY_THRESHOLD_MS
        )
    }

    /**
     * 测试获取所有记录的性能
     */
    @Test
    fun 获取所有记录_应小于100ms() = runTest {
        insertTestRecords(RECORD_COUNT)

        val queryTimeMs = measureNanoTime {
            database.recordDao().getAllRecordsByDateRange(
                startDate = "2024-01-01",
                endDate = "2024-12-31"
            ).first()
        } / 1_000_000

        println("获取所有记录耗时: ${queryTimeMs}ms")
        assertTrue(
            "获取所有记录时间 ${queryTimeMs}ms 超过阈值 ${QUERY_THRESHOLD_MS}ms",
            queryTimeMs < QUERY_THRESHOLD_MS
        )
    }

    /**
     * 测试单条记录插入性能
     */
    @Test
    fun 单条记录插入_应快速完成() = runTest {
        val record = RecordEntity(
            id = 0L,
            trackerId = 1L,
            value = 1.0,
            date = "2024-06-15",
            time = "12:00",
            timestamp = System.currentTimeMillis(),
            note = "单条插入测试",
            images = "[]",
            tags = "[]",
            mood = null,
            duration = 0L,
            createdAt = 0L,
            updatedAt = 0L
        )

        val insertTimeMs = measureNanoTime {
            database.recordDao().insert(record)
        } / 1_000_000

        println("单条记录插入耗时: ${insertTimeMs}ms")
        assertTrue(
            "单条插入时间 ${insertTimeMs}ms 过长",
            insertTimeMs < 100
        )
    }

    /**
     * 测试记录删除性能
     */
    @Test
    fun 删除记录_应快速完成() = runTest {
        val recordId = database.recordDao().insert(
            RecordEntity(
                id = 0L,
                trackerId = 1L,
                value = 1.0,
                date = "2024-06-15",
                time = "12:00",
                timestamp = 0L,
                note = "",
                images = "[]",
                tags = "[]",
                mood = null,
                duration = 0L,
                createdAt = 0L,
                updatedAt = 0L
            )
        )

        val deleteTimeMs = measureNanoTime {
            database.recordDao().deleteById(recordId)
        } / 1_000_000

        println("删除记录耗时: ${deleteTimeMs}ms")
        assertTrue(
            "删除时间 ${deleteTimeMs}ms 过长",
            deleteTimeMs < 100
        )
    }

    /**
     * 测试每日总量查询性能
     */
    @Test
    fun 每日总量查询_应小于100ms() = runTest {
        insertTestRecords(RECORD_COUNT)

        val queryTimeMs = measureNanoTime {
            database.recordDao().getDailyTotal(trackerId = 1L, date = "2024-06-15")
        } / 1_000_000

        println("每日总量查询耗时: ${queryTimeMs}ms")
        assertTrue(
            "每日总量查询时间 ${queryTimeMs}ms 超过阈值 ${QUERY_THRESHOLD_MS}ms",
            queryTimeMs < QUERY_THRESHOLD_MS
        )
    }

    /**
     * 测试每日计数查询性能
     */
    @Test
    fun 每日计数查询_应小于100ms() = runTest {
        insertTestRecords(RECORD_COUNT)

        val queryTimeMs = measureNanoTime {
            database.recordDao().getDailyCount(trackerId = 1L, date = "2024-06-15")
        } / 1_000_000

        println("每日计数查询耗时: ${queryTimeMs}ms")
        assertTrue(
            "每日计数查询时间 ${queryTimeMs}ms 超过阈值 ${QUERY_THRESHOLD_MS}ms",
            queryTimeMs < QUERY_THRESHOLD_MS
        )
    }

    /**
     * 测试多次查询的平均性能
     */
    @Test
    fun 多次查询_平均时间应小于100ms() = runTest {
        insertTestRecords(RECORD_COUNT)

        val iterations = 10
        var totalTime = 0L

        repeat(iterations) { i ->
            val date = "2024-${((i % 12) + 1).toString().padStart(2, '0')}-15"
            val time = measureNanoTime {
                database.recordDao().getRecordsByTrackerAndDate(1L, date).first()
            } / 1_000_000
            totalTime += time
            println("第 ${i + 1} 次查询耗时: ${time}ms")
        }

        val avgTime = totalTime / iterations
        println("平均查询耗时: ${avgTime}ms")
        assertTrue(
            "平均查询时间 ${avgTime}ms 超过阈值 ${QUERY_THRESHOLD_MS}ms",
            avgTime < QUERY_THRESHOLD_MS
        )
    }

    /**
     * 测试数据一致性
     */
    @Test
    fun 数据一致性_插入后可正确查询() = runTest {
        val record = RecordEntity(
            id = 0L,
            trackerId = 1L,
            value = 42.0,
            date = "2024-06-15",
            time = "14:30",
            timestamp = 1718447400000L,
            note = "一致性测试",
            images = "[\"img.jpg\"]",
            tags = "[\"测试\"]",
            mood = "开心",
            duration = 0L,
            createdAt = 0L,
            updatedAt = 0L
        )

        val id = database.recordDao().insert(record)
        val retrieved = database.recordDao().getRecordById(id)

        assertEquals(42.0, retrieved!!.value, 0.001)
        assertEquals("2024-06-15", retrieved.date)
        assertEquals("14:30", retrieved.time)
        assertEquals("一致性测试", retrieved.note)
    }

    /** 辅助方法：批量插入测试记录 */
    private suspend fun insertTestRecords(count: Int) {
        for (i in 0 until count) {
            database.recordDao().insert(
                RecordEntity(
                    id = 0L,
                    trackerId = 1L,
                    value = i.toDouble(),
                    date = "2024-${((i % 12) + 1).toString().padStart(2, '0')}-${((i % 28) + 1).toString().padStart(2, '0')}",
                    time = "${(i % 24).toString().padStart(2, '0')}:00",
                    timestamp = i.toLong(),
                    note = "记录 $i",
                    images = "[]",
                    tags = "[]",
                    mood = null,
                    duration = 0L,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
        }
    }
}
