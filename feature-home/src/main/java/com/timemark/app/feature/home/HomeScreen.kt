package com.timemark.app.feature.home

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timemark.app.core.ui.components.SkeletonScreen

/**
 * 首页
 *
 * 展示打卡项目列表、日期切换、今日进度概览。
 * 支持左右滑动切换日期，点击 FAB 创建新打卡项。
 *
 * 状态分发：
 * - Loading -> 骨架屏
 * - Loaded  -> HomeContent
 */
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("create_tracker") },
                icon = { Icon(Icons.Default.Add, contentDescription = "添加") },
                text = { Text("新建打卡") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                // 显示骨架屏
                SkeletonScreen(modifier = Modifier.padding(padding))
            }
            is HomeUiState.Loaded -> {
                HomeContent(state, viewModel, navController, Modifier.padding(padding))
            }
        }
    }
}
