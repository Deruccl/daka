package com.timemark.app.domain.usecase.tracker

import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.repository.TrackerRepository
import javax.inject.Inject

/**
 * 创建打卡项目用例
 */
class CreateTrackerUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    /** 插入新的打卡项目，返回新记录 id */
    suspend operator fun invoke(tracker: Tracker): Long =
        trackerRepository.insertTracker(tracker)
}
