package com.timemark.app.domain.usecase.tracker

import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 查询打卡项目用例
 *
 * 提供多种查询方式：全部、可见、按 id 查询。
 */
class GetTrackersUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    /** 获取全部打卡项目 */
    operator fun invoke(): Flow<List<Tracker>> = trackerRepository.getAllTrackers()

    /** 获取首页可见的打卡项目 */
    fun visible(): Flow<List<Tracker>> = trackerRepository.getVisibleTrackers()

    /** 按 id 获取打卡项目 */
    fun byId(id: Long): Flow<Tracker?> = trackerRepository.getTrackerById(id)
}
