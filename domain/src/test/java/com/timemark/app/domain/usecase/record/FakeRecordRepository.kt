package com.timemark.app.domain.usecase.record

import com.timemark.app.domain.model.Record
import com.timemark.app.domain.repository.RecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * RecordRepository 的内存假实现，用于单元测试。
 */
class FakeRecordRepository : RecordRepository {

    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: Flow<List<Record>> = _records.asStateFlow()

    private var nextId = 1L

    /** 预设初始数据 */
    fun setRecords(list: List<Record>) {
        _records.value = list
        nextId = (list.maxOfOrNull { it.id } ?: 0) + 1
    }

    override fun getRecordsByTrackerAndDate(trackerId: Long, date: String): Flow<List<Record>> =
        _records.map { it.filter { r -> r.trackerId == trackerId && r.date == date } }

    override fun getRecordsByTrackerAndDateRange(
        trackerId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<Record>> =
        _records.map { it.filter { r -> r.trackerId == trackerId && r.date in startDate..endDate } }

    override fun getAllRecordsByDate(date: String): Flow<List<Record>> =
        _records.map { it.filter { r -> r.date == date } }

    override fun getAllRecordsByDateRange(startDate: String, endDate: String): Flow<List<Record>> =
        _records.map { it.filter { r -> r.date in startDate..endDate } }

    override suspend fun insertRecord(record: Record): Long {
        val newId = if (record.id == 0L) nextId++ else record.id
        _records.value = _records.value + record.copy(id = newId)
        return newId
    }

    override suspend fun updateRecord(record: Record) {
        _records.value = _records.value.map {
            if (it.id == record.id) record else it
        }
    }

    override suspend fun deleteRecord(id: Long) {
        _records.value = _records.value.filter { it.id != id }
    }

    override suspend fun getRecordById(id: Long): Record? =
        _records.value.find { it.id == id }
}
