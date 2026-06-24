package com.timemark.app.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

/**
 * Token 优化器（Task 33.4）
 *
 * 提供 4 项 Token 优化能力：
 * 1. 提示词精简：去除多余空白、合并重复字符、使用简写
 * 2. 图片压缩：JPEG 质量可调
 * 3. 智能截断：在保留关键信息的前提下截断超长文本
 * 4. Token 数估算：粗略 4 字符 = 1 token
 */
object TokenOptimizer {

    /** 估算 Token 数（粗略：4 字符 ≈ 1 token，中文按 2 字符 ≈ 1 token） */
    fun estimateTokenCount(text: String): Int {
        if (text.isEmpty()) return 0
        var cjkCount = 0
        var otherCount = 0
        for (ch in text) {
            val code = ch.code
            // CJK 统一汉字、扩展区等
            if (code in 0x4E00..0x9FFF || code in 0x3400..0x4DBF ||
                code in 0x3000..0x303F || code in 0xFF00..0xFFEF
            ) {
                cjkCount++
            } else {
                otherCount++
            }
        }
        // 中文约 2 字符 1 token，英文约 4 字符 1 token
        return (cjkCount / 2.0 + otherCount / 4.0).toInt().coerceAtLeast(1)
    }

    /**
     * 精简提示词
     *
     * 优化策略：
     * - 去除行首行尾空白
     * - 合并连续空格为单个空格
     * - 合并连续空行为单个空行
     * - 去除重复的标点（如 ！！！ -> ！）
     */
    fun compressPrompt(prompt: String): String {
        if (prompt.isEmpty()) return prompt
        return prompt
            // 统一换行符
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            // 去除每行首尾空白
            .lines()
            .joinToString("\n") { it.trim() }
            // 合并连续空格
            .replace(Regex("[ ]{2,}"), " ")
            // 合并连续空行（3 个及以上换行 -> 2 个）
            .replace(Regex("\n{3,}"), "\n\n")
            // 合并重复标点（仅常见标点）
            .replace(Regex("([！？。，、；：…])\\1+"), "$1")
            .trim()
    }

    /**
     * 图片压缩
     *
     * 将 Bitmap 压缩为 JPEG 字节数组。
     *
     * @param bitmap 原始位图
     * @param quality JPEG 质量 50-100，默认 80
     * @return 压缩后的字节数组
     */
    fun compressImage(bitmap: Bitmap, quality: Int = 80): ByteArray {
        val clampedQuality = quality.coerceIn(50, 100)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, clampedQuality, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * 从图片字节数组解码为 Bitmap 并按最大尺寸缩放
     *
     * @param imageBytes 原始图片字节数组
     * @param maxDimension 最大边长（像素），默认 1024
     */
    fun decodeAndScale(imageBytes: ByteArray, maxDimension: Int = 1024): Bitmap? {
        // 先获取原始尺寸
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        val width = options.outWidth
        val height = options.outHeight
        if (width <= 0 || height <= 0) return null

        // 计算 inSampleSize
        var sampleSize = 1
        val maxSide = maxOf(width, height)
        if (maxSide > maxDimension) {
            sampleSize = maxSide / maxDimension
            if (sampleSize < 1) sampleSize = 1
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, decodeOptions)
    }

    /**
     * 智能截断
     *
     * 在保留关键信息的前提下截断超长文本：
     * - 优先保留前文（系统提示与上下文）
     * - 在句子边界（。！？.!?）截断
     * - 末尾添加省略号
     *
     * @param text 原始文本
     * @param maxTokens 最大 Token 数
     */
    fun smartTruncate(text: String, maxTokens: Int): String {
        if (maxTokens <= 0) return ""
        if (estimateTokenCount(text) <= maxTokens) return text

        // 粗略估算字符数上限（按平均 3 字符/token）
        val maxChars = maxTokens * 3
        if (text.length <= maxChars) return text

        // 在 maxChars 范围内寻找最近的句子边界
        val sub = text.substring(0, maxChars.coerceAtMost(text.length))
        val sentenceEnders = listOf('。', '！', '？', '.', '!', '?', '\n')
        var cutIndex = -1
        for (i in sub.length - 1 downTo 0) {
            if (sub[i] in sentenceEnders) {
                cutIndex = i + 1
                break
            }
        }
        val result = if (cutIndex > 0) sub.substring(0, cutIndex) else sub
        return result.trimEnd() + "…"
    }
}
