# ====================================
# ai 模块消费者 ProGuard 规则
# ====================================
# 本规则会自动应用到依赖 :ai 模块的模块（如 :app）
# ====================================

# ---------- OkHttp / Retrofit ----------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class okhttp3.internal.platform.** { *; }
-keep class okio.** { *; }

# ---------- Kotlinx Serialization ----------
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class **$$serializer { *; }

# ---------- Coil ----------
-dontwarn coil.**

# ---------- AI Provider 实现类 ----------
-keep class com.timemark.app.ai.** { *; }
-keepclassmembers class com.timemark.app.ai.** { *; }
