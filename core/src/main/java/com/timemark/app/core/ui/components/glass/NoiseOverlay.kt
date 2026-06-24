package com.timemark.app.core.ui.components.glass

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * 噪点纹理透明度（3-5% 极淡）
 *
 * 用于玻璃层叠加，模拟真实玻璃的细微颗粒感。
 * 过高会导致视觉杂乱，过低则失去质感。
 */
private const val NOISE_ALPHA = 0.04f

/**
 * 噪点纹理图块尺寸（像素）
 *
 * 预生成的小尺寸噪点图块，通过平铺方式覆盖整个区域，
 * 避免每帧重新生成随机点带来的性能开销。
 */
private const val NOISE_TILE_SIZE = 64

/**
 * 背景噪点纹理叠加层
 *
 * 在玻璃层上叠加一层极淡的随机噪点（3-5% 透明度），
 * 模拟真实玻璃材质的细微颗粒质感。
 *
 * 实现策略：
 * - 预生成 64x64 的噪点 ImageBitmap（仅一次）
 * - 通过 Canvas drawImage 平铺覆盖整个区域
 * - 避免每帧重新计算随机点，保证性能
 *
 * @param modifier 修饰符
 * @param alpha 噪点透明度，默认 0.04（4%）
 */
@Composable
fun NoiseOverlay(
    modifier: Modifier = Modifier,
    alpha: Float = NOISE_ALPHA
) {
    // 预生成噪点图块（仅在首次组合时生成，避免重复计算）
    val noiseTile = remember { generateNoiseTile(NOISE_TILE_SIZE) }

    Canvas(modifier = modifier) {
        val tile = noiseTile
        val tileWidth = tile.width
        val tileHeight = tile.height

        // 平铺噪点图块覆盖整个区域
        var y = 0
        while (y < size.height.toInt()) {
            var x = 0
            while (x < size.width.toInt()) {
                drawImage(
                    image = tile,
                    srcOffset = IntOffset(0, 0),
                    srcSize = IntSize(tileWidth, tileHeight),
                    dstOffset = IntOffset(x, y),
                    dstSize = IntSize(tileWidth, tileHeight),
                    alpha = alpha
                )
                x += tileWidth
            }
            y += tileHeight
        }
    }
}

/**
 * 生成噪点图块
 *
 * 创建指定尺寸的 ImageBitmap，在每个像素位置绘制随机灰度值。
 * 使用固定随机种子保证同一图块的一致性。
 *
 * @param size 图块边长（像素）
 * @return 包含随机噪点的 ImageBitmap
 */
private fun generateNoiseTile(size: Int): ImageBitmap {
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val random = Random(42) // 固定种子保证一致性

    // 为每个像素生成随机灰度值
    for (y in 0 until size) {
        for (x in 0 until size) {
            val gray = random.nextInt(256)
            // 使用纯白像素，通过 alpha 控制可见度
            val pixel = (255 shl 24) or (gray shl 16) or (gray shl 8) or gray
            bitmap.setPixel(x, y, pixel)
        }
    }
    return bitmap.asImageBitmap()
}
