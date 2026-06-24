package com.timemark.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.timemark.app.data.db.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 打卡记录数据访问对象。
 */
@Dao
interface RecordDao {
    @Query("SELECT * FROM records WHERE tracker_id = :trackerId AND date = :date ORDER BY timestamp ASC")
    fun getRecordsByTrackerAndDate(trackerId: Long, date: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE tracker_id = :trackerId AND date BETWEEN :startDate AND :endDate ORDER BY timestamp ASC")
    fun getRecordsByTrackerAndDateRange(trackerId: Long, startDate: String, endDate: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE date = :date ORDER BY timestamp ASC")
    fun getAllRecordsByDate(date: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp ASC")
    fun getAllRecordsByDateRange(startDate: String, endDate: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getRecordById(id: Long): RecordEntity?

    @Insert
    suspend fun insert(record: RecordEntity): Long

    @Update
    suspend fun update(record: RecordEntity)

    @Delete
    suspend fun delete(record: RecordEntity)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM records WHERE tracker_id = :trackerId")
    suspend fun deleteByTracker(trackerId: Long)

    @Query("DELETE FROM records WHERE tracker_id = :trackerId AND date BETWEEN :startDate AND :endDate")
    suspend fun deleteByTrackerAndDateRange(trackerId: Long, startDate: String, endDate: String)

    @Query("SELECT SUM(value) FROM records WHERE tracker_id = :trackerId AND date = :date")
    suspend fun getDailyTotal(trackerId: Long, date: String): Double?

    @Query("SELECT COUNT(*) FROM records WHERE tracker_id = :trackerId AND date = :date")
    suspend fun getDailyCount(trackerId: Long, date: String): Int
}
