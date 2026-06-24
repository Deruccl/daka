package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.repository.AIConfigRepository
import javax.inject.Inject

/**
 * 保存 AI 配置用例
 *
 * 插入新的 AI 配置，返回新记录 id。
 */
class SaveAIConfigUseCase @Inject constructor(
    private val aiConfigRepository: AIConfigRepository
) {
    /** 插入 AI 配置，返回新记录 id */
    suspend operator fun invoke(config: AIConfig): Long =
        aiConfigRepository.insertConfig(config)
}
