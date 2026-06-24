package com.timemark.app.domain.usecase.record

import com.timemark.app.domain.repository.RecordRepository
import javax.inject.Inject

/**
 * 删除打卡记录用例
 */
class DeleteRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    /** 删除指定 id 的打卡记录 */
    suspend operator fun invoke(id: Long) {
        recordRepository.deleteRecord(id)
    }
}
