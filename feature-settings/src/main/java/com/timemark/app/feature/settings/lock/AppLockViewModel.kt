package com.timemark.app.feature.settings.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.model.LockMethod
import com.timemark.app.domain.usecase.settings.GetSettingsUseCase
import com.timemark.app.domain.usecase.settings.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用锁设置 ViewModel（Task 32.1）
 *
 * 管理应用锁状态：
 * - 设置/修改/关闭密码
 * - 指纹/面部识别开关
 * - 自动锁定时间选择
 * - 密码验证
 *
 * 密码通过 UpdateSettingsUseCase 写入，内部经 KeystoreCrypto 加密后存储。
 */
@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {

    /** 应用锁开关 */
    val appLockEnabled: StateFlow<Boolean> = getSettingsUseCase.appLockEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 应用锁方式 */
    val lockMethod: StateFlow<LockMethod> = getSettingsUseCase.lockMethod()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LockMethod.NONE)

    /** 生物识别开关 */
    val biometricEnabled: StateFlow<Boolean> = getSettingsUseCase.biometricEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 自动锁定时间（分钟） */
    val autoLockMinutes: StateFlow<Int> = getSettingsUseCase.autoLockMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    /** 是否已设置密码 */
    val hasPassword: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        getSettingsUseCase.lockPassword(),
        getSettingsUseCase.appLockEnabled()
    ) { password, enabled -> !password.isNullOrEmpty() || enabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 设置应用锁开关 */
    fun setAppLockEnabled(enabled: Boolean) = viewModelScope.launch {
        updateSettingsUseCase.appLockEnabled(enabled)
    }

    /** 设置应用锁方式 */
    fun setLockMethod(method: LockMethod) = viewModelScope.launch {
        updateSettingsUseCase.lockMethod(method)
    }

    /** 设置生物识别开关 */
    fun setBiometricEnabled(enabled: Boolean) = viewModelScope.launch {
        updateSettingsUseCase.biometricEnabled(enabled)
    }

    /** 设置自动锁定时间 */
    fun setAutoLockMinutes(minutes: Int) = viewModelScope.launch {
        updateSettingsUseCase.autoLockMinutes(minutes)
    }

    /**
     * 设置应用锁密码。
     * 传入空字符串或 null 清除密码。
     */
    fun setPassword(password: String?) = viewModelScope.launch {
        updateSettingsUseCase.lockPassword(password)
    }

    /**
     * 验证密码是否正确。
     * @param input 用户输入的密码
     * @return true 表示验证通过
     */
    suspend fun verifyPassword(input: String): Boolean {
        val storedPassword = getSettingsUseCase.lockPassword().first()
        return !storedPassword.isNullOrEmpty() && storedPassword == input
    }

    /**
     * 启用应用锁并设置密码。
     * 同时将 lockMethod 设为 PASSWORD。
     */
    fun enablePasswordLock(password: String) = viewModelScope.launch {
        updateSettingsUseCase.lockPassword(password)
        updateSettingsUseCase.lockMethod(LockMethod.PASSWORD)
        updateSettingsUseCase.appLockEnabled(true)
    }

    /**
     * 关闭应用锁。
     * 清除密码、关闭开关、重置锁方式。
     */
    fun disableAppLock() = viewModelScope.launch {
        updateSettingsUseCase.lockPassword(null)
        updateSettingsUseCase.appLockEnabled(false)
        updateSettingsUseCase.biometricEnabled(false)
        updateSettingsUseCase.lockMethod(LockMethod.NONE)
    }
}
