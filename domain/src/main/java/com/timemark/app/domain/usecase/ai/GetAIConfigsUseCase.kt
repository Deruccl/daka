package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.repository.AIConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取 AI 配置用例
 *
 * 提供多种查询方式：全部、已启用、按 id、按功能。
 */
class GetAIConfigsUseCase @Inject constructor(
    private val aiConfigRepository: AIConfigRepository
) {
    /** 获取全部 AI 配置 */
    operator fun invoke(): Flow<List<AIConfig>> = aiConfigRepository.getAllConfigs()

    /** 获取已启用的 AI 配置 */
    fun enabled(): Flow<List<AIConfig>> = aiConfigRepository.getEnabledConfigs()

    /** 按 id 获取 AI 配置 */
    fun byId(id: Long): Flow<AIConfig?> = aiConfigRepository.getConfigById(id)

    /** 按功能获取 AI 配置 */
    fun byFeature(feature: AIFeature): Flow<List<AIConfig>> =
        aiConfigRepository.getConfigsByFeature(feature)
}
