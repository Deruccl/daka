package com.timemark.app.data.repository

import com.timemark.app.data.db.dao.RecordDao
import com.timemark.app.data.mapper.toDomain
import com.timemark.app.data.mapper.toEntity
import com.timemark.app.domain.model.Record
import com.timemark.app.domain.repository.RecordRepository
import com.timemark.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * RecordRepository 接口实现。
 * 通过 RecordDao 访问数据库，并在记录变更后触发每日统计的重新计算。
 *
 * @param recordDao 记录数据访问对象
 * @param statsRepository 统计仓库，用于在记录变更后重算 DailyStats
 */
class RecordRepositoryImpl @Inject constructor(
    private val recordDao: RecordDao,
    private val statsRepository: StatsRepository
) : RecordRepository {

    /** 获取指定 Tracker 在指定日期的记录列表 */
    override fun getRecordsByTrackerAndDate(trackerId: Long, date: String): Flow<List<Record>> =
        recordDao.getRecordsByTrackerAndDate(trackerId, date).map { it.map { e -> e.toDomain() } }

    /** 获取指定 Tracker 在日期范围内的记录列表 */
    override fun getRecordsByTrackerAndDateRange(
        trackerId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<Record>> =
        recordDao.getRecordsByTrackerAndDateRange(trackerId, startDate, endDate)
            .map { it.map { e -> e.toDomain() } }

    /** 获取所有 Tracker 在指定日期的记录列表 */
    override fun getAllRecordsByDate(date: String): Flow<List<Record>> =
        recordDao.getAllRecordsByDate(date).map { it.map { e -> e.toDomain() } }

    /** 获取所有 Tracker 在日期范围内的记录列表 */
    override fun getAllRecordsByDateRange(startDate: String, endDate: String): Flow<List<Record>> =
        recordDao.getAllRecordsByDateRange(startDate, endDate).map { it.map { e -> e.toDomain() } }

    /** 新增记录，返回自增 ID，并触发当日统计重算 */
    override suspend fun insertRecord(record: Record): Long {
        val id = recordDao.insert(record.toEntity())
        statsRepository.recalculateDailyStats(record.trackerId, record.date)
        return id
    }

    /** 更新记录，并触发当日统计重算 */
    override suspend fun updateRecord(record: Record) {
        recordDao.update(record.toEntity())
        statsRepository.recalculateDailyStats(record.trackerId, record.date)
    }

    /** 根据 ID 删除记录，并触发当日统计重算 */
    override suspend fun deleteRecord(id: Long) {
        // 先查询记录以获取 trackerId 和 date，用于后续统计重算
        val entity = recordDao.getRecordById(id)
        recordDao.deleteById(id)
        entity?.let { statsRepository.recalculateDailyStats(it.trackerId, it.date) }
    }

    /** 根据 ID 获取记录（一次性查询） */
    override suspend fun getRecordById(id: Long): Record? =
        recordDao.getRecordById(id)?.toDomain()
}
