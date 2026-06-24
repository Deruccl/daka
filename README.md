# 时光印记 TimeMark

<p align="center">
  <img src="app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml" alt="TimeMark Logo" width="120" height="120"/>
</p>

<p align="center">
  <strong>全能单机打卡助手 · 让每一份坚持都被铭记</strong>
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android"/>
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?logo=kotlin"/>
  <img alt="Compose" src="https://img.shields.io/badge/Jetpack%20Compose-2024.02-4285F4"/>
  <img alt="Material 3" src="https://img.shields.io/badge/Material-3-757575"/>
  <img alt="Min SDK" src="https://img.shields.io/badge/minSDK-26-FF6F00"/>
  <img alt="Target SDK" src="https://img.shields.io/badge/targetSDK-34-FF6F00"/>
  <img alt="License" src="https://img.shields.io/badge/License-MIT-green"/>
</p>

---

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [截图预览](#截图预览)
- [下载安装](#下载安装)
- [使用指南](#使用指南)
- [开发指南](#开发指南)
- [测试说明](#测试说明)
- [贡献指南](#贡献指南)
- [开源许可](#开源许可)
- [版本历史](#版本历史)

---

## 项目简介

**时光印记（TimeMark）** 是一款完全离线的 Android 原生打卡应用。无账号、无云端、无后台服务，所有数据本地存储并加密保护，让用户完全掌控自己的数据。

### 核心定位

- **完全离线**：无需注册登录，无需网络连接，所有数据存储在本地设备。
- **隐私至上**：数据加密存储，应用锁保护，绝不收集任何用户数据。
- **高度自定义**：支持 6 种打卡类型，自定义图标、颜色、提醒、目标。
- **AI 智能分析**：集成 10 个 AI 厂商，支持食物识别、营养分析、智能对话。
- **液态玻璃 UI**：Material 3 设计语言 + 液态玻璃质感，视觉与触觉的双重享受。

### 特色亮点

| 特色 | 说明 |
|------|------|
| 液态玻璃 UI | 基于 Material 3 的玻璃拟态设计，模糊、光影、动效一应俱全 |
| 自定义打卡系统 | 计数 / 时长 / 数值 / 勾选 / 图文 / 计时 6 种类型 |
| AI 智能分析 | 10 个 AI 厂商可选，食物识别、营养分析、聊天助手 |
| 完整离线 | 数据本地化，ZIP 备份恢复，CSV/JSON 导出 |
| 数据加密 | Keystore 加密 API Key，可选数据库加密 |
| 6 级时间视图 | 分钟 / 小时 / 日 / 周 / 月 / 年统计 |
| 自定义图表 | 折线 / 柱状 / 热力 / 饼图，Canvas 自定义绘制 |
| 智能提醒 | AlarmManager + WorkManager，快捷操作、智能提醒 |

---

## 功能特性

### 打卡管理

- **6 种打卡类型**：计数型（喝水杯数）、时长型（运动时长）、数值型（体重）、勾选型（完成/未完成）、图文型（饮食笔记）、计时型（自动计时）
- **4 步创建引导**：基本信息 → 类型配置 → 提醒设置 → 外观定制
- **模板库**：内置 20+ 常用打卡模板，一键创建
- **自定义外观**：图标选择器、颜色选择器、个性化展示
- **编辑与删除**：复用创建表单，删除前确认提示

### 记录管理

- **今日打卡**：首页快速打卡，进度环可视化
- **历史记录**：时间轴展示，按日期浏览
- **记录编辑**：支持修改数值、备注、时间
- **记录删除**：长按删除，二次确认

### 统计分析

- **6 级时间视图**：分钟 / 小时 / 日 / 周 / 月 / 年
- **4 种图表类型**：
  - 折线图：趋势变化
  - 柱状图：对比分析
  - 热力图：密度分布（类 GitHub 贡献图）
  - 饼图：占比分析
- **多打卡项对比**：选择多个打卡项同时查看
- **自定义日期范围**：灵活选择统计区间

### AI 智能分析

- **10 个 AI 厂商支持**：
  - 国际：OpenAI、Anthropic、Gemini
  - 国内：百度文心、阿里通义、字节豆包、智谱 GLM、Moonshot
  - 本地：Ollama
  - 自定义：兼容 OpenAI 协议的任意服务
- **食物识别**：拍照识别食物，自动估算营养
- **营养分析**：基于打卡数据分析饮食结构
- **AI 聊天助手**：智能对话，解答打卡疑问
- **协同模式**：多模型协同，故障自动转移
- **Token 统计**：用量监控、费用统计、预算限制

### 设置与隐私

- **主题模式**：浅色 / 深色 / 跟随系统
- **液态玻璃**：模糊效果开关、强度调节
- **动画系统**：页面切换、组件交互、列表动画
- **音效与触觉**：打卡音效、触觉反馈
- **应用锁**：密码 / 生物识别（指纹/面部）
- **数据库加密**：可选 SQLCipher 加密
- **数据备份**：ZIP 备份/恢复、CSV/JSON 导出

### 提醒系统

- **多种频率**：每天 / 每周指定日 / 间隔小时 / 智能提醒
- **快捷操作**：通知栏快速打卡
- **智能提醒**：基于历史打卡习惯智能推送
- **开机自启**：BootReceiver 自动恢复提醒

---

## 技术栈

### 语言与平台

| 技术 | 版本 | 说明 |
|------|------|------|
| Kotlin | 1.9.22 | 主开发语言 |
| Java | 17 | 编译版本 |
| Android Gradle Plugin | 8.2.2 | 构建工具 |
| minSdk | 26 | Android 8.0 |
| targetSdk | 34 | Android 14 |
| compileSdk | 34 | Android 14 |

### 核心框架

| 框架 | 版本 | 用途 |
|------|------|------|
| Jetpack Compose | BOM 2024.02.00 | 声明式 UI |
| Material 3 | 1.2.0 | 设计系统 |
| Navigation Compose | 2.7.7 | 导航 |
| Lifecycle | 2.7.0 | 生命周期 |
| Hilt | 2.51 | 依赖注入 |
| Room | 2.6.1 | 数据库 |
| DataStore | 1.0.0 | 偏好设置 |
| Coroutines | 1.7.3 | 异步 |
| Kotlinx Serialization | 1.6.2 | JSON 序列化 |

### 网络与多媒体

| 库 | 版本 | 用途 |
|------|------|------|
| OkHttp | 4.12.0 | HTTP 客户端 |
| Retrofit | 2.9.0 | HTTP 框架（可选） |
| Coil | 2.5.0 | 图片加载 |
| Accompanist | 0.34.0 | 权限处理 |

### 工具与测试

| 库 | 版本 | 用途 |
|------|------|------|
| WorkManager | 2.9.0 | 后台任务 |
| LeakCanary | 2.13 | 内存泄漏检测 |
| JUnit | 4.13.2 | 单元测试 |
| Espresso | 3.5.1 | UI 测试 |
| Compose Test | 1.6.2 | Compose 测试 |
| Room Testing | 2.6.1 | 数据库测试 |

---

## 项目结构

```
TimeMark/
├── app/                        # 应用主模块
│   ├── src/main/java/com/timemark/app/
│   │   ├── di/                 # Hilt 依赖注入模块
│   │   │   ├── AIModule.kt
│   │   │   ├── DataStoreModule.kt
│   │   │   ├── DatabaseModule.kt
│   │   │   ├── NetworkModule.kt
│   │   │   └── RepositoryModule.kt
│   │   ├── performance/        # 性能配置
│   │   ├── reminder/           # 提醒系统
│   │   │   ├── BootReceiver.kt
│   │   │   ├── ReminderReceiver.kt
│   │   │   ├── ReminderScheduler.kt
│   │   │   └── ReminderWorker.kt
│   │   ├── ui/
│   │   │   ├── navigation/     # 导航框架
│   │   │   │   ├── BottomNavItem.kt
│   │   │   │   ├── Route.kt
│   │   │   │   └── TimeMarkNavHost.kt
│   │   │   └── ScaffoldMain.kt
│   │   ├── MainActivity.kt
│   │   └── TimeMarkApp.kt      # Application 类
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── core/                       # 核心工具模块
│   └── src/main/java/com/timemark/app/core/
│       ├── extensions/         # Kotlin 扩展
│       ├── utils/              # 工具类
│       │   ├── ColorUtils.kt
│       │   ├── HapticFeedback.kt
│       │   ├── Logger.kt
│       │   ├── Result.kt
│       │   └── TimeUtils.kt
│       └── ui/                 # 通用 UI 组件与动画
│
├── data/                       # 数据层模块
│   └── src/main/java/com/timemark/app/data/
│       ├── datastore/          # DataStore 偏好设置
│       ├── db/                 # Room 数据库
│       ├── mapper/             # Entity ↔ Domain 映射
│       ├── repository/         # Repository 实现
│       ├── security/           # Keystore 加密
│       └── util/               # JSON 工具
│
├── domain/                     # 领域层模块
│   └── src/main/java/com/timemark/app/domain/
│       ├── model/              # 数据模型（9 个）
│       ├── repository/         # Repository 接口（6 个）
│       └── Result.kt           # 业务结果封装
│
├── ai/                         # AI 服务模块
│   └── src/main/java/com/timemark/app/ai/
│       ├── provider/           # AI 厂商 Provider（10 个）
│       ├── AIServiceImpl.kt    # AI 服务实现
│       └── CollaborativeService.kt
│
├── feature-home/               # 首页功能模块
├── feature-tracker/            # 打卡管理功能模块
├── feature-stats/              # 统计功能模块
├── feature-ai/                 # AI 功能模块
├── feature-settings/           # 设置功能模块
│
├── gradle/
│   ├── wrapper/
│   └── libs.versions.toml      # 版本目录
│
├── build.gradle.kts            # 根构建脚本
├── settings.gradle.kts         # 多模块配置
├── gradle.properties
├── keystore.properties.example # 签名配置示例
├── build-and-sign.bat          # Windows 构建脚本
├── build-and-sign.ps1          # PowerShell 构建脚本
└── README.md
```

### 模块说明

| 模块 | 职责 | 依赖 |
|------|------|------|
| `app` | 应用入口、DI、导航、提醒 | 所有模块 |
| `core` | 通用工具、扩展、UI 组件 | 无 |
| `data` | 数据库、DataStore、Repository 实现 | `core`, `domain` |
| `domain` | 数据模型、Repository 接口、UseCase | `core` |
| `ai` | AI Provider、AI 服务 | `core`, `domain` |
| `feature-home` | 首页 | `core`, `domain` |
| `feature-tracker` | 打卡创建/编辑/详情 | `core`, `domain` |
| `feature-stats` | 统计与图表 | `core`, `domain` |
| `feature-ai` | AI 配置、聊天、食物识别 | `core`, `domain` |
| `feature-settings` | 设置、备份恢复、关于 | `core`, `domain` |

---

## 快速开始

### 环境要求

- **JDK**：17 或以上
- **Android Studio**：Hedgehog (2023.1.1) 或以上
- **Android SDK**：compileSdk 34，minSdk 26
- **Gradle**：8.2+（项目自带 wrapper）
- **Kotlin**：1.9.22（项目自动配置）

### 克隆项目

```bash
git clone <repository-url>
cd TimeMark
```

### 构建项目

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

### 运行项目

1. 使用 USB 连接 Android 设备（开启开发者模式与 USB 调试），或启动模拟器。
2. 在 Android Studio 中点击 **Run** 按钮，或执行：

```bash
gradlew.bat installDebug
```

3. 应用安装完成后，桌面会出现「时光印记」图标，点击即可启动。

---

## 截图预览

> 截图待补充，以下为功能页面占位。

| 首页 | 统计 | AI | 设置 |
|------|------|-----|------|
| ![首页](docs/screenshots/home.png) | ![统计](docs/screenshots/stats.png) | ![AI](docs/screenshots/ai.png) | ![设置](docs/screenshots/settings.png) |

- **首页**：日期切换、问候语、今日进度环、打卡卡片列表
- **统计**：6 级时间视图、4 种图表、多打卡项对比
- **AI**：食物识别、营养分析、AI 聊天助手
- **设置**：主题、模糊、动画、应用锁、数据备份

---

## 下载安装

### APK 位置

构建完成后，APK 文件位于：

```
app/build/outputs/apk/release/
```

文件名格式：`TimeMark-v1.0.0-1-<abi>-release-<date>.apk`

- `universal`：通用 APK（兼容所有架构）
- `arm64-v8a`：64 位 ARM（推荐现代设备）
- `armeabi-v7a`：32 位 ARM（老设备）
- `x86_64`：模拟器

### 签名说明

Release 版本使用自签名证书。详见 [部署运行说明](docs/DEPLOYMENT.md#release-构建签名)。

### 安装方式

**方式一：adb 安装**

```bash
adb install -r app/build/outputs/apk/release/TimeMark-v1.0.0-1-universal-release-20240101.apk
```

**方式二：直接安装**

将 APK 文件传输到手机，点击安装（需开启「允许未知来源应用」）。

---

## 使用指南

### 创建打卡项

1. 打开应用，点击首页右下角 **+** 按钮。
2. **第 1 步 - 基本信息**：输入名称、描述，选择图标与颜色。
3. **第 2 步 - 类型配置**：选择打卡类型（计数/时长/数值/勾选/图文/计时），设置目标值与单位。
4. **第 3 步 - 提醒设置**：选择提醒频率（每天/每周/间隔/智能），设置提醒时间。
5. **第 4 步 - 外观定制**：确认图标、颜色、展示样式。
6. 点击 **完成** 创建打卡项。

> 💡 也可在「模板库」中选择预设模板，一键创建。

### 记录管理

- **快速打卡**：首页点击打卡卡片，根据类型快速记录。
- **查看详情**：点击打卡卡片进入详情页，查看历史时间轴。
- **编辑记录**：详情页点击记录项，修改数值、备注、时间。
- **删除记录**：详情页长按记录项，确认后删除。

### 统计查看

1. 点击底部导航 **统计** Tab。
2. 使用 **日期导航器** 选择日期范围。
3. 使用 **视图级别** 切换分钟/小时/日/周/月/年。
4. 使用 **打卡项选择器** 选择单个或多个打卡项。
5. 查看折线图、柱状图、热力图、饼图。

### AI 配置

1. 点击底部导航 **AI** Tab。
2. 点击 **AI 配置** → **新增配置**。
3. 选择 AI 厂商（OpenAI / Anthropic / Gemini / 百度 / 阿里 等）。
4. 填入 API Key、模型名称、Base URL（可选）。
5. 点击 **测试连接** 验证配置。
6. 保存后即可使用食物识别、营养分析、AI 聊天功能。

> 📖 详细的 AI Provider 扩展指南见 [AI Provider 指南](docs/AI_PROVIDER_GUIDE.md)。

---

## 开发指南

### 开发环境

- **IDE**：Android Studio Hedgehog (2023.1.1) 或以上
- **JDK**：17（项目内置 `compileOptions` 配置）
- **Kotlin**：1.9.22
- **Gradle**：8.2+（使用项目自带 `gradlew`）

### 调试

```bash
# 构建 Debug 版本
gradlew.bat assembleDebug

# 安装到设备并运行
gradlew.bat installDebug

# 查看日志
adb logcat -s TimeMark
```

**LeakCanary**：Debug 版本自动集成 LeakCanary，内存泄漏会在通知栏提示。

**Compose 报告**：构建后可在 `app/build/compose_reports/` 查看 Compose 性能报告。

### 构建 Release

详见 [部署运行说明](docs/DEPLOYMENT.md)。

快速步骤：

1. 复制 `keystore.properties.example` 为 `keystore.properties`。
2. 填入密钥库信息，将 `.jks` 文件放在项目根目录。
3. 运行构建脚本：

```bash
# Windows Batch
build-and-sign.bat

# PowerShell
.\build-and-sign.ps1
```

4. APK 输出至 `app/build/outputs/apk/release/`。

---

## 测试说明

### 单元测试

```bash
# 运行所有单元测试
gradlew.bat test

# 运行指定模块测试
gradlew.bat :core:test
gradlew.bat :domain:test
gradlew.bat :data:test
gradlew.bat :ai:test
```

### UI 测试

```bash
# 运行 UI 测试（需连接设备或模拟器）
gradlew.bat connectedAndroidTest
```

### 性能测试

```bash
# 运行性能测试
gradlew.bat connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.timemark.app.performance.*
```

详细的测试结果见 [测试报告](docs/TEST_REPORT.md)。

---

## 贡献指南

欢迎贡献代码！请遵循以下流程：

1. **Fork** 本仓库。
2. 创建特性分支：`git checkout -b feature/your-feature`。
3. 提交更改：`git commit -m "feat: 添加 XXX 功能"`。
4. 推送分支：`git push origin feature/your-feature`。
5. 提交 **Pull Request**。

### 提交规范

| 前缀 | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 文档更新 |
| `style` | 代码格式（不影响功能） |
| `refactor` | 重构（非新功能、非修复） |
| `test` | 测试相关 |
| `chore` | 构建/工具变更 |

### 代码规范

- 遵循 [Kotlin 官方代码风格](https://kotlinlang.org/docs/coding-conventions.html)。
- 使用 `ktlint` 检查代码风格。
- 新增功能需配套单元测试。
- 公共 API 需添加 KDoc 注释。

---

## 开源许可

本项目基于 **MIT License** 开源。

```
MIT License

Copyright (c) 2024 TimeMark

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### 第三方库许可

本项目使用的第三方库及其许可证详见 [THIRD_PARTY_LICENSES](docs/THIRD_PARTY_LICENSES.md)（待补充）。

主要依赖：

- [Kotlin](https://kotlinlang.org/) - Apache 2.0
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Apache 2.0
- [Material 3](https://m3.material.io/) - Apache 2.0
- [Hilt](https://dagger.dev/hilt/) - Apache 2.0
- [Room](https://developer.android.com/jetpack/androidx/releases/room) - Apache 2.0
- [OkHttp](https://square.github.io/okhttp/) - Apache 2.0
- [Coil](https://coil-kt.github.io/coil/) - Apache 2.0

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2024-02 | 首个正式版本 |

### v1.0.0（2024-02）

**新增功能：**

- 完成所有核心功能：打卡管理、记录管理、统计分析、AI 智能分析
- 液态玻璃 UI 主题系统
- 6 种打卡类型、4 种图表、6 级时间视图
- 10 个 AI 厂商集成
- 数据备份恢复、CSV/JSON 导出
- 提醒系统（AlarmManager + WorkManager）
- 应用锁、数据加密
- 完整动画系统

**技术优化：**

- 多模块 Clean Architecture
- Hilt 依赖注入
- Room 数据库 + DataStore
- Coroutines/Flow 响应式
- R8/ProGuard 代码混淆
- APK 分包（ABI 拆分）
- LeakCanary 内存泄漏检测
- 268 个单元测试 + 30 个 UI 测试

---

<p align="center">
  <strong>时光印记 · 让每一份坚持都被铭记</strong>
</p>

<p align="center">
  <a href="docs/ARCHITECTURE.md">架构设计</a> ·
  <a href="docs/AI_PROVIDER_GUIDE.md">AI Provider 指南</a> ·
  <a href="docs/DEPLOYMENT.md">部署运行</a> ·
  <a href="docs/TEST_REPORT.md">测试报告</a>
</p>
