package com.timemark.app.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * 生物识别辅助工具（Task 32.1）
 *
 * 封装 BiometricPrompt 相关逻辑：
 * - 检查设备是否支持生物识别（指纹/面部）
 * - 提供 BiometricPrompt.PromptInfo 配置
 * - 发起生物识别验证
 *
 * 使用 androidx.biometric 库，兼容指纹与面部识别。
 */
object BiometricHelper {

    /**
     * 检查设备是否支持生物识别且用户已录入生物特征。
     * @return true 表示可以使用 BiometricPrompt 进行认证
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * 检查设备是否支持生物识别（不强求已录入）。
     */
    fun isBiometricSupported(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        val canAuth = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS ||
            canAuth == BiometricManager.BIOMETRIC_ERROR_NO_BIOMETRICS
    }

    /**
     * 创建默认的 PromptInfo 配置。
     * @param title 标题
     * @param subtitle 副标题
     * @param description 描述
     * @param negativeButtonText 取消按钮文字（设备不支持生物识别时使用）
     */
    fun createPromptInfo(
        title: String = "身份验证",
        subtitle: String = "请使用生物识别解锁应用",
        description: String = "需要验证您的身份以访问应用数据",
        negativeButtonText: String = "取消"
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setConfirmationRequired(false)
            .build()
    }

    /**
     * 发起生物识别认证。
     *
     * @param activity FragmentActivity（BiometricPrompt 需要 Fragment 上下文）
     * @param onSuccess 认证成功回调
     * @param onError 认证失败回调（errorCode + errString）
     * @param onFailed 认证失败（如指纹不匹配）回调
     */
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (Int, String) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = createPromptInfo()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errorCode, errString.toString())
                }

                override fun onAuthenticationFailed() {
                    onFailed()
                }
            }
        )
        biometricPrompt.authenticate(promptInfo)
    }
}
