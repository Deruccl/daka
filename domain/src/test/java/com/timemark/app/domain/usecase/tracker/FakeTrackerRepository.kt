package com.timemark.app.domain.usecase.tracker

import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * TrackerRepository 的内存假实现，用于单元测试。
 *
 * 维护一个内存列表，模拟数据库操作。
 */
class FakeTrackerRepository : TrackerRepository {

    private val _trackers = MutableStateFlow<List<Tracker>>(emptyList())
    val trackers: Flow<List<Tracker>> = _trackers.asStateFlow()

    private var nextId = 1L

    /** 预设初始数据 */
    fun setTrackers(list: List<Tracker>) {
        _trackers.value = list
        nextId = (list.maxOfOrNull { it.id } ?: 0) + 1
    }

    override fun getAllTrackers(): Flow<List<Tracker>> =
        _trackers.map { it.sortedBy { t -> t.sortOrder } }

    override fun getVisibleTrackers(): Flow<List<Tracker>> =
        _trackers.map { it.filter { t -> t.isVisible }.sortedBy { t -> t.sortOrder } }

    override fun getTrackerById(id: Long): Flow<Tracker?> =
        _trackers.map { it.find { t -> t.id == id } }

    override suspend fun insertTracker(tracker: Tracker): Long {
        val newId = if (tracker.id == 0L) nextId++ else tracker.id
        val newTracker = tracker.copy(id = newId)
        _trackers.value = _trackers.value + newTracker
        return newId
    }

    override suspend fun updateTracker(tracker: Tracker) {
        _trackers.value = _trackers.value.map {
            if (it.id == tracker.id) tracker else it
        }
    }

    override suspend fun deleteTracker(id: Long) {
        _trackers.value = _trackers.value.filter { it.id != id }
    }

    override suspend fun updateSortOrder(orders: List<Pair<Long, Int>>) {
        _trackers.value = _trackers.value.map { tracker ->
            val newOrder = orders.find { it.first == tracker.id }?.second ?: tracker.sortOrder
            tracker.copy(sortOrder = newOrder)
        }
    }
}
