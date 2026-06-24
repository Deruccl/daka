package com.timemark.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timemark.app.data.db.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

/**
 * 每日统计数据访问对象。
 */
@Dao
interface StatsDao {
    @Query("SELECT * FROM daily_stats WHERE tracker_id = :trackerId AND date = :date")
    fun getDailyStats(trackerId: Long, date: String): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun getAllTrackersDailyStats(date: String): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM daily_stats WHERE tracker_id = :trackerId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRangeStats(trackerId: Long, startDate: String, endDate: String): Flow<List<DailyStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: DailyStatsEntity)

    @Query("DELETE FROM daily_stats WHERE tracker_id = :trackerId AND date = :date")
    suspend fun delete(trackerId: Long, date: String)

    @Query("SELECT COUNT(*) FROM daily_stats WHERE tracker_id = :trackerId AND completed = 1 AND date <= :date ORDER BY date DESC")
    suspend fun getStreak(trackerId: Long, date: String): Int
}
