package com.timemark.app.feature.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.core.ui.gesture.pinchToZoom
import com.timemark.app.core.utils.HapticLevel
import com.timemark.app.core.utils.rememberHapticFeedback
import com.timemark.app.domain.model.TimeViewLevel
import com.timemark.app.feature.stats.views.DayView
import com.timemark.app.feature.stats.views.HourView
import com.timemark.app.feature.stats.views.MinuteView
import com.timemark.app.feature.stats.views.MonthView
import com.timemark.app.feature.stats.views.WeekView
import com.timemark.app.feature.stats.views.YearView

/**
 * 统计页面
 *
 * 整体结构：
 * 1. 顶部栏（标题"统计" + 返回上一级按钮）
 * 2. 打卡项目选择器（横向滚动）
 * 3. 时间视图级别选择器（分/时/日/周/月/年）
 * 4. 日期导航（左右切换时间段）
 * 5. 统计概览卡片（选中项目时显示）
 * 6. 时间视图内容（支持双指缩放切换粒度、点击进入下一级、返回上一级）
 *
 * 交互：
 * - 双指外扩（scale > 1.3）：切换到更细粒度
 * - 双指内收（scale < 0.7）：切换到更粗粒度
 * - 点击图表区域：进入下一级视图
 * - 返回按钮：返回上一级视图
 *
 * 状态由 [StatsViewModel] 管理，通过 Hilt 注入。
 */
@Composable
fun StatsScreen(navController: NavController) {
    val viewModel: StatsViewModel = hiltViewModel()
    val trackers by viewModel.trackers.collectAsStateWithLifecycle()
    val selectedTrackerId by viewModel.selectedTrackerId.collectAsStateWithLifecycle()
    val viewLevel by viewModel.viewLevel.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val rangeStats by viewModel.rangeStats.collectAsStateWithLifecycle()
    val canGoBroader by viewModel.canGoBroader.collectAsStateWithLifecycle()
    val isZoomingDeeper by viewModel.isZoomingDeeper.collectAsStateWithLifecycle()

    // 触觉反馈控制器
    val haptic = rememberHapticFeedback()

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "统计",
                // 返回上一级按钮（非最粗粒度时显示）
                onBackClick = if (canGoBroader) {
                    {
                        haptic.performHaptic(HapticLevel.STRONG)
                        viewModel.returnToCoarserView()
                    }
                } else null
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 1. 打卡项目选择器（横向滚动）
            TrackerSelector(
                trackers = trackers,
                selectedTrackerId = selectedTrackerId,
                onSelect = viewModel::selectTracker
            )

            // 2. 时间视图级别选择器
            ViewLevelSelector(
                current = viewLevel,
                onSelect = viewModel::setViewLevel
            )

            // 3. 日期导航
            DateNavigator(
                selectedDate = selectedDate,
                viewLevel = viewLevel,
                onPrevious = viewModel::previousPeriod,
                onNext = viewModel::nextPeriod,
                onToday = viewModel::goToToday
            )

            // 4. 统计概览卡片（选中项目时显示）
            rangeStats?.let { stats ->
                val tracker = trackers.firstOrNull { it.id == selectedTrackerId }
                StatsOverview(stats = stats, tracker = tracker)
            }

            // 5. 时间视图内容（支持双指缩放 + 进入/返回动画）
            StatsContentWithZoom(
                modifier = Modifier.weight(1f),
                viewLevel = viewLevel,
                isZoomingDeeper = isZoomingDeeper,
                viewModel = viewModel,
                onZoom = { factor ->
                    // 在缩放回调中触发触觉反馈（仅当实际切换粒度时）
                    val beforeLevel = viewModel.viewLevel.value
                    viewModel.onZoom(factor)
                    // 如果级别发生了变化，触发 STRONG 触觉反馈
                    if (viewModel.viewLevel.value != beforeLevel) {
                        haptic.performHaptic(HapticLevel.STRONG)
                    }
                }
            )
        }
    }
}

/**
 * 统计内容区域：包裹双指缩放手势 + 进入/返回动画
 *
 * - 双指外扩（scale > 1.3）：进入更细粒度，触发 STRONG 触觉反馈
 * - 双指内收（scale < 0.7）：进入更粗粒度，触发 STRONG 触觉反馈
 * - 切换粒度时播放缩放动画（300ms，scale 1.0 → 1.1 → 1.0）
 * - 进入下一级：400ms，scale 0.8→1.0 + alpha 0→1，EaseOutBack
 * - 返回上一级：350ms，scale 1.0→0.8 + alpha 1→0，EaseInCubic
 *
 * @param modifier 修饰符（由父级 Column 传入 weight）
 * @param viewLevel 当前视图级别
 * @param isZoomingDeeper 是否为进入更细粒度方向
 * @param viewModel 统计 ViewModel
 * @param onZoom 缩放回调
 */
@Composable
private fun StatsContentWithZoom(
    modifier: Modifier = Modifier,
    viewLevel: TimeViewLevel,
    isZoomingDeeper: Boolean,
    viewModel: StatsViewModel,
    onZoom: (Float) -> Unit
) {
    // 缩放切换时的脉冲动画（300ms，scale 1.0 → 1.1 → 1.0）
    // 每次 viewLevel 变化时触发
    val zoomPulse = remember { Animatable(1.0f) }
    LaunchedEffect(viewLevel) {
        zoomPulse.snapTo(1.0f)
        zoomPulse.animateTo(1.1f, tween(durationMillis = 150))
        zoomPulse.animateTo(1.0f, tween(durationMillis = 150))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            // 双指缩放手势：切换视图粒度
            .pinchToZoom(
                onZoom = onZoom,
                onZoomEnd = { /* 手势结束，ViewModel 已重置 zoomLevel */ }
            )
    ) {
        // 使用 AnimatedContent 实现进入/返回动画
        AnimatedContent(
            targetState = viewLevel,
            transitionSpec = {
                if (isZoomingDeeper) {
                    // 进入下一级：400ms，scale 0.8→1.0 + alpha 0→1，EaseOutBack
                    (fadeIn(tween(400, easing = EaseOutBack)) +
                        scaleIn(tween(400, easing = EaseOutBack), initialScale = 0.8f))
                        .togetherWith(
                            fadeOut(tween(400)) +
                                scaleOut(tween(400), targetScale = 1.0f)
                        )
                } else {
                    // 返回上一级：350ms，scale 1.0→0.8 + alpha 1→0，EaseInCubic
                    (fadeIn(tween(350)) +
                        scaleIn(tween(350), initialScale = 1.0f))
                        .togetherWith(
                            fadeOut(tween(350, easing = EaseInCubic)) +
                                scaleOut(tween(350, easing = EaseInCubic), targetScale = 0.8f)
                        )
                }
            },
            label = "viewLevelTransition"
        ) { level ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = zoomPulse.value
                        scaleY = zoomPulse.value
                    }
            ) {
                when (level) {
                    TimeViewLevel.MINUTE -> MinuteView(viewModel)
                    TimeViewLevel.HOUR -> HourView(viewModel)
                    TimeViewLevel.DAY -> DayView(viewModel)
                    TimeViewLevel.WEEK -> WeekView(viewModel)
                    TimeViewLevel.MONTH -> MonthView(viewModel)
                    TimeViewLevel.YEAR -> YearView(viewModel)
                }
            }
        }
    }
}
