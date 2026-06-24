package com.timemark.app.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.SkeletonScreen
import com.timemark.app.core.ui.layout.TwoPaneLayout
import com.timemark.app.core.utils.WindowSizeClass
import com.timemark.app.core.utils.windowSizeClass

/**
 * 首页
 *
 * 展示打卡项目列表、日期切换、今日进度概览。
 * 支持左右滑动切换日期，点击 FAB 创建新打卡项。
 *
 * 状态分发：
 * - Loading -> 骨架屏
 * - Loaded  -> HomeContent
 *
 * Task 37.4: 平板模式（EXPANDED）使用双栏布局，左侧列表 + 右侧今日概览。
 */
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sizeClass = windowSizeClass()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("create_tracker") },
                icon = { Icon(Icons.Default.Add, contentDescription = "添加") },
                text = { Text("新建打卡") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                // Task 37.1: 无障碍语义
                modifier = Modifier.semantics { contentDescription = "新建打卡项目" }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                // 显示骨架屏
                SkeletonScreen(modifier = Modifier.padding(padding))
            }
            is HomeUiState.Loaded -> {
                // Task 37.4: 平板模式双栏布局
                if (sizeClass == WindowSizeClass.EXPANDED) {
                    TwoPaneLayout(
                        leftContent = {
                            HomeContent(
                                state = state,
                                viewModel = viewModel,
                                navController = navController,
                                modifier = Modifier
                                    .padding(padding)
                                    .fillMaxSize()
                            )
                        },
                        rightContent = {
                            // 右栏：今日概览详情
                            TodayOverview(
                                state = state,
                                modifier = Modifier
                                    .padding(padding)
                                    .fillMaxSize()
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        leftWeight = 0.5f
                    )
                } else {
                    HomeContent(state, viewModel, navController, Modifier.padding(padding))
                }
            }
        }
    }
}
