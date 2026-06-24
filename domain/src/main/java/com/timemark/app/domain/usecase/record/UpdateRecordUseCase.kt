package com.timemark.app.domain.usecase.record

import com.timemark.app.domain.model.Record
import com.timemark.app.domain.repository.RecordRepository
import javax.inject.Inject

/**
 * 更新打卡记录用例
 */
class UpdateRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    /** 更新指定打卡记录 */
    suspend operator fun invoke(record: Record) =
        recordRepository.updateRecord(record)
}
