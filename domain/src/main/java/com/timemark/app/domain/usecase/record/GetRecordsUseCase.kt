package com.timemark.app.domain.usecase.record

import com.timemark.app.domain.model.Record
import com.timemark.app.domain.repository.RecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 查询打卡记录用例
 *
 * 提供多种查询方式：按项目+日期、按项目+日期范围、按日期全量、按日期范围全量、按 id。
 */
class GetRecordsUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    /** 按打卡项目和日期查询记录 */
    fun byTrackerAndDate(trackerId: Long, date: String): Flow<List<Record>> =
        recordRepository.getRecordsByTrackerAndDate(trackerId, date)

    /** 按打卡项目和日期范围查询记录 */
    fun byTrackerAndDateRange(trackerId: Long, startDate: String, endDate: String): Flow<List<Record>> =
        recordRepository.getRecordsByTrackerAndDateRange(trackerId, startDate, endDate)

    /** 查询某一天全部项目的记录 */
    fun allByDate(date: String): Flow<List<Record>> =
        recordRepository.getAllRecordsByDate(date)

    /** 查询某日期范围内全部项目的记录 */
    fun allByDateRange(startDate: String, endDate: String): Flow<List<Record>> =
        recordRepository.getAllRecordsByDateRange(startDate, endDate)

    /** 按 id 获取记录（一次性查询） */
    suspend fun byId(id: Long): Record? = recordRepository.getRecordById(id)
}
