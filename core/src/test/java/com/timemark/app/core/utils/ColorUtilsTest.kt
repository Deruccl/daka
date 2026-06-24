package com.timemark.app.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * ColorUtils 单元测试
 *
 * 使用 Robolectric 提供 android.graphics.Color 的实现。
 * 覆盖颜色解析、格式化、变亮/变暗、透明度、混合等操作。
 */
@RunWith(RobolectricTestRunner::class)
class ColorUtilsTest {

    @Test
    fun parseColor_六位十六进制_返回不透明颜色() {
        // #FF0000 纯红色
        val color = ColorUtils.parseColor("#FF0000")
        assertEquals(255, android.graphics.Color.red(color))
        assertEquals(0, android.graphics.Color.green(color))
        assertEquals(0, android.graphics.Color.blue(color))
        assertEquals(255, android.graphics.Color.alpha(color))
    }

    @Test
    fun parseColor_无井号六位_正确解析() {
        val color = ColorUtils.parseColor("00FF00")
        assertEquals(0, android.graphics.Color.red(color))
        assertEquals(255, android.graphics.Color.green(color))
        assertEquals(0, android.graphics.Color.blue(color))
    }

    @Test
    fun parseColor_八位带透明度_正确解析() {
        val color = ColorUtils.parseColor("#80FF0000")
        assertEquals(128, android.graphics.Color.alpha(color))
        assertEquals(255, android.graphics.Color.red(color))
    }

    @Test
    fun parseColor_非法长度_抛出异常() {
        assertThrows(IllegalArgumentException::class.java) {
            ColorUtils.parseColor("#12345")
        }
    }

    @Test
    fun parseColor_空字符串_抛出异常() {
        assertThrows(IllegalArgumentException::class.java) {
            ColorUtils.parseColor("")
        }
    }

    @Test
    fun toHexString_不含Alpha_返回六位格式() {
        val color = android.graphics.Color.parseColor("#FF0000")
        val hex = ColorUtils.toHexString(color, includeAlpha = false)
        assertEquals("#FF0000", hex)
    }

    @Test
    fun toHexString_包含Alpha_返回八位格式() {
        val color = android.graphics.Color.parseColor("#80FF0000")
        val hex = ColorUtils.toHexString(color, includeAlpha = true)
        assertEquals("#80FF0000", hex)
    }

    @Test
    fun lighten_因子为0_返回原色() {
        val original = android.graphics.Color.parseColor("#808080")
        val result = ColorUtils.lighten(original, 0f)
        assertEquals(android.graphics.Color.red(original), android.graphics.Color.red(result))
        assertEquals(android.graphics.Color.green(original), android.graphics.Color.green(result))
        assertEquals(android.graphics.Color.blue(original), android.graphics.Color.blue(result))
    }

    @Test
    fun lighten_因子为1_返回白色() {
        val original = android.graphics.Color.parseColor("#000000")
        val result = ColorUtils.lighten(original, 1f)
        assertEquals(255, android.graphics.Color.red(result))
        assertEquals(255, android.graphics.Color.green(result))
        assertEquals(255, android.graphics.Color.blue(result))
    }

    @Test
    fun lighten_因子为05_颜色变亮() {
        val original = android.graphics.Color.parseColor("#000000")
        val result = ColorUtils.lighten(original, 0.5f)
        // 变亮后 RGB 应在 127 左右
        assertEquals(127, android.graphics.Color.red(result))
        assertEquals(127, android.graphics.Color.green(result))
        assertEquals(127, android.graphics.Color.blue(result))
    }

    @Test
    fun lighten_因子超过1_被限制为1() {
        val original = android.graphics.Color.parseColor("#000000")
        val result = ColorUtils.lighten(original, 2f)
        assertEquals(255, android.graphics.Color.red(result))
    }

    @Test
    fun darken_因子为0_返回原色() {
        val original = android.graphics.Color.parseColor("#808080")
        val result = ColorUtils.darken(original, 0f)
        assertEquals(android.graphics.Color.red(original), android.graphics.Color.red(result))
    }

    @Test
    fun darken_因子为1_返回黑色() {
        val original = android.graphics.Color.parseColor("#FFFFFF")
        val result = ColorUtils.darken(original, 1f)
        assertEquals(0, android.graphics.Color.red(result))
        assertEquals(0, android.graphics.Color.green(result))
        assertEquals(0, android.graphics.Color.blue(result))
    }

    @Test
    fun darken_因子为05_颜色变暗() {
        val original = android.graphics.Color.parseColor("#FFFFFF")
        val result = ColorUtils.darken(original, 0.5f)
        assertEquals(127, android.graphics.Color.red(result))
        assertEquals(127, android.graphics.Color.green(result))
        assertEquals(127, android.graphics.Color.blue(result))
    }

    @Test
    fun withAlpha_正常值_正确设置透明度() {
        val color = android.graphics.Color.parseColor("#FF0000")
        val result = ColorUtils.withAlpha(color, 0.5f)
        assertEquals(127, android.graphics.Color.alpha(result))
        assertEquals(255, android.graphics.Color.red(result))
    }

    @Test
    fun withAlpha_值为0_完全透明() {
        val color = android.graphics.Color.parseColor("#FF0000")
        val result = ColorUtils.withAlpha(color, 0f)
        assertEquals(0, android.graphics.Color.alpha(result))
    }

    @Test
    fun withAlpha_值为1_完全不透明() {
        val color = android.graphics.Color.parseColor("#FF0000")
        val result = ColorUtils.withAlpha(color, 1f)
        assertEquals(255, android.graphics.Color.alpha(result))
    }

    @Test
    fun blend_比例为0_返回第二个颜色() {
        val color1 = android.graphics.Color.parseColor("#FF0000")
        val color2 = android.graphics.Color.parseColor("#00FF00")
        val result = ColorUtils.blend(color1, color2, 0f)
        assertEquals(android.graphics.Color.red(color2), android.graphics.Color.red(result))
        assertEquals(android.graphics.Color.green(color2), android.graphics.Color.green(result))
        assertEquals(android.graphics.Color.blue(color2), android.graphics.Color.blue(result))
    }

    @Test
    fun blend_比例为1_返回第一个颜色() {
        val color1 = android.graphics.Color.parseColor("#FF0000")
        val color2 = android.graphics.Color.parseColor("#00FF00")
        val result = ColorUtils.blend(color1, color2, 1f)
        assertEquals(android.graphics.Color.red(color1), android.graphics.Color.red(result))
        assertEquals(android.graphics.Color.green(color1), android.graphics.Color.green(result))
        assertEquals(android.graphics.Color.blue(color1), android.graphics.Color.blue(result))
    }

    @Test
    fun blend_比例为05_返回中间色() {
        val color1 = android.graphics.Color.parseColor("#000000")
        val color2 = android.graphics.Color.parseColor("#FFFFFF")
        val result = ColorUtils.blend(color1, color2, 0.5f)
        assertEquals(127, android.graphics.Color.red(result))
        assertEquals(127, android.graphics.Color.green(result))
        assertEquals(127, android.graphics.Color.blue(result))
    }

    @Test
    fun blend_比例超过1_被限制为1() {
        val color1 = android.graphics.Color.parseColor("#FF0000")
        val color2 = android.graphics.Color.parseColor("#00FF00")
        val result = ColorUtils.blend(color1, color2, 2f)
        assertEquals(android.graphics.Color.red(color1), android.graphics.Color.red(result))
    }
}
