package com.timemark.app.core.ui.components.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timemark.app.core.ui.theme.DarkSurface
import com.timemark.app.core.ui.theme.LightSurface
import com.timemark.app.core.ui.theme.Primary
import com.timemark.app.core.ui.theme.TimeMarkTheme

/**
 * 液态玻璃底部导航栏
 *
 * 厚重玻璃质感，选中项有液态高亮效果（缩放放大 + 颜色变化）。
 * 适用于应用主页面底部导航。
 *
 * @param items 导航项列表，使用 [BottomNavItem]
 * @param currentRoute 当前选中路由，可为 null（无选中项）
 * @param onItemClick 点击导航项回调，参数为被点击项的 route
 * @param modifier 修饰符
 */
@Composable
fun GlassBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColors = if (isDark) {
        listOf(Color(0xE61E293B), Color(0xB31E293B))  // 90% -> 70%
    } else {
        listOf(Color(0xE6FFFFFF), Color(0xB3FFFFFF))
    }
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .blur(30.dp)
            .background(
                brush = Brush.verticalGradient(backgroundColors)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                val isSelected = item.route == currentRoute
                GlassNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemClick(item.route) }
                )
            }
        }
    }
}

/**
 * 单个导航项
 *
 * 选中时有液态高亮效果：图标放大、文字显示、颜色变为主色。
 * 若 [BottomNavItem.selectedIcon] 不为空，选中时切换为该图标。
 */
@Composable
private fun GlassNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 选中时图标放大动画
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "iconScale"
    )

    val iconColor = if (isSelected) {
        Primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            // 无障碍：设置 Tab 角色、选中状态与内容描述
            .semantics {
                role = Role.Tab
                this.contentDescription = item.label
                this.selected = isSelected
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // 选中项背景高亮
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .background(
                            color = Primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            Icon(
                imageVector = if (isSelected) (item.selectedIcon ?: item.icon) else item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier
                    .scale(iconScale)
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 选中时显示文字
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = iconColor
            )
        }
    }
}

@Preview(name = "GlassBottomBar", showBackground = true)
@Composable
private fun GlassBottomBarPreview() {
    val items = listOf(
        BottomNavItem("home", androidx.compose.material.icons.filled.Home, "首页"),
        BottomNavItem("stats", androidx.compose.material.icons.filled.BarChart, "统计"),
        BottomNavItem("ai", androidx.compose.material.icons.filled.AutoAwesome, "AI"),
        BottomNavItem("settings", androidx.compose.material.icons.filled.Person, "我的")
    )
    TimeMarkTheme(darkTheme = false) {
        Surface(color = LightSurface) {
            GlassBottomBar(
                items = items,
                currentRoute = "home",
                onItemClick = {}
            )
        }
    }
}

@Preview(name = "GlassBottomBar - Dark", showBackground = true)
@Composable
private fun GlassBottomBarDarkPreview() {
    val items = listOf(
        BottomNavItem("home", androidx.compose.material.icons.filled.Home, "首页"),
        BottomNavItem("stats", androidx.compose.material.icons.filled.BarChart, "统计"),
        BottomNavItem("ai", androidx.compose.material.icons.filled.AutoAwesome, "AI"),
        BottomNavItem("settings", androidx.compose.material.icons.filled.Person, "我的")
    )
    TimeMarkTheme(darkTheme = true) {
        Surface(color = DarkSurface) {
            GlassBottomBar(
                items = items,
                currentRoute = "stats",
                onItemClick = {}
            )
        }
    }
}
