package com.timemark.app.domain.usecase.tracker

import com.timemark.app.domain.repository.RecordRepository
import com.timemark.app.domain.repository.TrackerRepository
import javax.inject.Inject

/**
 * 删除打卡项目用例
 *
 * 删除打卡项目时，关联的打卡记录会通过外键 CASCADE 自动删除。
 */
class DeleteTrackerUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository,
    private val recordRepository: RecordRepository
) {
    /** 删除指定 id 的打卡项目（记录会通过外键 CASCADE 自动删除） */
    suspend operator fun invoke(trackerId: Long) {
        trackerRepository.deleteTracker(trackerId)
    }
}
