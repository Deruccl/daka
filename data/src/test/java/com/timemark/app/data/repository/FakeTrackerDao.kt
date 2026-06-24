package com.timemark.app.data.repository

import com.timemark.app.data.db.dao.TrackerDao
import com.timemark.app.data.db.entity.TrackerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * TrackerDao 的内存假实现，用于单元测试。
 */
class FakeTrackerDao : TrackerDao {

    private val _entities = MutableStateFlow<List<TrackerEntity>>(emptyList())
    private var nextId = 1L

    /** 预设初始数据 */
    fun setEntities(list: List<TrackerEntity>) {
        _entities.value = list
        nextId = (list.maxOfOrNull { it.id } ?: 0) + 1
    }

    override fun getAllTrackers(): Flow<List<TrackerEntity>> =
        _entities.map { it.sortedBy { e -> e.sortOrder } }

    override fun getVisibleTrackers(): Flow<List<TrackerEntity>> =
        _entities.map { it.filter { e -> e.isVisible }.sortedBy { e -> e.sortOrder } }

    override fun getTrackerById(id: Long): Flow<TrackerEntity?> =
        _entities.map { it.find { e -> e.id == id } }

    override suspend fun getTrackerByIdOnce(id: Long): TrackerEntity? =
        _entities.value.find { it.id == id }

    override suspend fun insert(tracker: TrackerEntity): Long {
        val newId = if (tracker.id == 0L) nextId++ else tracker.id
        _entities.value = _entities.value + tracker.copy(id = newId)
        return newId
    }

    override suspend fun update(tracker: TrackerEntity) {
        _entities.value = _entities.value.map {
            if (it.id == tracker.id) tracker else it
        }
    }

    override suspend fun delete(tracker: TrackerEntity) {
        _entities.value = _entities.value.filter { it.id != tracker.id }
    }

    override suspend fun deleteById(id: Long) {
        _entities.value = _entities.value.filter { it.id != id }
    }

    override suspend fun updateSortOrder(id: Long, order: Int) {
        _entities.value = _entities.value.map {
            if (it.id == id) it.copy(sortOrder = order) else it
        }
    }

    override suspend fun updateSortOrders(orders: List<Pair<Long, Int>>) {
        _entities.value = _entities.value.map { entity ->
            val newOrder = orders.find { it.first == entity.id }?.second ?: entity.sortOrder
            entity.copy(sortOrder = newOrder)
        }
    }
}
