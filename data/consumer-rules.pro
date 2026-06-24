# ====================================
# data 模块消费者 ProGuard 规则
# ====================================
# 本规则会自动应用到依赖 :data 模块的模块（如 :app）
# ====================================

# ---------- Room 数据库 ----------
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * {
    <fields>;
    <init>();
}
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Dao class * {
    <methods>;
}
-keep @androidx.room.Database class *
-keepclassmembers @androidx.room.Database class * {
    <methods>;
}
-keep @androidx.room.TypeConverter class *
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-dontwarn androidx.room.paging.**

# ---------- Kotlinx Serialization ----------
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class **$$serializer { *; }

# ---------- DataStore ----------
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ---------- 项目 Entity 与 Mapper ----------
-keep class com.timemark.app.data.db.entity.** { *; }
-keepclassmembers class com.timemark.app.data.db.entity.** { *; }
