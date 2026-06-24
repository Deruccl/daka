package com.timemark.app.domain.usecase.ai

import com.timemark.app.domain.repository.AIConfigRepository
import javax.inject.Inject

/**
 * 删除 AI 配置用例
 */
class DeleteAIConfigUseCase @Inject constructor(
    private val aiConfigRepository: AIConfigRepository
) {
    /** 删除指定 id 的 AI 配置 */
    suspend operator fun invoke(id: Long) {
        aiConfigRepository.deleteConfig(id)
    }
}
