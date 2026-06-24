import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    id("kotlin-kapt")
}

// 读取 keystore.properties 文件中的签名配置（密钥库不纳入版本控制）
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.timemark.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.timemark.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // 签名配置：从 keystore.properties 读取，避免硬编码密钥
    signingConfigs {
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                keyAlias = keystoreProperties["keyAlias"] as String?
                keyPassword = keystoreProperties["keyPassword"] as String?
                storeFile = file(keystoreProperties["storeFile"] as String?)
                storePassword = keystoreProperties["storePassword"] as String?
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            // 应用 release 签名配置
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // 启用 Compose Compiler Metrics 与 Reports（性能优化分析）
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.layout.buildDirectory.get().asFile.absolutePath}/compose_reports",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.layout.buildDirectory.get().asFile.absolutePath}/compose_metrics"
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // APK 分包：按 ABI 拆分，减小包体积
    splits {
        abi {
            isEnable = true
            reset()
            // 主流 ABI：armeabi-v7a（32 位 ARM）、arm64-v8a（64 位 ARM）、x86_64（模拟器/部分设备）
            include("armeabi-v7a", "arm64-v8a", "x86_64")
            isUniversalApk = true
        }
    }
}

// 配置 APK 输出文件名（包含应用名、版本号、ABI、构建日期）
android.applicationVariants.all {
    val variant = this
    outputs.all {
        val outputImpl = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
        val date = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.CHINA)
            .format(java.util.Date())
        // 获取 ABI 过滤器（universal APK 时为 null）
        val abi = outputImpl.getFilter(
            com.android.build.api.variant.FilterConfiguration.FilterType.ABI.name
        ) ?: "universal"
        // 输出格式：TimeMark-v1.0.0-1-<abi>-release-<date>.apk
        outputImpl.outputFileName = "TimeMark-v${variant.versionName}-${variant.versionCode}" +
            "-${abi}-${variant.name}-$date.apk"
    }
}

dependencies {
    // Project modules
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":ai"))
    implementation(project(":feature-home"))
    implementation(project(":feature-tracker"))
    implementation(project(":feature-stats"))
    implementation(project(":feature-ai"))
    implementation(project(":feature-settings"))

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    kapt(libs.hilt.work.compiler)

    // Coroutines
    implementation(libs.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Network
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Accompanist
    implementation(libs.accompanist.permissions)

    // Biometric（应用锁生物识别）
    implementation(libs.androidx.biometric)

    // SQLCipher（数据库加密，DatabaseModule 中引用 SupportOpenHelperFactory）
    implementation(libs.sqlcipher)

    // LeakCanary (debug only)
    debugImplementation(libs.leakcanary.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.test.junit4)
    androidTestImplementation(libs.compose.ui.tooling)
    androidTestImplementation(libs.room.testing)
    debugImplementation(libs.compose.test.manifest)
}

// Required for Hilt kapt
kapt {
    correctErrorTypes = true
}
