package com.timemark.app.ai

/**
 * AI 功能被禁用异常（Task 33.3）
 *
 * 当用户在设置中关闭了某个 AI 功能的独立开关时抛出。
 */
class FeatureDisabledException(
    val featureName: String,
    message: String = "AI 功能 $featureName 已被禁用"
) : Exception(message)

/**
 * WiFi 模式限制异常（Task 33.3）
 *
 * 当用户开启了"仅 WiFi 下使用"但当前不是 WiFi 网络时抛出。
 */
class WifiOnlyModeException(
    message: String = "当前不是 WiFi 网络，AI 功能不可用"
) : Exception(message)
