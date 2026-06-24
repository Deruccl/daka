package com.timemark.app.domain.repository

import com.timemark.app.domain.model.Record
import kotlinx.coroutines.flow.Flow

/** 打卡记录仓库接口 */
interface RecordRepository {
    fun getRecordsByTrackerAndDate(trackerId: Long, date: String): Flow<List<Record>>
    fun getRecordsByTrackerAndDateRange(trackerId: Long, startDate: String, endDate: String): Flow<List<Record>>
    fun getAllRecordsByDate(date: String): Flow<List<Record>>
    fun getAllRecordsByDateRange(startDate: String, endDate: String): Flow<List<Record>>
    suspend fun insertRecord(record: Record): Long
    suspend fun updateRecord(record: Record)
    suspend fun deleteRecord(id: Long)
    suspend fun getRecordById(id: Long): Record?
}
