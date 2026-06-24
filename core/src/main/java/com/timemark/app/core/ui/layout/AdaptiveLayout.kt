package com.timemark.app.core.ui.layout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.timemark.app.core.utils.WindowSizeClass
import com.timemark.app.core.utils.windowSizeClass

/**
 * Task 37.4: 自适应双栏布局
 *
 * 根据窗口尺寸自动切换单栏/双栏：
 * - COMPACT（手机）：仅显示左栏（列表）
 * - MEDIUM/EXPANDED（平板）：左右双栏，可配置比例
 *
 * 适用于"列表 + 详情"模式，如首页打卡列表 + 今日概览。
 *
 * @param leftContent 左栏内容（列表）
 * @param rightContent 右栏内容（详情）
 * @param leftWeight 左栏权重（默认 0.4f，即 40:60）
 */
@Composable
fun TwoPaneLayout(
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leftWeight: Float = 0.4f
) {
    val sizeClass = windowSizeClass()

    when (sizeClass) {
        WindowSizeClass.COMPACT -> {
            // 手机模式：仅显示左栏（列表）
            leftContent()
        }
        else -> {
            // 平板模式：双栏布局
            Row(modifier = modifier.fillMaxSize()) {
                // 左栏：列表
                Row(
                    modifier = Modifier
                        .weight(leftWeight)
                        .fillMaxHeight()
                ) {
                    leftContent()
                }
                // 右栏：详情
                Row(
                    modifier = Modifier
                        .weight(1f - leftWeight)
                        .fillMaxHeight()
                ) {
                    rightContent()
                }
            }
        }
    }
}
