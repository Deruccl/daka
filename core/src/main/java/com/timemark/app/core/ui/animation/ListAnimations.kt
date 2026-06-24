package com.timemark.app.core.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * 列表动画集合
 *
 * 提供列表项的瀑布式加载、添加、删除动画，以及液态水滴下拉刷新组件。
 *
 * 动画时长遵循设计规范：
 * - 瀑布式加载：每项延迟 50ms * index，淡入 + 上移 20dp，400ms
 * - 列表项添加：从顶部滑入 + 缩放，300ms
 * - 列表项删除：向左滑出 + 淡出 + 高度收缩，300ms
 */

/** 单项瀑布动画的基础时长 */
private const val STAGGER_DURATION = 400

/** 每项延迟 */
private const val STAGGER_DELAY_PER_ITEM = 50

/** 上移距离（dp） */
private const val STAGGER_OFFSET_DP = 20f

/** 列表项添加/删除动画时长 */
private const val ITEM_MUTATION_DURATION = 300

/**
 * 瀑布式加载动画
 *
 * 每项延迟 50ms * index，淡入 + 上移 20dp，400ms。
 * 适用于 LazyColumn/LazyRow 的 item 首次加载。
 *
 * @param index 列表项索引（从 0 开始）
 */
fun Modifier.staggeredEntrance(index: Int): Modifier = composed {
    // 计算该项的延迟时间（最多 600ms 避免长列表末尾等待过久）
    val delayMillis = (index * STAGGER_DELAY_PER_ITEM).coerceAtMost(600)

    // 上移距离转换为 px
    val density = LocalDensity.current
    val offsetYPx = with(density) { STAGGER_OFFSET_DP.dp.toPx() }

    // 通过 LaunchedEffect 在首次进入时触发可见状态
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    // 淡入动画
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = STAGGER_DURATION,
            delayMillis = delayMillis
        ),
        label = "staggerAlpha"
    )

    // 缩放动画（从 0.92f 到 1f）
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        animationSpec = tween(
            durationMillis = STAGGER_DURATION,
            delayMillis = delayMillis,
            easing = EaseOutCubic
        ),
        label = "staggerScale"
    )

    // 上移动画（从 20dp 下方移到原位，使用 translationY 不影响布局）
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else offsetYPx,
        animationSpec = tween(
            durationMillis = STAGGER_DURATION,
            delayMillis = delayMillis,
            easing = EaseOutCubic
        ),
        label = "staggerTranslationY"
    )

    this
        .alpha(alpha)
        .scale(scale)
        .graphicsLayer { this.translationY = translationY }
}

/**
 * 列表项添加动画
 *
 * 从顶部滑入 + 缩放，300ms。
 * 配合 AnimatedVisibility 使用。
 *
 * @param visible 是否可见
 * @param content 列表项内容
 */
@Composable
fun AnimatedItemInsertion(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = tween(
                durationMillis = ITEM_MUTATION_DURATION,
                easing = EaseOutCubic
            )
        ) + fadeIn(
            animationSpec = tween(durationMillis = ITEM_MUTATION_DURATION)
        ) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(
                durationMillis = ITEM_MUTATION_DURATION,
                easing = EaseOutCubic
            )
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = ITEM_MUTATION_DURATION))
    ) {
        content()
    }
}

/**
 * 列表项删除动画
 *
 * 向左滑出 + 淡出 + 高度收缩，300ms。
 * 配合 AnimatedVisibility 使用。
 *
 * @param visible 是否可见
 * @param content 列表项内容
 */
@Composable
fun AnimatedItemRemoval(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = ITEM_MUTATION_DURATION)),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(
                durationMillis = ITEM_MUTATION_DURATION,
                easing = EaseInCubic
            )
        ) + fadeOut(
            animationSpec = tween(durationMillis = ITEM_MUTATION_DURATION)
        ) + shrinkVertically(
            animationSpec = tween(durationMillis = ITEM_MUTATION_DURATION)
        )
    ) {
        content()
    }
}

/**
 * 液态水滴下拉刷新组件
 *
 * 水滴形状随下拉距离变形（拉长），释放后回弹。
 * 刷新中时水滴旋转。
 *
 * @param isRefreshing 是否正在刷新
 * @param pullProgress 下拉进度 0..1（由调用方根据拖拽距离计算）
 * @param modifier 修饰符
 * @param content 刷新内容
 */
@Composable
fun LiquidPullToRefresh(
    isRefreshing: Boolean,
    pullProgress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        // 刷新指示器（仅在下拉或刷新中显示）
        if (pullProgress > 0f || isRefreshing) {
            LiquidDropIndicator(
                isRefreshing = isRefreshing,
                pullProgress = pullProgress,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }
}

/**
 * 液态水滴刷新指示器
 *
 * 水滴形状：上窄下宽，随下拉进度拉长。
 * 刷新中时围绕中心旋转。
 */
@Composable
private fun LiquidDropIndicator(
    isRefreshing: Boolean,
    pullProgress: Float,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary

    // 刷新中时持续旋转
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "dropRotation"
    )

    // 水滴尺寸随下拉进度变化
    val baseSize = 32.dp
    val dropSize = baseSize * (0.6f + 0.4f * pullProgress.coerceIn(0f, 1f))

    // 透明度随下拉进度变化
    val indicatorAlpha = pullProgress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .size(dropSize)
            .alpha(if (isRefreshing) 1f else indicatorAlpha)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val w = size.width
            val h = size.height

            // 刷新中时旋转
            val angle = if (isRefreshing) rotation else 0f

            rotate(degrees = angle, pivot = Offset(centerX, centerY)) {
                // 绘制水滴形状（上尖下圆）
                val dropPath = Path().apply {
                    val dropWidth = w * 0.7f
                    val dropHeight = h
                    moveTo(centerX, centerY - dropHeight / 2f)
                    cubicTo(
                        centerX + dropWidth / 2f, centerY - dropHeight / 4f,
                        centerX + dropWidth / 2f, centerY + dropHeight / 2f,
                        centerX, centerY + dropHeight / 2f
                    )
                    cubicTo(
                        centerX - dropWidth / 2f, centerY + dropHeight / 2f,
                        centerX - dropWidth / 2f, centerY - dropHeight / 4f,
                        centerX, centerY - dropHeight / 2f
                    )
                    close()
                }
                drawPath(
                    path = dropPath,
                    color = color
                )
            }
        }
    }
}
