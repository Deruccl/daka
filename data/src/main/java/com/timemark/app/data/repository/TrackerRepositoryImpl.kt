package com.timemark.app.data.repository

import com.timemark.app.data.db.dao.TrackerDao
import com.timemark.app.data.mapper.toDomain
import com.timemark.app.data.mapper.toEntity
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * TrackerRepository 接口实现。
 * 通过 TrackerDao 访问数据库，使用 Mapper 完成 Entity/Domain 模型互转。
 */
class TrackerRepositoryImpl @Inject constructor(
    private val trackerDao: TrackerDao
) : TrackerRepository {

    /** 获取所有打卡项目（按 sortOrder 升序） */
    override fun getAllTrackers(): Flow<List<Tracker>> =
        trackerDao.getAllTrackers().map { entities -> entities.map { it.toDomain() } }

    /** 获取所有可见的打卡项目 */
    override fun getVisibleTrackers(): Flow<List<Tracker>> =
        trackerDao.getVisibleTrackers().map { entities -> entities.map { it.toDomain() } }

    /** 根据 ID 获取打卡项目 */
    override fun getTrackerById(id: Long): Flow<Tracker?> =
        trackerDao.getTrackerById(id).map { it?.toDomain() }

    /** 新增打卡项目，返回自增 ID */
    override suspend fun insertTracker(tracker: Tracker): Long =
        trackerDao.insert(tracker.toEntity())

    /** 更新打卡项目 */
    override suspend fun updateTracker(tracker: Tracker) =
        trackerDao.update(tracker.toEntity())

    /** 根据 ID 删除打卡项目 */
    override suspend fun deleteTracker(id: Long) =
        trackerDao.deleteById(id)

    /** 批量更新排序（在单个事务中执行） */
    override suspend fun updateSortOrder(orders: List<Pair<Long, Int>>) =
        trackerDao.updateSortOrders(orders)
}
