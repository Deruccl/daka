package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.model.AIUsage
import com.timemark.app.domain.repository.AIUsageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取 AI 使用记录用例
 *
 * 提供多种查询方式：全部、今日、本周、本月、按配置、按功能。
 */
class GetAIUsageUseCase @Inject constructor(
    private val aiUsageRepository: AIUsageRepository
) {
    /** 获取全部 AI 使用记录 */
    fun all(): Flow<List<AIUsage>> = aiUsageRepository.getAllUsage()

    /** 获取今日 AI 使用记录 */
    fun today(): Flow<List<AIUsage>> = aiUsageRepository.getTodayUsage()

    /** 获取本周 AI 使用记录 */
    fun week(): Flow<List<AIUsage>> = aiUsageRepository.getWeekUsage()

    /** 获取本月 AI 使用记录 */
    fun month(): Flow<List<AIUsage>> = aiUsageRepository.getMonthUsage()

    /** 按配置 id 获取 AI 使用记录 */
    fun byConfig(configId: Long): Flow<List<AIUsage>> =
        aiUsageRepository.getUsageByConfig(configId)

    /** 按功能获取 AI 使用记录 */
    fun byFeature(feature: AIFeature): Flow<List<AIUsage>> =
        aiUsageRepository.getUsageByFeature(feature)
}
