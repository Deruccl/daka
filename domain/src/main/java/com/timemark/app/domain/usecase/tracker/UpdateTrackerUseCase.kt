package com.timemark.app.domain.usecase.tracker

import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.repository.TrackerRepository
import javax.inject.Inject

/**
 * 更新打卡项目用例
 */
class UpdateTrackerUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    /** 更新指定打卡项目 */
    suspend operator fun invoke(tracker: Tracker) =
        trackerRepository.updateTracker(tracker)
}
