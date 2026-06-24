package com.timemark.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.timemark.app.core.ui.theme.TimeMarkTheme
import com.timemark.app.domain.model.ThemeMode
import com.timemark.app.domain.repository.SettingsRepository
import com.timemark.app.feature.settings.lock.LockScreen
import com.timemark.app.ui.ScaffoldMain
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 主入口 Activity
 *
 * - 使用 @AndroidEntryPoint 启用 Hilt 注入
 * - 启用 edge-to-edge 显示，内容延伸至系统栏后方
 * - 状态栏设置为透明
 * - 根据 [SettingsRepository.themeMode] 动态切换浅色/深色/跟随系统主题
 * - 承载 [ScaffoldMain] 主框架（导航 + 底部导航栏）
 * - Task 32.1: 应用锁检查（onResume 时根据自动锁定时间判断是否需要锁定）
 * - Task 32.2: 安全屏幕（FLAG_SECURE 禁止截图与最近任务列表预览）
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    /** 上次暂停时间戳（用于自动锁定判断） */
    private var lastPausedTime: Long = 0L

    /** 锁屏状态 Flow，UI 订阅自动更新 */
    private val lockState = MutableStateFlow(false)

    /** IO 协程作用域，用于后台读取设置 */
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        // 启用 edge-to-edge 显示（在 super.onCreate 之前调用以确保生效）
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 状态栏透明，内容绘制到状态栏后方
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // Task 32.2: 根据安全屏幕设置应用 FLAG_SECURE
        applySecureFlagFromSettings()

        // Task 32.1: 初始化时检查是否需要锁定（首次启动）
        checkInitialLockState()

        setContent {
            // 观察主题模式 Flow，初始值使用 SYSTEM 避免首帧闪烁
            val themeMode by settingsRepository.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

            // Task 32.2: 观察安全屏幕设置
            val secureScreen by settingsRepository.secureScreen
                .collectAsStateWithLifecycle(initialValue = false)

            // 应用 FLAG_SECURE
            LaunchedEffect(secureScreen) {
                applySecureFlag(secureScreen)
            }

            // Task 32.1: 观察锁屏状态
            val isLocked by lockState.collectAsStateWithLifecycle()

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            TimeMarkTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLocked) {
                        // 显示锁屏页面
                        LockScreen(
                            onUnlocked = { lockState.value = false }
                        )
                    } else {
                        val navController = rememberNavController()
                        ScaffoldMain(navController = navController)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Task 32.1: 检查是否需要自动锁定
        checkAutoLock()
    }

    override fun onPause() {
        super.onPause()
        // 记录暂停时间，用于自动锁定判断
        lastPausedTime = System.currentTimeMillis()
    }

    /**
     * Task 32.1: 初始锁定状态检查。
     * 如果应用锁已启用，首次启动时需要锁定。
     */
    private fun checkInitialLockState() {
        ioScope.launch {
            try {
                val appLockEnabled = settingsRepository.appLockEnabled.first()
                if (appLockEnabled) {
                    lockState.value = true
                }
            } catch (e: Exception) {
                // 忽略异常，不阻塞启动
            }
        }
    }

    /**
     * Task 32.1: 检查自动锁定。
     * 根据 lastPausedTime 与 autoLockMinutes 判断是否超时需要锁定。
     */
    private fun checkAutoLock() {
        if (lastPausedTime == 0L) return
        ioScope.launch {
            try {
                val appLockEnabled = settingsRepository.appLockEnabled.first()
                if (!appLockEnabled) {
                    lastPausedTime = 0L
                    return@launch
                }
                val autoLockMinutes = settingsRepository.autoLockMinutes.first()
                val elapsedMinutes = (System.currentTimeMillis() - lastPausedTime) / (60 * 1000)
                if (elapsedMinutes >= autoLockMinutes) {
                    lockState.value = true
                }
                lastPausedTime = 0L
            } catch (e: Exception) {
                // 忽略异常
            }
        }
    }

    /**
     * Task 32.2: 从设置读取安全屏幕开关并应用 FLAG_SECURE。
     */
    private fun applySecureFlagFromSettings() {
        ioScope.launch {
            try {
                val secure = settingsRepository.secureScreen.first()
                runOnUiThread { applySecureFlag(secure) }
            } catch (e: Exception) {
                // 忽略异常
            }
        }
    }

    /**
     * Task 32.2: 应用或移除 FLAG_SECURE。
     * FLAG_SECURE 阻止截图并隐藏最近任务列表中的内容预览。
     */
    private fun applySecureFlag(enabled: Boolean) {
        if (enabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
