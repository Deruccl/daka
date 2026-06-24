package com.timemark.app.domain.repository

import com.timemark.app.domain.model.Tracker
import kotlinx.coroutines.flow.Flow

/** 打卡项目仓库接口 */
interface TrackerRepository {
    fun getAllTrackers(): Flow<List<Tracker>>
    fun getVisibleTrackers(): Flow<List<Tracker>>
    fun getTrackerById(id: Long): Flow<Tracker?>
    suspend fun insertTracker(tracker: Tracker): Long
    suspend fun updateTracker(tracker: Tracker)
    suspend fun deleteTracker(id: Long)
    suspend fun updateSortOrder(orders: List<Pair<Long, Int>>)
}
