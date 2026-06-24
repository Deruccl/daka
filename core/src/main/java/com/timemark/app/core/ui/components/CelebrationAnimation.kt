package com.timemark.app.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.Primary
import com.timemark.app.core.ui.theme.Secondary
import com.timemark.app.core.ui.theme.Success
import com.timemark.app.core.ui.theme.Tertiary
import com.timemark.app.core.ui.theme.TimeMarkTheme
import com.timemark.app.core.ui.theme.Warning
import kotlinx.coroutines.delay

/**
 * 庆祝动画类型
 *
 * - CHECK_IN：完成打卡，闪光 + 数字跳动
 * - GOAL：达成目标，彩色粒子爆发
 * - MILESTONE：里程碑，全屏庆祝
 */
enum class CelebrationType {
    CHECK_IN,   // 完成打卡
    GOAL,       // 达成目标
    MILESTONE   // 里程碑
}

/**
 * 庆祝动画组件
 *
 * 根据类型播放不同强度的庆祝动画：
 * - CHECK_IN：中心闪光 + 缩放跳动，持续 1.5s
 * - GOAL：彩色粒子向外扩散，持续 2s
 * - MILESTONE：全屏粒子爆发 + 文字弹出，持续 3s
 *
 * @param visible 是否显示
 * @param type 庆祝类型
 * @param text 显示文字（可选）
 * @param modifier 修饰符
 * @param onFinished 动画结束回调
 */
@Composable
fun CelebrationAnimation(
    visible: Boolean,
    type: CelebrationType,
    modifier: Modifier = Modifier,
    text: String? = null,
    onFinished: () -> Unit = {}
) {
    val duration = when (type) {
        CelebrationType.CHECK_IN -> 1500L
        CelebrationType.GOAL -> 2000L
        CelebrationType.MILESTONE -> 3000L
    }

    // 动画结束回调
    LaunchedEffect(visible) {
        if (visible) {
            delay(duration)
            onFinished()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        when (type) {
            CelebrationType.CHECK_IN -> CheckInCelebration(text)
            CelebrationType.GOAL -> GoalCelebration(text)
            CelebrationType.MILESTONE -> MilestoneCelebration(text)
        }
    }
}

/**
 * 完成打卡庆祝：闪光 + 数字跳动
 */
@Composable
private fun CheckInCelebration(text: String?) {
    val infiniteTransition = rememberInfiniteTransition()

    // 闪光缩放
    val flashScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flashScale"
    )

    // 闪光透明度
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flashAlpha"
    )

    // 数字跳动
    var bounce by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { bounce = true }
    val textScale by animateFloatAsState(
        targetValue = if (bounce) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "textBounce"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) * flashScale

            // 闪光圆环
            drawCircle(
                color = Success.copy(alpha = flashAlpha * 0.3f),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Success.copy(alpha = flashAlpha * 0.6f),
                radius = radius * 0.6f,
                center = center
            )
        }

        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Success,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .scale(textScale)
            )
        }
    }
}

/**
 * 达成目标庆祝：彩色粒子爆发
 */
@Composable
private fun GoalCelebration(text: String?) {
    val infiniteTransition = rememberInfiniteTransition()

    // 粒子扩散
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle"
    )

    val particleColors = listOf(Primary, Secondary, Tertiary, Success, Warning)
    val particleCount = 12

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            for (i in 0 until particleCount) {
                val angle = (360f / particleCount) * i
                val radians = Math.toRadians(angle.toDouble()).toFloat()
                val distance = maxRadius * particleProgress
                val particleOffset = Offset(
                    x = center.x + distance * kotlin.math.cos(radians),
                    y = center.y + distance * kotlin.math.sin(radians)
                )
                val particleSize = (8.dp.toPx()) * (1f - particleProgress * 0.5f)
                val particleAlpha = 1f - particleProgress

                drawCircle(
                    color = particleColors[i % particleColors.size].copy(alpha = particleAlpha),
                    radius = particleSize,
                    center = particleOffset
                )
            }

            // 中心光晕
            drawCircle(
                color = Success.copy(alpha = (1f - particleProgress) * 0.4f),
                radius = maxRadius * 0.2f * (1f - particleProgress * 0.5f),
                center = center
            )
        }

        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * 里程碑庆祝：全屏粒子爆发 + 文字弹出
 */
@Composable
private fun MilestoneCelebration(text: String?) {
    val infiniteTransition = rememberInfiniteTransition()

    // 粒子旋转
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // 粒子扩散
    val expansion by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "expansion"
    )

    val particleColors = listOf(Primary, Secondary, Tertiary, Success, Warning)
    val particleCount = 24

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            rotate(degrees = rotation, pivot = center) {
                for (i in 0 until particleCount) {
                    val angle = (360f / particleCount) * i
                    val radians = Math.toRadians(angle.toDouble()).toFloat()
                    val distance = maxRadius * expansion
                    val particleOffset = Offset(
                        x = center.x + distance * kotlin.math.cos(radians),
                        y = center.y + distance * kotlin.math.sin(radians)
                    )
                    val particleSize = (10.dp.toPx()) * (1f - expansion * 0.3f)
                    val particleAlpha = 1f - expansion * 0.7f

                    drawCircle(
                        color = particleColors[i % particleColors.size].copy(alpha = particleAlpha),
                        radius = particleSize,
                        center = particleOffset
                    )
                }
            }

            // 中心多层光晕
            for (layer in 0 until 3) {
                drawCircle(
                    color = Success.copy(alpha = (1f - expansion * 0.5f) * (0.3f - layer * 0.08f)),
                    radius = maxRadius * (0.15f + layer * 0.05f) * (1f + expansion * 0.3f),
                    center = center
                )
            }
        }

        if (text != null) {
            var showText by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(200)
                showText = true
            }
            val textScale by animateFloatAsState(
                targetValue = if (showText) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "milestoneText"
            )

            Text(
                text = text,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(32.dp)
                    .scale(textScale)
            )
        }
    }
}

@Preview(name = "Celebration - CheckIn", showBackground = true)
@Composable
private fun CelebrationCheckInPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            CelebrationAnimation(
                visible = true,
                type = CelebrationType.CHECK_IN,
                text = "+1"
            )
        }
    }
}

@Preview(name = "Celebration - Goal", showBackground = true)
@Composable
private fun CelebrationGoalPreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            CelebrationAnimation(
                visible = true,
                type = CelebrationType.GOAL,
                text = "目标达成!"
            )
        }
    }
}

@Preview(name = "Celebration - Milestone", showBackground = true)
@Composable
private fun CelebrationMilestonePreview() {
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            CelebrationAnimation(
                visible = true,
                type = CelebrationType.MILESTONE,
                text = "7天连续打卡!"
            )
        }
    }
}
