package com.timemark.app.core.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

/**
 * 页面切换动画集合
 *
 * 统一管理应用内所有页面切换动画，按场景分为：
 * - 底部导航切换（淡入淡出 + 轻微位移）
 * - 详情页进入/退出（左右滑入滑出）
 * - 全屏弹窗进入/退出（底部滑入滑出 + 缩放）
 *
 * 动画时长遵循设计规范：
 * - 底部导航 250ms
 * - 详情进入 350ms（EaseOutCubic）
 * - 详情退出 300ms（EaseInCubic）
 * - 弹窗进入 300ms（EaseOutBack）
 * - 弹窗退出 250ms（EaseInCubic）
 */

/** 底部导航切换动画时长 */
private const val BOTTOM_NAV_DURATION = 250

/** 详情页进入动画时长 */
private const val DETAIL_ENTER_DURATION = 350

/** 详情页退出动画时长 */
private const val DETAIL_EXIT_DURATION = 300

/** 弹窗进入动画时长 */
private const val DIALOG_ENTER_DURATION = 300

/** 弹窗退出动画时长 */
private const val DIALOG_EXIT_DURATION = 250

/** 位移距离（px），用于轻微位移效果 */
private const val SLIDE_OFFSET = 60

/**
 * 底部导航切换进入动画
 *
 * 淡入 + 轻微向上位移，250ms。
 * 适用于主 Tab 之间的切换。
 */
fun bottomNavEnterTransition(): EnterTransition {
    return fadeIn(
        animationSpec = tween(durationMillis = BOTTOM_NAV_DURATION)
    ) + slideInVertically(
        initialOffsetY = { SLIDE_OFFSET / 2 },
        animationSpec = tween(durationMillis = BOTTOM_NAV_DURATION)
    )
}

/**
 * 底部导航切换退出动画
 *
 * 淡出 + 轻微向下位移，250ms。
 */
fun bottomNavExitTransition(): ExitTransition {
    return fadeOut(
        animationSpec = tween(durationMillis = BOTTOM_NAV_DURATION)
    ) + slideOutVertically(
        targetOffsetY = { SLIDE_OFFSET / 2 },
        animationSpec = tween(durationMillis = BOTTOM_NAV_DURATION)
    )
}

/**
 * 详情页进入动画
 *
 * 从右侧滑入 + 淡入，350ms，EaseOutCubic 缓动。
 * 适用于 TrackerDetail、EditTracker 等详情页。
 */
fun detailEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(
            durationMillis = DETAIL_ENTER_DURATION,
            easing = EaseOutCubic
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = DETAIL_ENTER_DURATION,
            easing = EaseOutCubic
        )
    )
}

/**
 * 详情页退出动画
 *
 * 向右滑出 + 淡出，300ms，EaseInCubic 缓动。
 */
fun detailExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(
            durationMillis = DETAIL_EXIT_DURATION,
            easing = EaseInCubic
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = DETAIL_EXIT_DURATION,
            easing = EaseInCubic
        )
    )
}

/**
 * 详情页弹出进入动画（返回时）
 *
 * 从左侧滑入 + 淡入，350ms，EaseOutCubic。
 */
fun detailPopEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(
            durationMillis = DETAIL_ENTER_DURATION,
            easing = EaseOutCubic
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = DETAIL_ENTER_DURATION,
            easing = EaseOutCubic
        )
    )
}

/**
 * 详情页弹出退出动画（返回时）
 *
 * 向左滑出 + 淡出，300ms，EaseInCubic。
 */
fun detailPopExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(
            durationMillis = DETAIL_EXIT_DURATION,
            easing = EaseInCubic
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = DETAIL_EXIT_DURATION,
            easing = EaseInCubic
        )
    )
}

/**
 * 全屏弹窗进入动画
 *
 * 从底部滑入 + 缩放，300ms，EaseOutBack 缓动（带过冲效果）。
 * 适用于 AIChat、FoodRecognition 等全屏弹窗式页面。
 */
fun dialogEnterTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(
            durationMillis = DIALOG_ENTER_DURATION,
            easing = EaseOutBack
        )
    ) + scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(
            durationMillis = DIALOG_ENTER_DURATION,
            easing = EaseOutBack
        )
    ) + fadeIn(
        animationSpec = tween(durationMillis = DIALOG_ENTER_DURATION)
    )
}

/**
 * 弹窗退出动画
 *
 * 向下滑出 + 缩放，250ms，EaseInCubic 缓动。
 */
fun dialogExitTransition(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(
            durationMillis = DIALOG_EXIT_DURATION,
            easing = EaseInCubic
        )
    ) + scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(
            durationMillis = DIALOG_EXIT_DURATION,
            easing = EaseInCubic
        )
    ) + fadeOut(
        animationSpec = tween(durationMillis = DIALOG_EXIT_DURATION)
    )
}

/**
 * 弹窗弹出进入动画（返回时）
 *
 * 从顶部滑入 + 缩放，300ms，EaseOutBack。
 */
fun dialogPopEnterTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { -it / 3 },
        animationSpec = tween(
            durationMillis = DIALOG_ENTER_DURATION,
            easing = EaseOutBack
        )
    ) + scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(
            durationMillis = DIALOG_ENTER_DURATION,
            easing = EaseOutBack
        )
    ) + fadeIn(
        animationSpec = tween(durationMillis = DIALOG_ENTER_DURATION)
    )
}

/**
 * 弹窗弹出退出动画（返回时）
 *
 * 向上滑出 + 缩放，250ms，EaseInCubic。
 */
fun dialogPopExitTransition(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { -it / 3 },
        animationSpec = tween(
            durationMillis = DIALOG_EXIT_DURATION,
            easing = EaseInCubic
        )
    ) + scaleOut(
        targetScale = 0.95f,
        animationSpec = tween(
            durationMillis = DIALOG_EXIT_DURATION,
            easing = EaseInCubic
        )
    ) + fadeOut(
        animationSpec = tween(durationMillis = DIALOG_EXIT_DURATION)
    )
}

/**
 * 路由类型枚举
 *
 * 用于根据目标路由选择对应的切换动画。
 */
enum class RouteTransitionType {
    /** 底部导航主 Tab */
    BOTTOM_NAV,

    /** 详情页（从右侧滑入） */
    DETAIL,

    /** 全屏弹窗（从底部滑入） */
    DIALOG
}

/**
 * 根据路由名称判断页面类型
 *
 * @param route 路由路径
 * @return 对应的动画类型
 */
fun classifyRoute(route: String?): RouteTransitionType {
    if (route == null) return RouteTransitionType.BOTTOM_NAV
    return when (route) {
        // 主 Tab 页面
        "home", "stats", "ai", "settings" -> RouteTransitionType.BOTTOM_NAV

        // 全屏弹窗式页面
        "ai_chat", "food_recognition" -> RouteTransitionType.DIALOG

        // 详情页
        else -> RouteTransitionType.DETAIL
    }
}

/**
 * 根据路由类型生成进入动画
 *
 * 在 NavHost 的 enterTransition lambda 中使用。
 * 通过 [AnimatedContentTransitionScope.targetState] 获取目标路由。
 */
fun enterTransitionForRoute(
    scope: AnimatedContentTransitionScope<*>
): EnterTransition {
    val targetRoute = scope.targetState.destination.route
    return when (classifyRoute(targetRoute)) {
        RouteTransitionType.BOTTOM_NAV -> bottomNavEnterTransition()
        RouteTransitionType.DETAIL -> detailEnterTransition()
        RouteTransitionType.DIALOG -> dialogEnterTransition()
    }
}

/**
 * 根据路由类型生成退出动画
 */
fun exitTransitionForRoute(
    scope: AnimatedContentTransitionScope<*>
): ExitTransition {
    val targetRoute = scope.targetState.destination.route
    return when (classifyRoute(targetRoute)) {
        RouteTransitionType.BOTTOM_NAV -> bottomNavExitTransition()
        RouteTransitionType.DETAIL -> detailExitTransition()
        RouteTransitionType.DIALOG -> dialogExitTransition()
    }
}

/**
 * 根据路由类型生成弹出进入动画
 */
fun popEnterTransitionForRoute(
    scope: AnimatedContentTransitionScope<*>
): EnterTransition {
    val initialRoute = scope.initialState.destination.route
    return when (classifyRoute(initialRoute)) {
        RouteTransitionType.BOTTOM_NAV -> bottomNavEnterTransition()
        RouteTransitionType.DETAIL -> detailPopEnterTransition()
        RouteTransitionType.DIALOG -> dialogPopEnterTransition()
    }
}

/**
 * 根据路由类型生成弹出退出动画
 */
fun popExitTransitionForRoute(
    scope: AnimatedContentTransitionScope<*>
): ExitTransition {
    val initialRoute = scope.initialState.destination.route
    return when (classifyRoute(initialRoute)) {
        RouteTransitionType.BOTTOM_NAV -> bottomNavExitTransition()
        RouteTransitionType.DETAIL -> detailPopExitTransition()
        RouteTransitionType.DIALOG -> dialogPopExitTransition()
    }
}
