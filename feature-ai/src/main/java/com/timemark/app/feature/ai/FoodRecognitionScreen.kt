package com.timemark.app.feature.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.timemark.app.core.ui.components.glass.GlassButton
import com.timemark.app.core.ui.components.glass.GlassButtonType
import com.timemark.app.core.ui.components.glass.GlassCard
import com.timemark.app.core.ui.components.glass.GlassLevel
import com.timemark.app.core.ui.components.glass.GlassTopBar
import com.timemark.app.domain.model.FoodItem
import com.timemark.app.feature.ai.food.FoodRecognitionViewModel

/**
 * 食物识别页面
 *
 * 功能：
 * - 拍照/相册选择
 * - 图片预览
 * - 识别按钮
 * - 结果展示（食物列表）
 * - 用户确认保存
 */
@Composable
fun FoodRecognitionScreen(navController: NavController) {
    val viewModel: FoodRecognitionViewModel = hiltViewModel()
    val imagePath by viewModel.imagePath.collectAsStateWithLifecycle()
    val recognitionState by viewModel.recognitionState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 相册选择 launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = getRealPathFromUri(context, it)
            if (path != null) {
                viewModel.selectImage(path)
            }
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "食物识别",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 图片预览或选择按钮
            if (imagePath == null) {
                GlassCard(
                    level = GlassLevel.STANDARD,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "选择食物图片",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "拍照或从相册选择食物图片，AI 将自动识别",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassButton(
                                text = "相册",
                                onClick = { galleryLauncher.launch("image/*") },
                                icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) }
                            )
                        }
                    }
                }
            } else {
                // 图片预览
                GlassCard(
                    level = GlassLevel.STANDARD,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        AsyncImage(
                            model = imagePath,
                            contentDescription = "食物图片",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // 清除按钮
                        androidx.compose.material3.IconButton(
                            onClick = { viewModel.clearImage() },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // 识别按钮
                if (recognitionState is FoodRecognitionViewModel.RecognitionState.Idle) {
                    GlassButton(
                        text = "开始识别",
                        onClick = { viewModel.recognize() },
                        modifier = Modifier.fillMaxWidth(),
                        icon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) }
                    )
                }

                // 识别状态
                when (val state = recognitionState) {
                    is FoodRecognitionViewModel.RecognitionState.Loading -> {
                        GlassCard(
                            level = GlassLevel.LIGHT,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("识别中...")
                            }
                        }
                    }
                    is FoodRecognitionViewModel.RecognitionState.Success -> {
                        RecognitionResultCard(state.result) {
                            // 保存确认
                            navController.popBackStack()
                        }
                    }
                    is FoodRecognitionViewModel.RecognitionState.Error -> {
                        GlassCard(
                            level = GlassLevel.LIGHT,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "⚠️ 识别失败",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                GlassButton(
                                    text = "重试",
                                    onClick = { viewModel.recognize() },
                                    type = GlassButtonType.SECONDARY
                                )
                            }
                        }
                    }
                    else -> { /* Idle 已处理 */ }
                }
            }
        }
    }
}

/** 识别结果卡片 */
@Composable
private fun RecognitionResultCard(
    result: com.timemark.app.domain.model.FoodRecognitionResult,
    onConfirm: () -> Unit
) {
    GlassCard(
        level = GlassLevel.STANDARD,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "识别结果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // 总热量
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "总热量",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${result.totalCalories.toInt()} 大卡",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 餐次
            result.mealType?.let { meal ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "餐次",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = meal,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 食物列表
            Text(
                text = "食物清单（${result.items.size}）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            result.items.forEach { item ->
                FoodItemCard(item)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 确认保存按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassButton(
                    text = "重新识别",
                    onClick = { /* 重新识别由外部处理 */ },
                    type = GlassButtonType.SECONDARY,
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "确认保存",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.Check, contentDescription = null) }
                )
            }
        }
    }
}

/** 单个食物项卡片 */
@Composable
private fun FoodItemCard(item: FoodItem) {
    GlassCard(
        level = GlassLevel.LIGHT,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.calories.toInt()} 大卡",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "份量: ${item.portion} (${item.portionGrams.toInt()}g)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "蛋白质 ${item.protein.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "碳水 ${item.carbs.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "脂肪 ${item.fat.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.confidence > 0) {
                Text(
                    text = "置信度: ${(item.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 从 Uri 获取真实文件路径
 *
 * 简化实现：优先使用 contentResolver 查询 DATA 列，
 * 失败时复制到缓存文件并返回路径。
 */
private fun getRealPathFromUri(context: android.content.Context, uri: Uri): String? {
    return runCatching {
        // 尝试从 MediaStore 查询
        val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } ?: copyUriToCache(context, uri)
    }.getOrNull() ?: copyUriToCache(context, uri)
}

/** 将 Uri 内容复制到缓存文件，返回文件路径 */
private fun copyUriToCache(context: android.content.Context, uri: Uri): String {
    val cacheFile = java.io.File(context.cacheDir, "food_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        cacheFile.outputStream().use { output -> input.copyTo(output) }
    }
    return cacheFile.absolutePath
}
