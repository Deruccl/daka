package com.timemark.app.core.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 触觉反馈分级
 *
 * 定义不同交互场景对应的振动强度与模式：
 * - LIGHT：轻微点击，用于按钮点击
 * - MEDIUM：中等点击，用于卡片点击
 * - STRONG：强烈点击，用于长按
 * - SUCCESS：成功模式，用于操作成功
 * - WARNING：警告模式，用于警告提示
 * - ERROR：错误模式，用于操作失败
 */
enum class HapticLevel {
    /** 轻微点击（按钮点击） */
    LIGHT,

    /** 中等点击（卡片点击） */
    MEDIUM,

    /** 强烈点击（长按） */
    STRONG,

    /** 成功模式（操作成功） */
    SUCCESS,

    /** 警告模式（警告提示） */
    WARNING,

    /** 错误模式（操作失败） */
    ERROR
}

/**
 * 触觉反馈控制器
 *
 * 封装 Android Vibrator/VibratorManager，提供分级触觉反馈。
 * - Android 12+（API 31+）使用 VibrationEffect 预定义效果
 * - 低版本使用 VibrationEffect.createOneShot/createWaveform 降级
 *
 * @param context 应用上下文
 */
class HapticFeedbackController(private val context: Context) {

    // 获取 Vibrator 实例（API 31+ 使用 VibratorManager，低版本使用 Vibrator）
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * 执行触觉反馈
     *
     * 根据级别选择不同的振动效果：
     * - LIGHT: EFFECT_TICK（轻微）
     * - MEDIUM: EFFECT_CLICK（中等）
     * - STRONG: EFFECT_HEAVY_CLICK（强烈）
     * - SUCCESS/WARNING/ERROR: 预定义波形模式
     *
     * @param level 触觉反馈级别
     */
    fun performHaptic(level: HapticLevel) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        when (level) {
            HapticLevel.LIGHT -> performPredefined(vib, VibrationEffect.EFFECT_TICK, 20)
            HapticLevel.MEDIUM -> performPredefined(vib, VibrationEffect.EFFECT_CLICK, 30)
            HapticLevel.STRONG -> performPredefined(vib, VibrationEffect.EFFECT_HEAVY_CLICK, 50)
            HapticLevel.SUCCESS -> performPattern(vib, SUCCESS_PATTERN)
            HapticLevel.WARNING -> performPattern(vib, WARNING_PATTERN)
            HapticLevel.ERROR -> performPattern(vib, ERROR_PATTERN)
        }
    }

    /**
     * 执行预定义效果（API 29+）
     *
     * 低版本降级为单次振动。
     */
    private fun performPredefined(vib: Vibrator, effectId: Int, fallbackDurationMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vib.vibrate(VibrationEffect.createPredefined(effectId))
        } else {
            // API 26-28 降级为单次振动
            @Suppress("DEPRECATION")
            vib.vibrate(VibrationEffect.createOneShot(fallbackDurationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    /**
     * 执行波形振动模式
     */
    private fun performPattern(vib: Vibrator, pattern: LongArray) {
        vib.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    companion object {
        /** 成功模式：短-停-短（轻快） */
        private val SUCCESS_PATTERN = longArrayOf(0, 40, 60, 30)

        /** 警告模式：中-停-中（提醒） */
        private val WARNING_PATTERN = longArrayOf(0, 80, 50, 80)

        /** 错误模式：长-停-长-停-长（沉重） */
        private val ERROR_PATTERN = longArrayOf(0, 150, 80, 150, 80, 150)
    }
}

/**
 * 记住触觉反馈控制器
 *
 * 在 Composable 中获取 [HapticFeedbackController] 实例。
 * 控制器在首次组合时创建，后续重组复用。
 *
 * @return 触觉反馈控制器
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackController {
    val context = LocalContext.current
    return remember(context) { HapticFeedbackController(context) }
}
