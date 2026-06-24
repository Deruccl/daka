# 部署运行说明

> 本文档描述「时光印记 TimeMark」Android 应用的开发环境搭建、项目导入、Debug/Release 构建、APK 生成与安装、设备兼容性、数据备份恢复、常见问题排查与性能调优。

## 目录

- [开发环境要求](#开发环境要求)
- [项目导入步骤](#项目导入步骤)
- [Debug 构建运行](#debug-构建运行)
- [Release 构建签名](#release-构建签名)
- [APK 生成](#apk-生成)
- [APK 安装](#apk-安装)
- [设备兼容性](#设备兼容性)
- [数据备份与恢复操作](#数据备份与恢复操作)
- [常见问题排查](#常见问题排查)
- [性能调优建议](#性能调优建议)

---

## 开发环境要求

### 必需环境

| 环境 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 17 或以上 | 项目 `compileOptions` 强制 Java 17 |
| **Android Studio** | Hedgehog (2023.1.1) 或以上 | 推荐 Iguana (2023.2.1) |
| **Android SDK** | compileSdk 34 | 需安装 Android 14 (API 34) SDK Platform |
| **Gradle** | 8.2+ | 项目自带 `gradlew`，无需单独安装 |
| **Kotlin** | 1.9.22 | 由项目自动配置 |

### SDK 组件

| 组件 | 版本 | 用途 |
|------|------|------|
| Android SDK Platform | 34 (Android 14) | 编译 |
| Android SDK Build-Tools | 34.0.0 | 构建 |
| Android SDK Platform-Tools | 最新 | adb 等工具 |
| Android Emulator | 最新 | 模拟器（可选） |

### JDK 17 安装验证

```bash
# 验证 JDK 版本
java -version
# 输出应包含 "17"，例如：
# openjdk version "17.0.10" 2024-01-16

# 验证 JAVA_HOME 环境变量
echo %JAVA_HOME%
# 应指向 JDK 17 安装目录
```

> 💡 Android Studio 自带 JBR (JetBrains Runtime)，通常无需单独安装 JDK。若命令行构建报错，请检查 `JAVA_HOME` 是否指向 JDK 17。

---

## 项目导入步骤

### 方式一：Android Studio 导入（推荐）

1. 打开 Android Studio。
2. 选择 **File → Open**（或欢迎页 **Open**）。
3. 浏览到项目根目录 `e:\daka`，选择 `build.gradle.kts` 文件。
4. 点击 **OK**，等待 Gradle Sync 完成。
5. 同步完成后，项目结构将显示在左侧 Project 面板。

### 方式二：命令行导入

```bash
# 克隆项目（如使用 Git）
git clone <repository-url>
cd TimeMark

# 验证 Gradle 可用
gradlew.bat --version

# 列出所有模块
gradlew.bat projects
```

### Gradle Sync 失败处理

若同步失败，请检查：

1. **网络连接**：Gradle 需下载依赖，确保网络畅通，建议配置镜像源。
2. **JDK 版本**：`File → Settings → Build → Gradle → Gradle JDK` 选择 17。
3. **SDK 路径**：`local.properties` 中 `sdk.dir` 指向正确的 SDK 路径。
4. **代理设置**：如使用代理，在 `gradle.properties` 中配置 `systemProp.http.proxyHost` 等。

---

## Debug 构建运行

### 命令行构建

```bash
# 构建 Debug APK
gradlew.bat assembleDebug

# 安装到已连接设备
gradlew.bat installDebug
```

### Android Studio 运行

1. 连接 Android 设备（开启 USB 调试）或启动模拟器。
2. 选择运行配置 **app**。
3. 点击 **Run** 按钮（绿色三角形）或按 `Shift+F10`。

### Debug APK 位置

```
app/build/outputs/apk/debug/app-debug.apk
```

### Debug 版本特性

- **LeakCanary**：自动集成内存泄漏检测，泄漏时通知栏提示。
- **Compose 报告**：构建后在 `app/build/compose_reports/` 查看性能报告。
- **日志输出**：`Logger` 工具输出 Debug 级别日志。
- **未混淆**：便于调试，代码不混淆。

---

## Release 构建签名

### 步骤 1：生成密钥库

使用 `keytool` 生成签名密钥库（.jks 文件）：

```bash
keytool -genkeypair -v -keystore timemark.jks -alias timemark -keyalg RSA -keysize 2048 -validity 10000
```

按提示输入：

- 密钥库口令（storePassword）
- 姓名、组织、城市等信息
- 密钥口令（keyPassword，可与密钥库口令相同）

生成的 `timemark.jks` 文件放在项目根目录。

> ⚠️ **重要**：妥善保管 `.jks` 文件与口令，丢失将无法更新应用！切勿将 `.jks` 与 `keystore.properties` 提交到版本控制。

### 步骤 2：配置 keystore.properties

复制示例文件：

```bash
copy keystore.properties.example keystore.properties
```

编辑 `keystore.properties`，填入真实信息：

```properties
storeFile=timemark.jks
storePassword=your_store_password
keyAlias=timemark
keyPassword=your_key_password
```

### 步骤 3：执行构建

**方式一：使用构建脚本（推荐）**

```bash
# Windows Batch
build-and-sign.bat

# PowerShell
.\build-and-sign.ps1
```

**方式二：直接 Gradle 命令**

```bash
gradlew.bat assembleRelease
```

### 步骤 4：验证签名

```bash
# 验证 APK 签名
apksigner verify --verbose app\build\outputs\apk\release\TimeMark-v1.0.0-1-universal-release-20240101.apk
```

输出应包含 `Verifies` 与 `Signed using v1 scheme` / `v2 scheme` / `v3 scheme`。

### 签名配置说明

`app/build.gradle.kts` 中的签名配置：

```kotlin
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
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
            signingConfig = signingConfigs.getByName("release")
            // ...
        }
    }
}
```

---

## APK 生成

### APK 输出位置

构建完成后，APK 文件位于：

```
app/build/outputs/apk/release/
```

### APK 文件命名

文件名格式：`TimeMark-v{versionName}-{versionCode}-{abi}-{buildType}-{date}.apk`

示例：

```
TimeMark-v1.0.0-1-universal-release-20240201.apk
TimeMark-v1.0.0-1-arm64-v8a-release-20240201.apk
TimeMark-v1.0.0-1-armeabi-v7a-release-20240201.apk
TimeMark-v1.0.0-1-x86_64-release-20240201.apk
```

### ABI 分包说明

项目配置了 ABI 分包，生成 4 个 APK：

| APK | ABI | 适用设备 | 体积 |
|-----|-----|----------|------|
| `universal` | 所有架构 | 兼容所有设备 | 最大 |
| `arm64-v8a` | 64 位 ARM | 现代手机（推荐） | 较小 |
| `armeabi-v7a` | 32 位 ARM | 老设备 | 较小 |
| `x86_64` | x86 64 位 | 模拟器 | 较小 |

**推荐安装**：现代设备安装 `arm64-v8a` 版本，体积更小、性能更优。

### 构建配置

`app/build.gradle.kts` 中的 Release 配置：

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true       // R8 代码混淆
        isShrinkResources = true     // 资源压缩
        isCrunchPngs = true          // PNG 压缩
        signingConfig = signingConfigs.getByName("release")
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}

splits {
    abi {
        isEnable = true
        reset()
        include("armeabi-v7a", "arm64-v8a", "x86_64")
        isUniversalApk = true
    }
}
```

---

## APK 安装

### 方式一：adb 安装

```bash
# 安装 universal APK（推荐）
adb install -r app\build\outputs\apk\release\TimeMark-v1.0.0-1-universal-release-20240201.apk

# 安装指定 ABI APK
adb install -r app\build\outputs\apk\release\TimeMark-v1.0.0-1-arm64-v8a-release-20240201.apk
```

参数说明：

- `-r`：重新安装，保留数据
- `-d`：允许降级安装
- `-g`：自动授予所有权限

### 方式二：直接安装

1. 将 APK 文件传输到手机（USB / 蓝牙 / 网盘）。
2. 在手机文件管理器中点击 APK 文件。
3. 允许「未知来源应用」安装。
4. 点击 **安装**。

### 方式三：Android Studio 安装

1. 在 Android Studio 中选择 **Build → Build APK(s) → Build APK(s)**。
2. 构建完成后点击通知 **locate** 打开 APK 所在文件夹。
3. 拖拽 APK 到模拟器或使用 adb 安装。

---

## 设备兼容性

### SDK 版本兼容

| 配置 | 值 | 说明 |
|------|-----|------|
| minSdk | 26 | Android 8.0 |
| targetSdk | 34 | Android 14 |
| compileSdk | 34 | Android 14 |

### Android 版本兼容范围

| Android 版本 | API Level | 兼容性 | 说明 |
|--------------|-----------|--------|------|
| Android 8.0 | 26 | ✅ 最低支持 | minSdk |
| Android 8.1 | 27 | ✅ | |
| Android 9 | 28 | ✅ | |
| Android 10 | 29 | ✅ | |
| Android 11 | 30 | ✅ | |
| Android 12 | 31 | ✅ | |
| Android 12L | 32 | ✅ | |
| Android 13 | 33 | ✅ | |
| Android 14 | 34 | ✅ 目标版本 | targetSdk |

### ABI 架构支持

| 架构 | 支持 | 说明 |
|------|------|------|
| `arm64-v8a` | ✅ | 64 位 ARM，现代设备主流 |
| `armeabi-v7a` | ✅ | 32 位 ARM，老设备 |
| `x86_64` | ✅ | 模拟器 / 部分 Chromebook |
| `x86` | ❌ | 已弃用，使用 x86_64 |
| `mips` | ❌ | 已弃用 |

### 屏幕适配

- **最小宽度**：支持 `sw360dp` 及以上（主流手机）
- **横竖屏**：默认竖屏，部分页面支持横屏
- **折叠屏**：自适应布局
- **平板**：响应式布局（未来支持）

---

## 数据备份与恢复操作

### 应用内备份

1. 打开应用，进入 **设置 → 备份与恢复**。
2. 点击 **备份**。
3. 选择保存位置（系统文件选择器）。
4. 等待备份完成，生成 `.zip` 文件。

### 应用内恢复

1. 进入 **设置 → 备份与恢复**。
2. 点击 **恢复**。
3. 选择之前备份的 `.zip` 文件。
4. 确认恢复，应用将重启。

### 备份文件内容

ZIP 文件包含：

- `timemark.db` - Room 数据库文件
- `datastore/` - DataStore 偏好设置
- `backup_meta.json` - 备份元信息（版本、时间）

### 数据导出

| 格式 | 用途 | 说明 |
|------|------|------|
| CSV | 表格分析 | 打卡记录导出，可用 Excel 打开 |
| JSON | 数据迁移 | 完整数据导出，可导入其他设备 |

### adb 备份（高级）

```bash
# 备份应用数据（需设备允许备份）
adb backup -f timemark_backup.ab -noapk com.timemark.app

# 恢复
adb restore timemark_backup.ab
```

> ⚠️ adb 备份在 Android 12+ 可能受限，建议优先使用应用内备份功能。

---

## 常见问题排查

### 构建失败

#### 问题 1：Gradle Sync 失败 - 下载依赖超时

**原因**：网络连接问题，无法访问 Maven 仓库。

**解决方案**：

配置镜像源，在 `settings.gradle.kts` 中添加国内镜像：

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
    }
}
```

#### 问题 2：JDK 版本不匹配

**错误信息**：`JDK 17 is required`

**解决方案**：

1. 安装 JDK 17。
2. `File → Settings → Build → Gradle → Gradle JDK` 选择 17。
3. 设置 `JAVA_HOME` 环境变量指向 JDK 17。

#### 问题 3：Kotlin 编译错误 - Compose Compiler 版本

**错误信息**：`Compose Compiler version mismatch`

**解决方案**：

确认 `gradle/libs.versions.toml` 中：

```toml
kotlin = "1.9.22"
composeCompiler = "1.5.8"
```

Kotlin 与 Compose Compiler 版本需匹配，参考 [Compose Compiler 版本映射](https://developer.android.com/jetpack/androidx/releases/compose-kotlin)。

#### 问题 4：Hilt 编译错误 - kapt 失败

**错误信息**：`[Hilt] Processing error`

**解决方案**：

1. 清理构建：`gradlew.bat clean`
2. 重新构建：`gradlew.bat assembleDebug`
3. 检查 `app/build.gradle.kts` 中 `kapt { correctErrorTypes = true }` 配置。

### 签名错误

#### 问题 1：keystore.properties 未找到

**错误信息**：`File 'keystore.properties' not found`

**解决方案**：

```bash
copy keystore.properties.example keystore.properties
# 编辑 keystore.properties 填入真实信息
```

#### 问题 2：密钥库口令错误

**错误信息**：`Keystore was tampered with, or password was incorrect`

**解决方案**：

1. 确认 `keystore.properties` 中的 `storePassword` 与 `keyPassword` 正确。
2. 重新生成密钥库（如口令遗忘，需重新签名，旧版本无法升级）。

#### 问题 3：APK 签名验证失败

**错误信息**：`Signer #1 certificate SHA-256 mismatch`

**解决方案**：

确保使用同一密钥库签名。如需更换签名，需先卸载旧版本：

```bash
adb uninstall com.timemark.app
adb install new.apk
```

### 运行崩溃

#### 问题 1：应用启动崩溃 - Hilt 注入失败

**日志**：`Hilt binding missing`

**解决方案**：

1. 检查 `app/di/` 下所有 Module 是否正确注册。
2. 确认 Repository 接口与实现的 `@Binds` 绑定。
3. 执行 `gradlew.bat clean assembleDebug` 重新构建。

#### 问题 2：Room 数据库迁移崩溃

**日志**：`A migration from X to Y was required but not found`

**解决方案**：

项目当前使用 `fallbackToDestructiveMigration()`，开发阶段会清空数据重建。正式版需实现 `Migration` 对象。

#### 问题 3：AI 请求失败

**日志**：`HTTP 401: Unauthorized`

**解决方案**：

1. 检查 AI 配置中的 API Key 是否正确。
2. 在 AI 配置页面点击「测试连接」验证。
3. 检查 baseUrl 是否正确（留空使用默认）。
4. 确认网络可访问目标 AI 服务。

#### 问题 4：OOM 内存溢出

**日志**：`OutOfMemoryError`

**解决方案**：

1. 增加 Gradle JVM 内存：`gradle.properties` 中 `org.gradle.jvmargs=-Xmx4096m`。
2. 检查图片加载，避免加载过大图片。
3. 使用 LeakCanary 检测内存泄漏。

---

## 性能调优建议

### 构建性能

#### 1. 启用并行构建

`gradle.properties` 已配置：

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=true
```

#### 2. 增大 JVM 内存

```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
```

#### 3. 关闭 Jetifier

```properties
android.enableJetifier=false
```

#### 4. 启用配置缓存（实验性）

```properties
org.gradle.configuration-cache=true
```

### 运行性能

#### 1. Compose 性能优化

- 使用 `@Stable` / `@Immutable` 注解减少重组
- 避免在 `Composable` 中创建新对象
- 使用 `derivedStateOf` 减少不必要重组
- 查看构建报告：`app/build/compose_reports/`

#### 2. 数据库优化

- 为高频查询字段建立索引
- 使用 Flow 响应式查询避免轮询
- 批量操作使用 `@Transaction`
- 大数据集使用分页（Paging 3）

#### 3. 内存优化

- LeakCanary 检测泄漏（Debug 自动启用）
- 图片使用 Coil 加载，自动缓存
- ViewModel 持有数据，避免 Activity 泄漏
- 长列表使用 `LazyColumn` / `LazyRow`

#### 4. 启动优化

- Baseline Profile：`app/src/main/baseline-prof.txt` 预编译热点路径
- 避免在 Application.onCreate 中执行耗时操作
- 使用 `androidx.startup` 延迟初始化非关键组件
- 启动时间目标：冷启动 < 1s，热启动 < 300ms

#### 5. APK 体积优化

- R8 代码混淆与压缩（已启用）
- 资源压缩（已启用）
- ABI 分包（已启用）
- 移除无用语言资源：`defaultConfig { resConfigs("zh", "en") }`
- 使用 WebP 替代 PNG

---

## 相关文档

- [README](../README.md) - 项目说明
- [架构设计](ARCHITECTURE.md) - 架构与性能设计
- [AI Provider 指南](AI_PROVIDER_GUIDE.md) - AI 配置指南
- [测试报告](TEST_REPORT.md) - 性能测试结果
