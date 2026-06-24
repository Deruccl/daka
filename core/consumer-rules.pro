# ====================================
# core 模块消费者 ProGuard 规则
# ====================================
# 本规则会自动应用到依赖 :core 模块的模块（如 :app 和各 feature 模块）
# ====================================

# ---------- Compose ----------
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep @androidx.compose.runtime.Composable class *
-keep @androidx.compose.runtime.Stable class *
-keep @androidx.compose.runtime.Immutable class *
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ---------- Coroutines ----------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }
