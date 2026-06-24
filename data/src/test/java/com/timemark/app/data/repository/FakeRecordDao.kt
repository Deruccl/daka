package com.timemark.app.data.repository

import com.timemark.app.data.db.dao.RecordDao
import com.timemark.app.data.db.entity.RecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * RecordDao 的内存假实现，用于单元测试。
 */
class FakeRecordDao : RecordDao {

    private val _entities = MutableStateFlow<List<RecordEntity>>(emptyList())
    private var nextId = 1L

    /** 预设初始数据 */
    fun setEntities(list: List<RecordEntity>) {
        _entities.value = list
        nextId = (list.maxOfOrNull { it.id } ?: 0) + 1
    }

    /** 获取当前所有记录（用于测试断言） */
    fun getAll(): List<RecordEntity> = _entities.value

    override fun getRecordsByTrackerAndDate(trackerId: Long, date: String): Flow<List<RecordEntity>> =
        _entities.map { it.filter { e -> e.trackerId == trackerId && e.date == date } }

    override fun getRecordsByTrackerAndDateRange(
        trackerId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<RecordEntity>> =
        _entities.map { it.filter { e -> e.trackerId == trackerId && e.date in startDate..endDate } }

    override fun getAllRecordsByDate(date: String): Flow<List<RecordEntity>> =
        _entities.map { it.filter { e -> e.date == date } }

    override fun getAllRecordsByDateRange(startDate: String, endDate: String): Flow<List<RecordEntity>> =
        _entities.map { it.filter { e -> e.date in startDate..endDate } }

    override suspend fun getRecordById(id: Long): RecordEntity? =
        _entities.value.find { it.id == id }

    override suspend fun insert(record: RecordEntity): Long {
        val newId = if (record.id == 0L) nextId++ else record.id
        _entities.value = _entities.value + record.copy(id = newId)
        return newId
    }

    override suspend fun update(record: RecordEntity) {
        _entities.value = _entities.value.map {
            if (it.id == record.id) record else it
        }
    }

    override suspend fun delete(record: RecordEntity) {
        _entities.value = _entities.value.filter { it.id != record.id }
    }

    override suspend fun deleteById(id: Long) {
        _entities.value = _entities.value.filter { it.id != id }
    }

    override suspend fun deleteByTracker(trackerId: Long) {
        _entities.value = _entities.value.filter { it.trackerId != trackerId }
    }

    override suspend fun deleteByTrackerAndDateRange(
        trackerId: Long,
        startDate: String,
        endDate: String
    ) {
        _entities.value = _entities.value.filter {
            !(it.trackerId == trackerId && it.date in startDate..endDate)
        }
    }

    override suspend fun getDailyTotal(trackerId: Long, date: String): Double? =
        _entities.value
            .filter { it.trackerId == trackerId && it.date == date }
            .sumOf { it.value }

    override suspend fun getDailyCount(trackerId: Long, date: String): Int =
        _entities.value.count { it.trackerId == trackerId && it.date == date }
}
