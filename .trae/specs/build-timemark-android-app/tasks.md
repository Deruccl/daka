# Tasks

## 阶段一：项目基础架构

- [x] Task 1: 初始化 Android 项目与 Gradle 配置
  - [x] SubTask 1.1: 创建项目根目录结构、settings.gradle.kts（多模块配置）、根 build.gradle.kts、gradle.properties、libs.versions.toml 版本目录
  - [x] SubTask 1.2: 配置 app 模块 build.gradle.kts（compileSdk=34, minSdk=26, targetSdk=34, Compose, Kotlin 1.9+, Java 17）
  - [x] SubTask 1.3: 创建 core/data/domain/ai/feature-home/feature-tracker/feature-stats/feature-ai/feature-settings 模块的 build.gradle.kts
  - [x] SubTask 1.4: 配置 AndroidManifest.xml（权限：网络、通知、震动、指纹、闹钟、前台服务；Application 类注册）
  - [x] SubTask 1.5: 创建 gradle wrapper、proguard-rules.pro、签名配置占位

- [x] Task 2: 创建 Application 类与基础工具
  - [x] SubTask 2.1: 在 app 模块创建 TimeMarkApp Application 类（@HiltAndroidApp、通知渠道创建、WorkManager 初始化）
  - [x] SubTask 2.2: 创建 core 模块的包结构（com.timemark.app.core.utils / extensions / components）
  - [x] SubTask 2.3: 实现日志工具 Logger（分级日志、Release 关闭 Debug）

## 阶段二：数据层

- [x] Task 3: 定义领域数据模型
  - [x] SubTask 3.1: 在 domain 模块定义 Tracker、TrackerType、Record、DailyStats、AIConfig、AIUsage、AIProvider 等数据类
  - [x] SubTask 3.2: 定义枚举：TrackerType（COUNT/DURATION/VALUE/CHECK/IMAGE_TEXT/TIMER）、AIProvider、ThemeMode 等
  - [x] SubTask 3.3: 定义 Repository 接口：TrackerRepository、RecordRepository、StatsRepository、AIConfigRepository、AIUsageRepository、SettingsRepository

- [x] Task 4: 实现 Room 数据库
  - [x] SubTask 4.1: 在 data 模块定义 Entity（TrackerEntity、RecordEntity、DailyStatsEntity、AIConfigEntity、AIUsageEntity）
  - [x] SubTask 4.2: 实现 DAO（TrackerDao、RecordDao、StatsDao、AIConfigDao、AIUsageDao），含 Flow 查询
  - [x] SubTask 4.3: 实现 TimeMarkDatabase（@Database、版本、迁移策略、类型转换器 TypeConverters）
  - [x] SubTask 4.4: 创建数据库索引（tracker_id、date、timestamp 等）

- [x] Task 5: 实现 DataStore 偏好设置
  - [x] SubTask 5.1: 实现 SettingsDataStore（主题模式、模糊开关、动画开关、音效、触觉、24小时制、每周第一天、语言、应用锁、数据库加密）
  - [x] SubTask 5.2: 实现 KeystoreCrypto 工具（API Key 加密存储）

- [x] Task 6: 实现 Repository
  - [x] SubTask 6.1: 实现 TrackerRepositoryImpl、RecordRepositoryImpl、StatsRepositoryImpl
  - [x] SubTask 6.2: 实现 AIConfigRepositoryImpl、AIUsageRepositoryImpl、SettingsRepositoryImpl
  - [x] SubTask 6.3: 实现 DailyStats 自动计算与更新逻辑

## 阶段三：依赖注入与领域层

- [x] Task 7: 配置 Hilt 依赖注入
  - [x] SubTask 7.1: 创建 DatabaseModule（提供 Room 数据库和 DAO）
  - [x] SubTask 7.2: 创建 DataStoreModule（提供 SettingsDataStore）
  - [x] SubTask 7.3: 创建 RepositoryModule（绑定 Repository 接口与实现）
  - [x] SubTask 7.4: 创建 NetworkModule（提供 OkHttp、Retrofit、JSON 解析）
  - [x] SubTask 7.5: 创建 AIModule（提供 AI 服务）

- [x] Task 8: 实现 UseCase
  - [x] SubTask 8.1: 打卡相关：CreateTrackerUseCase、UpdateTrackerUseCase、DeleteTrackerUseCase、GetTrackersUseCase
  - [x] SubTask 8.2: 记录相关：AddRecordUseCase、UpdateRecordUseCase、DeleteRecordUseCase、GetRecordsUseCase
  - [x] SubTask 8.3: 统计相关：GetDailyStatsUseCase、GetRangeStatsUseCase、GetStreakUseCase
  - [x] SubTask 8.4: AI 相关：RecognizeFoodUseCase、AnalyzeNutritionUseCase、ChatWithAIUseCase

## 阶段四：UI 主题与设计系统

- [x] Task 9: 实现主题系统
  - [x] SubTask 9.1: 定义 Color.kt（主色/次色/第三色/语义色/中性色，浅色与深色调色板）
  - [x] SubTask 9.2: 定义 Type.kt（Display/Headline/Title/Body/Label 字体层级，Noto Sans SC + Inter）
  - [x] SubTask 9.3: 定义 Shape.kt（小/中/大/超大/完全圆角）
  - [x] SubTask 9.4: 定义 Spacing.kt（xs/sm/md/lg/xl/2xl/3xl 间距）
  - [x] SubTask 9.5: 实现 TimeMarkTheme（支持浅色/深色/跟随系统、Android 12+ 动态颜色）

- [x] Task 10: 实现液态玻璃组件
  - [x] SubTask 10.1: 实现 GlassCard（三级：LightGlass/StandardGlass/ThickGlass，模糊+渐变+高光+边框+阴影）
  - [x] SubTask 10.2: 实现 GlassButton（主按钮渐变填充、次按钮纯玻璃、小按钮轻薄玻璃）
  - [x] SubTask 10.3: 实现 GlassTopBar、GlassBottomBar、GlassDialog、GlassTextField
  - [x] SubTask 10.4: 实现模糊效果降级方案（低性能设备使用半透明纯色）
  - [x] SubTask 10.5: 实现液态动效（按压下沉、回弹过冲、水波纹）

- [x] Task 11: 实现通用 UI 组件
  - [x] SubTask 11.1: 实现 ProgressRing（环形进度条）、ProgressBar（带光效）
  - [x] SubTask 11.2: 实现 EmptyState（空状态插画+引导）、SkeletonScreen（骨架屏）
  - [x] SubTask 11.3: 实现 LoadingAnimation（液态加载动画）
  - [x] SubTask 11.4: 实现 CelebrationAnimation（完成打卡/达成目标/里程碑庆祝）

## 阶段五：导航与主框架

- [x] Task 12: 实现导航框架
  - [x] SubTask 12.1: 定义 Route（首页/统计/AI/我的/打卡详情/创建打卡/设置等）
  - [x] SubTask 12.2: 实现 TimeMarkNavHost（Navigation Compose 配置）
  - [x] SubTask 12.3: 实现 MainActivity（setContent、Theme、NavHost、应用锁检查）

- [x] Task 13: 实现主页面 Scaffold
  - [x] SubTask 13.1: 实现 GlassBottomBar（首页/统计/AI/我的，液态高亮移动效果）
  - [x] SubTask 13.2: 实现 Scaffold 布局（底部导航 + 内容区 + FAB）
  - [x] SubTask 13.3: 实现页面切换动画（淡入淡出+缩放，300ms）

## 阶段六：首页打卡列表

- [x] Task 14: 实现首页
  - [x] SubTask 14.1: 实现 HomeScreen（顶部日期+问候语+今日进度环+连续天数）
  - [x] SubTask 14.2: 实现 HomeViewModel（加载打卡列表、今日统计、StateFlow）
  - [x] SubTask 14.3: 实现 TrackerCard（图标/名称/今日进度/快捷操作按钮，已完成视觉变化）
  - [x] SubTask 14.4: 实现卡片交互（点击展开、快捷打卡、长按编辑、左右滑动切日期、下拉刷新）
  - [x] SubTask 14.5: 实现今日概览展开（完成率、连续天数、今日亮点）
  - [x] SubTask 14.6: 实现 FAB 添加按钮（跳转创建页）

## 阶段七：打卡创建与编辑

- [x] Task 15: 实现打卡创建流程
  - [x] SubTask 15.1: 实现 CreateTrackerScreen 分步引导（4 步：类型选择/基础设置/高级设置/预览确认）
  - [x] SubTask 15.2: 实现预设模板库（健康/学习/生活/工作四大类，横向滚动推荐）
  - [x] SubTask 15.3: 实现基础设置表单（名称/图标选择器/颜色选择器/单位/目标值/描述）
  - [x] SubTask 15.4: 实现高级设置（时间段/提醒设置/可见性/排序/AI 开关）
  - [x] SubTask 15.5: 实现预览确认页（展示卡片预览）
  - [x] SubTask 15.6: 实现 CreateTrackerViewModel 与保存逻辑

- [x] Task 16: 实现打卡编辑
  - [x] SubTask 16.1: 实现 EditTrackerScreen（复用创建表单，预填数据）
  - [x] SubTask 16.2: 实现删除打卡（确认提示，可选保留历史数据）

## 阶段八：打卡详情与记录管理

- [x] Task 17: 实现打卡详情页
  - [x] SubTask 17.1: 实现 TrackerDetailScreen（顶部返回+名称+更多操作）
  - [x] SubTask 17.2: 实现今日状态区（大字号数值、目标进度条、与昨日对比、快速操作）
  - [x] SubTask 17.3: 实现历史记录时间轴（日/周/月切换，可点击查看详情）
  - [x] SubTask 17.4: 实现 TrackerDetailViewModel

- [x] Task 18: 实现记录管理
  - [x] SubTask 18.1: 实现添加记录面板（数值输入/时间选择/备注/图片上传/标签）
  - [x] SubTask 18.2: 实现记录编辑（点击历史记录编辑）
  - [x] SubTask 18.3: 实现记录删除（确认提示 + 5 秒撤销）
  - [x] SubTask 18.4: 实现批量删除（按时间范围/按打卡项目）

## 阶段九：统计与时间视图

- [x] Task 19: 实现统计页面框架
  - [x] SubTask 19.1: 实现 StatsScreen（时间视图切换器、打卡项目选择器）
  - [x] SubTask 19.2: 实现 StatsViewModel（按时间范围聚合数据）
  - [x] SubTask 19.3: 实现双指缩放手势识别与视图级别切换

- [x] Task 20: 实现各级时间视图
  - [x] SubTask 20.1: 实现分钟视图（1 小时时间轴，5 分钟刻度，记录点脉冲动画）
  - [x] SubTask 20.2: 实现小时视图（24 小时柱状图/热力图，高峰高亮）
  - [x] SubTask 20.3: 实现日视图（日期栏+概览卡片+时间轴+打卡列表）
  - [x] SubTask 20.4: 实现周视图（7 天热力图+趋势折线+周统计）
  - [x] SubTask 20.5: 实现月视图（月历热力图+月度趋势+月度统计）
  - [x] SubTask 20.6: 实现年视图（GitHub 风格年度热力图+月度对比+年度总结）

- [x] Task 21: 实现图表组件
  - [x] SubTask 21.1: 实现 LineChartView（折线图，支持多曲线对比）
  - [x] SubTask 21.2: 实现 BarChartView（柱状图）
  - [x] SubTask 21.3: 实现 HeatMapView（热力图，日/周/月/年）
  - [x] SubTask 21.4: 实现 PieChartView（饼图，营养素比例）
  - [x] SubTask 21.5: 实现 ProgressRingView（环形进度，支持动画）

## 阶段十：AI 功能模块

- [x] Task 22: 实现 AI API 集成层
  - [x] SubTask 22.1: 定义统一 AIService 接口（chat/recognizeImage/transcribe）
  - [x] SubTask 22.2: 实现 OpenAIProvider、AnthropicProvider、GeminiProvider
  - [x] SubTask 22.3: 实现 国内厂商 Provider（百度/阿里/字节/智谱/Moonshot）
  - [x] SubTask 22.4: 实现 OllamaProvider（本地模型）和 CustomProvider（OpenAI 兼容）
  - [x] SubTask 22.5: 实现模型路由策略（按功能/智能路由/故障转移）
  - [x] SubTask 22.6: 实现多模态与非多模态协同（多模态识别→结构化文字→非多模态分析）

- [x] Task 23: 实现 AI 配置管理
  - [x] SubTask 23.1: 实现 AIConfigScreen（模型列表、添加/编辑/删除、拖拽排序）
  - [x] SubTask 23.2: 实现添加模型分步流程（选厂商/填 API/测试连接/高级设置）
  - [x] SubTask 23.3: 实现 Token 消耗统计页（日/周/月、费用估算、趋势图、预算提醒）
  - [x] SubTask 23.4: 实现协同模式配置（开关、模型配对、效果对比）

- [x] Task 24: 实现 AI 分析功能
  - [x] SubTask 24.1: 实现食物识别（拍照/相册→压缩→多模态识别→用户确认→保存）
  - [x] SubTask 24.2: 实现营养分析（热量/营养素/饮食习惯/个性化建议）
  - [x] SubTask 24.3: 实现饮水/运动/睡眠/习惯养成分析
  - [x] SubTask 24.4: 实现 AI 聊天助手（气泡式对话、富文本、多轮上下文、本地历史）
  - [x] SubTask 24.5: 实现 Token 优化（提示词精简、图片压缩、结果缓存、批量处理）

## 阶段十一：设置与其他功能

- [x] Task 25: 实现设置页面
  - [x] SubTask 25.1: 实现 SettingsScreen（主题/模糊/动画/音效/触觉/时间制/每周首日/语言）
  - [x] SubTask 25.2: 实现应用锁设置（密码/指纹/面部、自动锁定时间）
  - [x] SubTask 25.3: 实现数据库加密设置（SQLCipher 密码）
  - [x] SubTask 25.4: 实现关于页面、帮助页面

- [x] Task 26: 实现数据备份与导出
  - [x] SubTask 26.1: 实现完整备份（.zip 含数据库+图片+配置+版本）
  - [x] SubTask 26.2: 实现仅数据备份
  - [x] SubTask 26.3: 实现从备份恢复（恢复前自动备份、完全替换/合并、进度显示）
  - [x] SubTask 26.4: 实现自动备份（每日/每周/每月，保留 N 个版本）
  - [x] SubTask 26.5: 实现导出 CSV/JSON/PDF（全部/时间段/指定打卡项目）

- [x] Task 27: 实现提醒系统
  - [x] SubTask 27.1: 实现提醒调度（AlarmManager + WorkManager）
  - [x] SubTask 27.2: 实现提醒通知（快捷操作：已完成/稍后提醒）
  - [x] SubTask 27.3: 实现免打扰时段（全局/单个打卡）
  - [x] SubTask 27.4: 实现智能提醒（根据用户习惯预测）

## 阶段十二：动画与交互优化

- [x] Task 28: 完善动画系统
  - [x] SubTask 28.1: 实现页面切换动画（底部导航/详情进入/返回/全屏弹窗）
  - [x] SubTask 28.2: 实现组件交互动画（按钮/卡片/开关/进度条）
  - [x] SubTask 28.3: 实现列表动画（加载瀑布式/添加删除/下拉刷新液态水滴）
  - [x] SubTask 28.4: 实现手势交互（滑动跟手/缩放/长按/侧滑返回）
  - [x] SubTask 28.5: 实现触觉反馈分级

## 阶段十三：测试与交付

- [x] Task 29: 编写测试
  - [x] SubTask 29.1: 编写单元测试（Repository/UseCase/工具类，覆盖率 > 80%）
  - [x] SubTask 29.2: 编写 Compose UI 测试（主要流程/边界情况）
  - [x] SubTask 29.3: 编写性能测试（启动时间/内存/帧率）

- [x] Task 30: 构建与优化
  - [x] SubTask 30.1: 配置 Release 签名
  - [x] SubTask 30.2: 启用 R8/ProGuard 混淆与压缩
  - [x] SubTask 30.3: 生成 Release APK
  - [x] SubTask 30.4: 集成 LeakCanary（debug 构建）
  - [x] SubTask 30.5: 性能优化（冷启动 <1s、60fps、内存优化）

- [x] Task 31: 编写文档
  - [x] SubTask 31.1: 编写项目说明文档（README）
  - [x] SubTask 31.2: 编写架构设计文档
  - [x] SubTask 31.3: 编写 API 接口文档（AI Provider 扩展指南）
  - [x] SubTask 31.4: 编写部署运行说明
  - [x] SubTask 31.5: 编写测试报告

## 阶段十四：检查点修复（基于 checklist 验证结果）

- [x] Task 32: 数据隐私与安全补全
  - [x] SubTask 32.1: 实现应用锁（密码/指纹/面部识别、自动锁定时间）
  - [x] SubTask 32.2: 实现最近任务列表隐藏内容、截图禁用、敏感数据模糊显示
  - [x] SubTask 32.3: 实现 SQLCipher 数据库加密
  - [x] SubTask 32.4: 实现自动备份（每日/每周/每月，保留 N 个版本）
  - [x] SubTask 32.5: 实现 PDF 导出
  - [x] SubTask 32.6: 实现网络请求日志查看、网络权限禁用选项

- [x] Task 33: AI 分析功能补全
  - [x] SubTask 33.1: 实现饮水/运动/睡眠/习惯养成分析 UseCase 与 UI
  - [x] SubTask 33.2: 实现对话历史本地保存（可删除）
  - [x] SubTask 33.3: 实现每个 AI 功能独立开关、仅 WiFi 下使用
  - [x] SubTask 33.4: 实现 Token 优化（提示词精简/图片压缩/结果缓存/批量处理/智能截断）

- [ ] Task 34: 液态玻璃与动画效果补全
  - [ ] SubTask 34.1: 实现玻璃折射效果、背景噪点纹理、水波纹扩散
  - [ ] SubTask 34.2: 实现低性能设备降级方案（检测 + 自动降级）
  - [ ] SubTask 34.3: 实现视差效果、卡片悬停、玻璃折射偏移
  - [ ] SubTask 34.4: 实现骨架屏微光流动、液态加载动画（水滴旋转/液体流动）

- [x] Task 35: 时间视图与交互补全
  - [x] SubTask 35.1: 实现双指缩放切换视图粒度（平滑连续）
  - [x] SubTask 35.2: 实现点击进入下一级视图（400ms 动画）、返回上一级（350ms 回弹）
  - [x] SubTask 35.3: 实现时间轴当前时间红色标记

- [ ] Task 36: 多模型 API 管理补全
  - [ ] SubTask 36.1: 实现 API Key 密码模式输入（显示/隐藏）
  - [ ] SubTask 36.2: 实现 HTTP 代理支持
  - [ ] SubTask 36.3: 实现协同效果对比（节省 Token 比例）
  - [ ] SubTask 36.4: 实现 API 性能监控（响应时间/成功率/质量评分）

- [ ] Task 37: 无障碍与兼容性补全
  - [ ] SubTask 37.1: 为所有可交互元素添加内容描述、图片文字说明、图标按钮语义
  - [ ] SubTask 37.2: 实现高对比度模式、超大字体布局适配
  - [ ] SubTask 37.3: 实现键盘导航、TalkBack 屏幕阅读器支持
  - [ ] SubTask 37.4: 实现平板双栏布局适配

- [ ] Task 38: UI 设计系统与错误处理补全
  - [ ] SubTask 38.1: 集成自定义字体（Noto Sans SC、Inter、JetBrains Mono）
  - [ ] SubTask 38.2: 实现主题切换 600ms 平滑过渡动画
  - [ ] SubTask 38.3: 实现日志文件大小限制、用户可开关日志、日志导出
  - [ ] SubTask 38.4: 实现可选的崩溃收集（本地保存，用户可关闭）

# Task Dependencies

- Task 2 依赖 Task 1（项目结构）
- Task 4、5 依赖 Task 3（数据模型）
- Task 6 依赖 Task 4、5
- Task 7 依赖 Task 6
- Task 8 依赖 Task 7
- Task 9、10、11 可并行（UI 基础）
- Task 12 依赖 Task 10、11
- Task 13 依赖 Task 12
- Task 14 依赖 Task 13、8
- Task 15、16 依赖 Task 14
- Task 17、18 依赖 Task 14
- Task 19、20、21 依赖 Task 8、13
- Task 22 依赖 Task 7、8
- Task 23、24 依赖 Task 22
- Task 25、26、27 依赖 Task 7、8
- Task 28 贯穿所有 UI 任务
- Task 29、30、31 依赖所有功能任务完成

# 可并行任务

- Task 9、10、11（UI 基础组件）
- Task 15 与 Task 17（创建与详情，独立页面）
- Task 20 各子任务（不同时间视图）
- Task 22 各 Provider 实现
- Task 25、26、27（设置/备份/提醒，独立模块）
