# ====================================
# 时光印记 TimeMark - ProGuard / R8 规则
# ====================================
# 本文件配置 Release 构建时的代码混淆、压缩与保留规则。
# 目标：在显著减小 APK 体积的同时，保证运行时不出错。
# ====================================

# ---------- 通用属性保留 ----------
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes Deprecated

# ---------- Kotlin ----------
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
# 保留 Kotlin 协程内部主调度器
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}

# ---------- Kotlinx Serialization ----------
# 保留 @Serializable 类的序列化器
-keep,includedescriptorclasses class com.timemark.app.**$$serializer { *; }
-keepclassmembers class com.timemark.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.timemark.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
# 保留 JsonSerializer 与 KSerializer 接口实现
-keep,allowobfuscation,allowshrinking interface kotlinx.serialization.KSerializer
-keep,allowobfuscation,allowshrinking class * implements kotlinx.serialization.KSerializer
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.json.** { *; }
# 保留 @SerialName 注解查询能力
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

# ---------- Hilt / Dagger 依赖注入 ----------
-keep class dagger.hilt.** { *; }
-keep class dagger.internal.** { *; }
-keep class javax.inject.** { *; }
# 保留 @HiltAndroidApp Application 类
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep @dagger.hilt.android.HiltAndroidApp class *
# 保留 @HiltViewModel
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *
-keep,allowobfuscation,allowshrinking interface * { @dagger.hilt.android.lifecycle.HiltViewModel *; }
# 保留 @AndroidEntryPoint
-keep @dagger.hilt.android.AndroidEntryPoint class *
# 保留 @Inject 构造函数与字段
-keepclassmembers class * { @javax.inject.Inject *; }
# 保留 @Module 与 @InstallIn
-keep @dagger.Module class *
-keep @dagger.hilt.InstallIn class *
-keepclassmembers class * {
    @dagger.Provides *;
    @dagger.Binds *;
}
# 保留 Hilt 生成的类
-keep class **_HiltModules { *; }
-keep class **_HiltComponents$* { *; }
-keep class **_GeneratedInjector { *; }
-keep class **_Factory { *; }

# ---------- Room 数据库 ----------
# 保留 RoomDatabase 子类
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.room.RoomDatabase { *; }
# 保留 @Entity 数据类（字段被反射访问）
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * {
    <fields>;
    <init>();
}
# 保留 @Dao 接口
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Dao class * {
    <methods>;
}
# 保留 @Database 注解类
-keep @androidx.room.Database class *
-keepclassmembers @androidx.room.Database class * {
    <methods>;
}
# 保留 Room 类型转换器
-keep @androidx.room.TypeConverter class *
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-keep class androidx.room.FtsOptions { *; }
-keep class androidx.room.Relation { *; }
-keep class androidx.room.Embedded { *; }
-keep class androidx.room.PrimaryKey { *; }
-dontwarn androidx.room.paging.**

# ---------- Compose ----------
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
# 保留 @Composable 与 @Stable 注解类型
-keep @androidx.compose.runtime.Composable class *
-keep @androidx.compose.runtime.Stable class *
-keep @androidx.compose.runtime.Immutable class *
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
    @androidx.compose.runtime.Stable <fields>;
}
# 保留 Compose 生成代码
-keep class **$$Composable { *; }
-keep class **ComposableSingletons$* { *; }

# ---------- Coroutines ----------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }

# ---------- OkHttp / Retrofit ----------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
# 保留 Retrofit 接口（API 接口定义）
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking interface retrofit2.Response
-keep,allowobfuscation,allowshrinking class retrofit2.OkHttpCall$ExceptionCatchingRequestBody
# 保留 OkHttp 平台相关类
-keep class okhttp3.internal.platform.** { *; }
-keep class okio.** { *; }

# ---------- Coil ----------
-dontwarn coil.**

# ---------- WorkManager ----------
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keepclassmembers class * extends androidx.work.ListenableWorker {
    <init>(...);
}

# ---------- DataStore ----------
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ---------- Accompanist ----------
-dontwarn com.google.accompanist.**

# ---------- LeakCanary ----------
-dontwarn com.squareup.leakcanary.**
-keep class com.squareup.leakcanary.** { *; }

# ---------- 枚举保留 ----------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---------- 反射相关保留 ----------
-keep class java.lang.reflect.** { *; }
-keep class sun.misc.** { *; }
-dontwarn sun.misc.**
-keep class * implements java.io.Serializable { *; }

# ---------- data class 保留 ----------
# 保留 data class 的核心方法（toString/equals/hashCode/copy）
-keepclassmembers class com.timemark.app.** {
    public java.lang.String toString();
    public boolean equals(java.lang.Object);
    public int hashCode();
    public ** copy(...);
    public ** component1();
    public ** component2();
    public ** component3();
    public ** component4();
    public ** component5();
    public ** component6();
    public ** component7();
    public ** component8();
    public ** component9();
    public ** component10();
}

# ---------- 项目自身 Model 类 ----------
# 领域模型（被 Room / Serialization / Retrofit 反射访问）
-keep class com.timemark.app.domain.model.** { *; }
-keepclassmembers class com.timemark.app.domain.model.** { *; }
# 数据库 Entity
-keep class com.timemark.app.data.db.entity.** { *; }
-keepclassmembers class com.timemark.app.data.db.entity.** { *; }
# AI 请求/响应模型（被序列化框架反射）
-keep class com.timemark.app.ai.** { *; }
-keepclassmembers class com.timemark.app.ai.** { *; }
# DI 模块中的 Provider 实现类
-keep class com.timemark.app.di.** { *; }
# Application / Activity / Receiver / Service
-keep class com.timemark.app.TimeMarkApp { *; }
-keep class com.timemark.app.MainActivity { *; }
-keep class com.timemark.app.reminder.** { *; }

# ---------- 优化选项 ----------
# 关闭 R8 对部分库的警告
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.lang.model.element.**
-dontwarn com.google.errorprone.**
# 保留泛型签名（用于 Kotlin/Java 互操作）
-keepattributes Signature, InnerClasses, EnclosingMethod
# 保留注解默认值
-keepattributes AnnotationDefault
