package com.timemark.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.timemark.app.data.db.entity.TrackerEntity
import kotlinx.coroutines.flow.Flow

/**
 * 打卡项目数据访问对象。
 */
@Dao
interface TrackerDao {
    @Query("SELECT * FROM trackers ORDER BY sortOrder ASC")
    fun getAllTrackers(): Flow<List<TrackerEntity>>

    @Query("SELECT * FROM trackers WHERE isVisible = 1 ORDER BY sortOrder ASC")
    fun getVisibleTrackers(): Flow<List<TrackerEntity>>

    @Query("SELECT * FROM trackers WHERE id = :id")
    fun getTrackerById(id: Long): Flow<TrackerEntity?>

    @Query("SELECT * FROM trackers WHERE id = :id")
    suspend fun getTrackerByIdOnce(id: Long): TrackerEntity?

    @Insert
    suspend fun insert(tracker: TrackerEntity): Long

    @Update
    suspend fun update(tracker: TrackerEntity)

    @Delete
    suspend fun delete(tracker: TrackerEntity)

    @Query("DELETE FROM trackers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE trackers SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: Long, order: Int)

    /** 批量更新排序，在单个事务中执行。 */
    @Transaction
    suspend fun updateSortOrders(orders: List<Pair<Long, Int>>) {
        orders.forEach { (id, order) -> updateSortOrder(id, order) }
    }
}
