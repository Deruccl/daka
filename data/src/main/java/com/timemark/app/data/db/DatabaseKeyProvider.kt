package com.timemark.app.data.db

import com.timemark.app.data.datastore.SettingsDataStore
import com.timemark.app.data.security.KeystoreCrypto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据库加密密钥提供者（Task 32.3）
 *
 * 职责：
 * - 从 Keystore 获取或生成数据库加密密钥
 * - 密钥使用 KeystoreCrypto 加密后存储在 SettingsDataStore
 * - 提供 SQLCipher 所需的 passphrase（ByteArray）
 *
 * 工作流程：
 * 1. 首次启用加密时，生成 32 字节随机密钥
 * 2. 使用 KeystoreCrypto 加密密钥并存储到 SettingsDataStore
 * 3. 后续读取时从 SettingsDataStore 获取密文，经 KeystoreCrypto 解密后返回
 *
 * 注意：由于 Room 数据库在应用启动时创建，使用 runBlocking 同步读取设置。
 */
@Singleton
class DatabaseKeyProvider @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {

    /**
     * 获取或创建数据库加密密钥。
     * 如果 SettingsDataStore 中不存在，则生成新密钥并存储。
     *
     * @return 加密密钥的明文 ByteArray（用于 SQLCipher SupportFactory）
     */
    fun getOrCreatePassphrase(): ByteArray {
        return runBlocking {
            val existingEncrypted = settingsDataStore.databasePassword.first()
            if (existingEncrypted != null) {
                // 已有密钥，解密返回
                KeystoreCrypto.decrypt(existingEncrypted).toByteArray(Charsets.UTF_8)
            } else {
                // 生成新密钥
                val newPassphrase = generateRandomPassphrase()
                val passphraseString = newPassphrase.joinToString("") { "%02x".format(it) }
                // 加密后存储
                settingsDataStore.setDatabasePassword(passphraseString)
                newPassphrase
            }
        }
    }

    /**
     * 检查数据库加密是否已启用。
     */
    fun isEncryptionEnabled(): Boolean {
        return runBlocking {
            settingsDataStore.databaseEncryptionEnabled.first()
        }
    }

    /**
     * 启用数据库加密并生成密钥。
     * 如果已有密钥则复用，否则生成新密钥。
     */
    fun enableEncryption() {
        runBlocking {
            // 确保密钥已生成
            getOrCreatePassphrase()
            settingsDataStore.setDatabaseEncryptionEnabled(true)
        }
    }

    /**
     * 禁用数据库加密。
     * 注意：禁用后已有加密数据库无法直接打开，需要清除数据或迁移。
     */
    fun disableEncryption() {
        runBlocking {
            settingsDataStore.setDatabaseEncryptionEnabled(false)
        }
    }

    /**
     * 生成 32 字节随机密钥（256 位，符合 SQLCipher 推荐长度）。
     * 使用 SecureRandom 保证密码学安全。
     */
    private fun generateRandomPassphrase(): ByteArray {
        val random = SecureRandom()
        val passphrase = ByteArray(PASSPHRASE_LENGTH_BYTES)
        random.nextBytes(passphrase)
        return passphrase
    }

    companion object {
        /** SQLCipher 推荐密钥长度：32 字节（256 位） */
        private const val PASSPHRASE_LENGTH_BYTES = 32
    }
}
