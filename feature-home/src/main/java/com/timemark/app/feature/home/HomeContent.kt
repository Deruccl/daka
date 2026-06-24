package com.timemark.app.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.timemark.app.core.ui.animation.parallaxScroll
import com.timemark.app.core.ui.animation.staggeredEntrance
import com.timemark.app.core.ui.components.EmptyState

/**
 * 首页内容区
 *
 * 包含顶部区域、今日概览与打卡卡片列表。
 * 支持左右滑动切换日期（阈值 80dp）。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    state: HomeUiState.Loaded,
    viewModel: HomeViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // 滑动切换日期的阈值
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 80.dp.toPx() }
    var dragAmount by remember { mutableFloatStateOf(0f) }

    // 滚动状态（用于视差效果）
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAmount = 0f },
                    onDragEnd = {
                        when {
                            dragAmount > swipeThreshold -> viewModel.previousDay()
                            dragAmount < -swipeThreshold -> viewModel.nextDay()
                        }
                        dragAmount = 0f
                    },
                    onDragCancel = { dragAmount = 0f },
                    onHorizontalDrag = { _, delta -> dragAmount += delta }
                )
            },
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部区域：日期 + 问候语 + 进度环
        // 应用视差滚动（ratio=0.5f，背景元素滚动速度为内容的一半）
        item {
            HomeHeader(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.parallaxScroll(ratio = 0.5f, scrollState = listState)
            )
        }

        // 今日概览（可展开）
        item { TodayOverview(state) }

        // 打卡卡片列表 / 空状态
        if (state.trackers.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Inbox,
                    title = "还没有打卡项目",
                    description = "点击右下角按钮创建第一个打卡项目，开始养成好习惯",
                    actionText = "新建打卡",
                    onActionClick = { navController.navigate("create_tracker") }
                )
            }
        } else {
            itemsIndexed(
                state.trackers,
                key = { _, item -> item.tracker.id }
            ) { index, trackerWithStats ->
                TrackerCard(
                    trackerWithStats = trackerWithStats,
                    onClick = {
                        navController.navigate("tracker_detail/${trackerWithStats.tracker.id}")
                    },
                    onQuickCheckIn = { viewModel.quickCheckIn(trackerWithStats.tracker) },
                    onLongClick = {
                        navController.navigate("edit_tracker/${trackerWithStats.tracker.id}")
                    },
                    // 应用瀑布式加载动画（每项延迟 50ms * index）
                    modifier = Modifier
                        .staggeredEntrance(index)
                        .animateItemPlacement()
                )
            }
        }
    }
}
