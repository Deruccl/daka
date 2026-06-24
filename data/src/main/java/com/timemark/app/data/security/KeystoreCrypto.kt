package com.timemark.app.data.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Keystore 加密工具。
 * 基于 Android Keystore 系统使用 AES/GCM/NoPadding 算法加解密敏感数据（如密码、API Key）。
 * 密钥保存在 AndroidKeystore 中，不需要用户认证。
 */
object KeystoreCrypto {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "timemark_master_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12 // bytes
    private const val GCM_TAG_LENGTH = 128 // bits

    /**
     * 检查 Keystore 是否可用。
     * AndroidKeyStore 自 API 18 起可用，AES/GCM 自 API 23 起受支持。
     */
    fun isAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    /**
     * 获取或创建主密钥。若 Keystore 中不存在指定别名则生成新密钥。
     */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        keyGen.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGen.generateKey()
    }

    /**
     * 加密明文，返回 Base64 字符串。
     * 内部生成随机 IV，最终输出 = Base64(IV + 密文 + GCM Tag)。
     */
    fun encrypt(plainText: String): String {
        require(plainText.isNotEmpty()) { "待加密文本不能为空" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + cipherBytes.size).apply {
            System.arraycopy(iv, 0, this, 0, iv.size)
            System.arraycopy(cipherBytes, 0, this, iv.size, cipherBytes.size)
        }
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * 解密 Base64 密文字符串，返回明文。
     * 输入格式需为 Base64(IV + 密文 + GCM Tag)。
     */
    fun decrypt(cipherText: String): String {
        require(cipherText.isNotEmpty()) { "待解密文本不能为空" }
        val combined = Base64.decode(cipherText, Base64.NO_WRAP)
        require(combined.size > GCM_IV_LENGTH) { "密文格式无效" }

        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }
}
