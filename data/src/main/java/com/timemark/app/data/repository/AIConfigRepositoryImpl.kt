package com.timemark.app.data.repository

import com.timemark.app.data.db.dao.AIConfigDao
import com.timemark.app.data.db.entity.AIConfigEntity
import com.timemark.app.data.mapper.toDomain
import com.timemark.app.data.mapper.toEntity
import com.timemark.app.data.security.KeystoreCrypto
import com.timemark.app.domain.model.AIConfig
import com.timemark.app.domain.model.AIFeature
import com.timemark.app.domain.repository.AIConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * AIConfigRepository 接口实现。
 * 通过 AIConfigDao 访问数据库，使用 AIMapper 完成 Entity/Domain 模型互转。
 *
 * 注意：API Key 在数据库中以密文存储，读取时解密、写入时加密，
 * 加解密通过 KeystoreCrypto 完成。若加解密失败则保留原值，避免阻断业务流程。
 */
class AIConfigRepositoryImpl @Inject constructor(
    private val aiConfigDao: AIConfigDao
) : AIConfigRepository {

    /** 获取所有 AI 配置（按 priority 升序），解密 API Key */
    override fun getAllConfigs(): Flow<List<AIConfig>> =
        aiConfigDao.getAllConfigs().map { entities -> entities.map { it.toDomainDecrypted() } }

    /** 获取所有已启用的 AI 配置，解密 API Key */
    override fun getEnabledConfigs(): Flow<List<AIConfig>> =
        aiConfigDao.getEnabledConfigs().map { entities -> entities.map { it.toDomainDecrypted() } }

    /** 根据 ID 获取 AI 配置，解密 API Key */
    override fun getConfigById(id: Long): Flow<AIConfig?> =
        aiConfigDao.getConfigById(id).map { it?.toDomainDecrypted() }

    /** 获取支持指定功能且已启用的 AI 配置列表，解密 API Key */
    override fun getConfigsByFeature(feature: AIFeature): Flow<List<AIConfig>> =
        aiConfigDao.getConfigsByFeature(feature.name)
            .map { entities -> entities.map { it.toDomainDecrypted() } }

    /** 获取默认多模态配置，解密 API Key */
    override fun getDefaultMultimodalConfig(): Flow<AIConfig?> =
        aiConfigDao.getDefaultMultimodalConfig().map { it?.toDomainDecrypted() }

    /** 获取默认文本配置，解密 API Key */
    override fun getDefaultTextConfig(): Flow<AIConfig?> =
        aiConfigDao.getDefaultTextConfig().map { it?.toDomainDecrypted() }

    /** 新增 AI 配置，加密 API Key 后入库，返回自增 ID */
    override suspend fun insertConfig(config: AIConfig): Long =
        aiConfigDao.insert(config.toEntityEncrypted())

    /** 更新 AI 配置，加密 API Key 后入库 */
    override suspend fun updateConfig(config: AIConfig) =
        aiConfigDao.update(config.toEntityEncrypted())

    /** 根据 ID 删除 AI 配置 */
    override suspend fun deleteConfig(id: Long) =
        aiConfigDao.deleteById(id)

    /** 批量更新优先级 */
    override suspend fun updatePriority(orders: List<Pair<Long, Int>>) {
        orders.forEach { (id, priority) -> aiConfigDao.updatePriority(id, priority) }
    }

    /**
     * 将 Entity 转为 Domain 模型并解密 API Key。
     * 若解密失败（如 Keystore 不可用或密文损坏），保留原值。
     */
    private fun AIConfigEntity.toDomainDecrypted(): AIConfig {
        val domain = this.toDomain()
        if (domain.apiKey.isEmpty()) return domain
        val decrypted = runCatching { KeystoreCrypto.decrypt(domain.apiKey) }
            .getOrDefault(domain.apiKey)
        return domain.copy(apiKey = decrypted)
    }

    /**
     * 将 Domain 模型转为 Entity 并加密 API Key。
     * 若加密失败，保留原值。
     */
    private fun AIConfig.toEntityEncrypted(): AIConfigEntity {
        if (apiKey.isEmpty()) return this.toEntity()
        val encrypted = runCatching { KeystoreCrypto.encrypt(apiKey) }
            .getOrDefault(apiKey)
        return this.copy(apiKey = encrypted).toEntity()
    }
}
