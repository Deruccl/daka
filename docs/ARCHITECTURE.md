# 架构设计文档

> 本文档描述「时光印记 TimeMark」Android 应用的整体架构设计、模块划分、数据流、依赖注入、数据库设计、安全设计、AI 架构、性能设计与离线架构。

## 目录

- [架构概览](#架构概览)
- [模块依赖关系](#模块依赖关系)
- [各层职责说明](#各层职责说明)
- [数据流](#数据流)
- [依赖注入](#依赖注入)
- [导航架构](#导航架构)
- [数据库设计](#数据库设计)
- [安全设计](#安全设计)
- [AI 架构](#ai-架构)
- [性能设计](#性能设计)
- [离线架构](#离线架构)

---

## 架构概览

时光印记采用 **Clean Architecture + MVVM** 分层架构，结合 **多模块** 工程组织，实现关注点分离、可测试性、可维护性。

### 分层架构图

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation 层                       │
│   (Jetpack Compose UI + ViewModel + Navigation)         │
│                                                         │
│  feature-home  feature-tracker  feature-stats           │
│  feature-ai    feature-settings                         │
└────────────────────────┬────────────────────────────────┘
                         │ 依赖
┌────────────────────────▼────────────────────────────────┐
│                      Domain 层                          │
│   (UseCase + Repository 接口 + 数据模型)                 │
│                                                         │
│   model/    repository/    (UseCase 在各 feature 内)     │
└────────────────────────┬────────────────────────────────┘
                         │ 依赖
┌────────────────────────▼────────────────────────────────┐
│                       Data 层                           │
│   (Repository 实现 + Room + DataStore + Mapper)         │
│                                                         │
│   db/    datastore/    repository/    mapper/           │
│   security/    util/                                    │
└────────────────────────┬────────────────────────────────┘
                         │ 依赖
┌────────────────────────▼────────────────────────────────┐
│                      AI 层                              │
│   (AIProvider + AIService + 协同服务)                    │
│                                                         │
│   provider/    AIServiceImpl.kt    CollaborativeService │
└─────────────────────────────────────────────────────────┘
                         │ 依赖
┌────────────────────────▼────────────────────────────────┐
│                     Core 层                             │
│   (通用工具 + 扩展 + UI 组件 + 动画)                      │
│                                                         │
│   utils/    extensions/    ui/                          │
└─────────────────────────────────────────────────────────┘
```

### 设计原则

1. **单向依赖**：上层依赖下层，下层不依赖上层。Domain 层不依赖 Data 层实现。
2. **依赖倒置**：Domain 层定义 Repository 接口，Data 层提供实现，通过 Hilt 注入。
3. **单一职责**：每个模块、类、函数只做一件事。
4. **可测试性**：UseCase、Repository、Provider 均可独立单元测试。
5. **响应式**：使用 Kotlin Flow/StateFlow 实现响应式数据流。

---

## 模块依赖关系

### 模块依赖图

```
                    ┌─────────┐
                    │   app   │
                    └────┬────┘
        ┌──────────┬─────┼──────┬──────────┐
        ▼          ▼     ▼      ▼          ▼
  ┌──────────┐ ┌─────────┐ ┌────────┐ ┌──────────┐
  │feature-  │ │feature- │ │feature-│ │feature-  │
  │  home    │ │tracker  │ │ stats  │ │   ai     │
  └────┬─────┘ └────┬────┘ └───┬────┘ └────┬─────┘
       │            │          │           │
       └────────────┴────┬─────┴───────────┘
                         ▼
                    ┌─────────┐
                    │ domain  │
                    └────┬────┘
                         ▼
                    ┌─────────┐    ┌─────┐
                    │  data   │───▶│ ai  │
                    └────┬────┘    └──┬──┘
                         │            │
                         └─────┬──────┘
                               ▼
                          ┌─────────┐
                          │  core   │
                          └─────────┘
```

### 模块依赖详情

| 模块 | 依赖 | 被依赖 |
|------|------|--------|
| `core` | 无 | 所有模块 |
| `domain` | `core` | `data`, `ai`, `app`, 所有 feature |
| `data` | `core`, `domain` | `app` |
| `ai` | `core`, `domain` | `app` |
| `feature-home` | `core`, `domain` | `app` |
| `feature-tracker` | `core`, `domain` | `app` |
| `feature-stats` | `core`, `domain` | `app` |
| `feature-ai` | `core`, `domain` | `app` |
| `feature-settings` | `core`, `domain` | `app` |
| `app` | 所有模块 | 无 |

### 依赖规则

- **Domain 层零外部依赖**：`domain` 模块仅依赖 `core`，不依赖 Android Framework、Room、Hilt 等。
- **Data 层依赖 Domain**：`data` 模块实现 `domain` 定义的 Repository 接口。
- **Feature 层依赖 Domain**：各 feature 模块通过 UseCase 访问业务逻辑，不直接访问 Data 层。
- **App 层聚合所有**：`app` 模块负责 DI 装配、导航、Application 初始化。

---

## 各层职责说明

### Presentation 层（表现层）

**职责**：UI 展示、用户交互、状态管理。

**组成**：

- **Composable 函数**：声明式 UI，使用 Jetpack Compose + Material 3。
- **ViewModel**：持有 UI 状态（StateFlow/SharedFlow），调用 UseCase，不持有 View 引用。
- **Navigation**：Navigation Compose 管理页面路由与转场动画。

**示例**：

```kotlin
// HomeViewModel.kt
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrackersUseCase: GetTrackersUseCase,
    private val getRecordsUseCase: GetRecordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayData()
    }

    fun loadTodayData(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            getTrackersUseCase().collect { trackers ->
                _uiState.value = HomeUiState.Success(trackers)
            }
        }
    }
}
```

### Domain 层（领域层）

**职责**：业务逻辑封装，定义数据模型与 Repository 接口。

**组成**：

- **Model**：领域数据模型（Tracker、Record、AIConfig 等），纯 Kotlin 数据类。
- **Repository 接口**：定义数据访问契约，由 Data 层实现。
- **UseCase**：封装单一业务用例，组合多个 Repository。

**示例**：

```kotlin
// domain/repository/TrackerRepository.kt
interface TrackerRepository {
    fun getAllTrackers(): Flow<List<Tracker>>
    fun getTrackerById(id: Long): Flow<Tracker?>
    suspend fun insertTracker(tracker: Tracker): Long
    suspend fun updateTracker(tracker: Tracker)
    suspend fun deleteTracker(id: Long)
}

// domain/model/Tracker.kt
data class Tracker(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: String,
    val type: TrackerType,
    val unit: String,
    val targetValue: Double,
    // ...
)
```

### Data 层（数据层）

**职责**：数据持久化、数据源管理、数据转换。

**组成**：

- **Entity**：Room 数据库表结构。
- **DAO**：数据库访问对象，SQL 查询。
- **Repository 实现**：实现 Domain 层接口，协调 DAO、DataStore、Mapper。
- **Mapper**：Entity ↔ Domain 模型转换。
- **DataStore**：偏好设置存储。
- **Security**：Keystore 加密。

**示例**：

```kotlin
// data/repository/TrackerRepositoryImpl.kt
class TrackerRepositoryImpl @Inject constructor(
    private val trackerDao: TrackerDao
) : TrackerRepository {

    override fun getAllTrackers(): Flow<List<Tracker>> =
        trackerDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertTracker(tracker: Tracker): Long =
        trackerDao.insert(tracker.toEntity())
}
```

---

## 数据流

### 单向数据流（UDF）

时光印记遵循 **单向数据流** 模式：UI 事件 → ViewModel → UseCase → Repository → 数据库 → Flow → ViewModel → UI。

```
┌──────────┐  用户事件   ┌────────────┐  调用   ┌──────────┐
│   UI     │────────────▶│ ViewModel  │────────▶│ UseCase  │
│(Composable)│           │(StateFlow) │         │          │
└──────────┘             └────────────┘         └──────────┘
     ▲                        │                      │
     │ UI 状态                 │ 调用                  │ 调用
     │                        ▼                      ▼
     │                   ┌──────────┐          ┌────────────┐
     │                   │ StateFlow│◀─────────│ Repository │
     │                   │          │  Flow    │   (Impl)   │
     └───────────────────┴──────────┘          └────────────┘
                                                     │
                                                     │ SQL
                                                     ▼
                                               ┌──────────┐
                                               │   Room   │
                                               │ Database │
                                               └──────────┘
```

### StateFlow 与 SharedFlow

| 类型 | 用途 | 示例 |
|------|------|------|
| `StateFlow` | UI 状态持有，始终有值，conflation | `HomeUiState` |
| `SharedFlow` | 一次性事件（导航、Toast），无 conflation | 显示提示消息 |
| `Flow` | 数据库响应式查询，冷流 | `trackerDao.getAll()` |

**示例**：

```kotlin
// UI 状态
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val trackers: List<Tracker>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

// ViewModel
private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

// 一次性事件
private val _events = MutableSharedFlow<HomeEvent>()
val events: SharedFlow<HomeEvent> = _events.asSharedFlow()
```

---

## 依赖注入

### Hilt Module 组织

时光印记使用 **Hilt** 进行依赖注入，共 5 个 Module，全部位于 `app/di/` 目录。

| Module | 类型 | 职责 |
|--------|------|------|
| `DatabaseModule` | object | 提供 Room 数据库与 5 个 DAO |
| `DataStoreModule` | object | 提供 SettingsDataStore |
| `RepositoryModule` | abstract | 绑定 6 个 Repository 接口与实现 |
| `NetworkModule` | object | 提供 OkHttpClient、Json |
| `AIModule` | abstract | 绑定 AIService 接口与 AIServiceImpl |

### Module 详情

#### DatabaseModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTimeMarkDatabase(@ApplicationContext context: Context): TimeMarkDatabase =
        Room.databaseBuilder(context, TimeMarkDatabase::class.java, TimeMarkDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTrackerDao(db: TimeMarkDatabase): TrackerDao = db.trackerDao()
    // ... 其他 DAO
}
```

#### RepositoryModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTrackerRepository(impl: TrackerRepositoryImpl): TrackerRepository

    @Binds
    @Singleton
    abstract fun bindRecordRepository(impl: RecordRepositoryImpl): RecordRepository
    // ... 其他 Repository
}
```

#### AIModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class AIModule {

    @Binds
    @Singleton
    abstract fun bindAIService(impl: AIServiceImpl): AIService
}
```

### 注入范围

- **SingletonComponent**：所有 Module 安装在 Application 级别，单例生命周期。
- **ViewModelScoped**：ViewModel 通过 `@HiltViewModel` 自动管理生命周期。

---

## 导航架构

### Route 定义

所有路由集中定义在 `Route.kt`，避免硬编码字符串。

```kotlin
sealed class Route(val route: String) {
    object Home : Route("home")
    object Stats : Route("stats")
    object AI : Route("ai")
    object Settings : Route("settings")
    object CreateTracker : Route("create_tracker")
    object EditTracker : Route("edit_tracker/{trackerId}") {
        fun createRoute(trackerId: Long) = "edit_tracker/$trackerId"
    }
    object TrackerDetail : Route("tracker_detail/{trackerId}") {
        fun createRoute(trackerId: Long) = "tracker_detail/$trackerId"
    }
    object AIConfig : Route("ai_config")
    object AIConfigAdd : Route("ai_config_add")
    object AIConfigEdit : Route("ai_config_edit/{configId}") {
        fun createRoute(configId: Long) = "ai_config_edit/$configId"
    }
    object AIChat : Route("ai_chat")
    object FoodRecognition : Route("food_recognition")
    object TokenUsage : Route("token_usage")
    object BackupRestore : Route("backup_restore")
    object About : Route("about")
}
```

### NavHost 配置

`TimeMarkNavHost` 集中注册所有页面路由与 Composable，并配置页面切换动画。

```kotlin
@Composable
fun TimeMarkNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
        modifier = modifier,
        enterTransition = { enterTransitionForRoute(this) },
        exitTransition = { exitTransitionForRoute(this) },
        popEnterTransition = { popEnterTransitionForRoute(this) },
        popExitTransition = { popExitTransitionForRoute(this) }
    ) {
        composable(Route.Home.route) { HomeScreen(navController) }
        composable(Route.Stats.route) { StatsScreen(navController) }
        // ... 其他路由
    }
}
```

### 导航结构图

```
┌─────────────────────────────────────────────┐
│              ScaffoldMain                    │
│  ┌───────────────────────────────────────┐  │
│  │            TopBar                      │  │
│  ├───────────────────────────────────────┤  │
│  │                                       │  │
│  │           NavHost 内容区               │  │
│  │                                       │  │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐│  │
│  │  │ Home │ │Stats │ │  AI  │ │Setting│  │
│  │  └──────┘ └──────┘ └──────┘ └──────┘│  │
│  │                                       │  │
│  ├───────────────────────────────────────┤  │
│  │           BottomBar                   │  │
│  │  [首页] [统计] [AI] [设置]              │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

### 页面切换动画

| 路由类型 | 动画 | 时长 |
|----------|------|------|
| 底部导航（Home/Stats/AI/Settings） | 淡入淡出 + 轻微位移 | 250ms |
| 详情页（TrackerDetail/EditTracker） | 从右侧滑入 | 350ms EaseOutCubic |
| 全屏弹窗（AIChat/FoodRecognition） | 从底部滑入 + 缩放 | 300ms EaseOutBack |

---

## 数据库设计

### ER 图

```
┌──────────────────┐       ┌──────────────────┐
│   trackers       │       │     records      │
├──────────────────┤       ├──────────────────┤
│ id (PK)          │◀──────│ tracker_id (FK)  │
│ name             │  1:N  │ id (PK)          │
│ icon             │       │ value            │
│ color            │       │ date             │
│ type             │       │ time             │
│ unit             │       │ timestamp        │
│ target_value     │       │ note             │
│ description      │       │ images           │
│ time_period      │       │ tags             │
│ is_visible       │       │ mood             │
│ sort_order       │       │ duration         │
│ ai_enabled       │       │ created_at       │
│ reminder_*       │       │ updated_at       │
│ created_at       │       └──────────────────┘
│ updated_at       │
└──────────────────┘
        │
        │ 1:N
        ▼
┌──────────────────┐       ┌──────────────────┐
│   daily_stats    │       │    ai_configs    │
├──────────────────┤       ├──────────────────┤
│ id (PK)          │       │ id (PK)          │
│ tracker_id (FK)  │       │ name             │
│ date             │       │ provider         │
│ total_value      │       │ api_key (加密)    │
│ count            │       │ base_url         │
│ completed        │       │ model            │
│ extra            │       │ model_type       │
└──────────────────┘       │ price_input      │
                           │ price_output     │
                           │ enabled          │
                           │ priority         │
                           │ applicable_features│
                           └────────┬─────────┘
                                    │
                                    │ 1:N
                                    ▼
                           ┌──────────────────┐
                           │    ai_usage      │
                           ├──────────────────┤
                           │ id (PK)          │
                           │ config_id        │
                           │ feature          │
                           │ tokens_input     │
                           │ tokens_output    │
                           │ cost             │
                           │ timestamp        │
                           │ success          │
                           │ error_message    │
                           │ response_time_ms │
                           └──────────────────┘
```

### Entity 关系

| 关系 | 说明 |
|------|------|
| `trackers` 1:N `records` | 一个打卡项有多条记录，删除打卡项级联删除记录 |
| `trackers` 1:N `daily_stats` | 一个打卡项每天一条统计，(tracker_id, date) 唯一 |
| `ai_configs` 1:N `ai_usage` | 一个 AI 配置有多条使用记录（逻辑关联，无外键） |

### 索引设计

| 表 | 索引 | 用途 |
|----|------|------|
| `trackers` | `sort_order` | 按排序字段查询 |
| `records` | `tracker_id` | 按打卡项查询记录 |
| `records` | `date` | 按日期查询记录 |
| `records` | `timestamp` | 按时间戳排序 |
| `records` | `(tracker_id, date)` | 复合索引，按打卡项+日期查询 |
| `daily_stats` | `(tracker_id, date)` UNIQUE | 唯一约束，确保每项每天一条 |
| `ai_configs` | `priority` | 按优先级排序 |
| `ai_usage` | `config_id` | 按 AI 配置查询用量 |
| `ai_usage` | `timestamp` | 按时间查询用量 |
| `ai_usage` | `feature` | 按功能查询用量 |

### 数据库版本

- **当前版本**：1
- **迁移策略**：`fallbackToDestructiveMigration()`（开发阶段，正式版将改为增量迁移）
- **数据库名**：`timemark.db`

---

## 安全设计

### Keystore 加密

使用 Android Keystore 系统加密敏感数据（API Key、密码）。

**算法**：AES/GCM/NoPadding，256 位密钥

**流程**：

```
┌────────────┐    encrypt     ┌──────────────────┐
│  明文 API  │───────────────▶│ Base64(IV+密文+Tag)│
│   Key      │                └──────────────────┘
└────────────┘                         │
                                       │ 存储
                                       ▼
                              ┌──────────────────┐
                              │  Room Database   │
                              │  (ai_configs)    │
                              └──────────────────┘
                                       │
                                       │ 读取
                                       ▼
┌────────────┐    decrypt     ┌──────────────────┐
│  明文 API  │◀───────────────│ Base64(IV+密文+Tag)│
│   Key      │                └──────────────────┘
└────────────┘
```

**密钥特性**：

- 密钥别名：`timemark_master_key`
- 存储位置：AndroidKeystore（硬件支持时使用 TEE/StrongBox）
- 密钥不导出，不可提取
- API 23+（minSdk 26 满足）

### 应用锁

| 方式 | 说明 |
|------|------|
| 密码 | 用户设置数字/字母密码，本地加密存储 |
| 生物识别 | 指纹/面部识别，使用 BiometricPrompt |
| 无 | 关闭应用锁 |

### 数据本地化

- **无网络权限请求**：核心功能完全离线，不发送任何数据到服务器。
- **AI 功能可选**：仅在用户主动配置 AI Provider 时使用网络，且仅与用户配置的 AI 服务通信。
- **网络配置**：`network_security_config.xml` 限制明文流量，强制 HTTPS。

---

## AI 架构

### Provider 模式

采用 **Provider 模式** 统一不同 AI 厂商的接口差异。

```
┌─────────────────────────────────────────────┐
│              AIService 接口                  │
│  (chat / recognizeImage / testConnection)   │
└───────────────────┬─────────────────────────┘
                    │ 实现
                    ▼
┌─────────────────────────────────────────────┐
│            AIServiceImpl                    │
│  - 维护 Provider 映射                        │
│  - 检查全局开关与预算                         │
│  - 调用对应 Provider                         │
│  - 记录 Token 用量                           │
│  - 故障转移                                  │
└───────────────────┬─────────────────────────┘
                    │ 路由
                    ▼
┌─────────────────────────────────────────────┐
│              AIProvider 接口                 │
│  (chat / recognizeImage / testConnection)   │
└──┬─────┬─────┬─────┬─────┬─────┬─────┬─────┘
   │     │     │     │     │     │     │
   ▼     ▼     ▼     ▼     ▼     ▼     ▼
OpenAI Anthropic Gemini Baidu Alibaba ... Custom
```

### 路由策略

1. **功能路由**：根据 AIFeature 选择支持该功能的 AI 配置。
2. **优先级路由**：多个配置支持同一功能时，按 `priority` 升序选择。
3. **故障转移**：主配置失败时，依次尝试备用配置。

```kotlin
suspend fun routeRequest(feature: AIFeature, request: ChatRequest): ChatResponse {
    val configs = aiConfigRepository.getConfigsByFeature(feature).first()
    if (configs.isEmpty()) {
        // 回退到默认配置
        val fallback = when (feature) {
            AIFeature.FOOD_RECOGNITION -> aiConfigRepository.getDefaultMultimodalConfig().first()
            else -> aiConfigRepository.getDefaultTextConfig().first()
        }
        return fallback?.let { chat(request, it) } ?: failureResponse(...)
    }
    // 故障转移
    val sorted = configs.sortedBy { it.priority }
    return executeWithFallback(request, sorted.first(), sorted.drop(1))
}
```

### 协同模式

`CollaborativeService` 支持多模型协同：

- **串行协同**：模型 A 的输出作为模型 B 的输入。
- **并行协同**：多模型同时处理，取最优结果。
- **投票协同**：多模型投票决策。

### 预算控制

| 限制项 | 说明 |
|--------|------|
| 每日 Token 上限 | `dailyTokenLimit`，超过则拒绝请求 |
| 每月费用上限 | `monthlyBudgetLimit`，超过则拒绝请求 |
| 全局开关 | `aiGlobalEnabled`，一键禁用所有 AI 功能 |

---

## 性能设计

### 动画 60fps

- **Compose 动画**：使用 `AnimatedVisibility`、`animate*AsState`、`Animatable` 等 API。
- **硬件加速**：默认开启硬件层加速。
- **避免过度绘制**：合理使用 `Modifier.background`，减少透明叠加。
- **Baseline Profile**：`baseline-prof.txt` 预编译热点路径，提升冷启动速度。

### 数据库优化

- **索引优化**：为高频查询字段建立索引（见[索引设计](#索引设计)）。
- **Flow 响应式查询**：避免轮询，数据变更自动推送。
- **分页查询**：大量数据使用 `Limit/Offset` 分页。
- **事务批处理**：批量操作使用 `@Transaction`。

### 内存管理

- **LeakCanary**：Debug 版本自动检测内存泄漏。
- **Compose 稳定性**：使用 `@Stable`、`@Immutable` 注解优化重组。
- **图片加载**：Coil 自动管理图片缓存与内存。
- **ViewModel 生命周期**：数据与 ViewModel 绑定，避免 Activity 泄漏。

### APK 体积优化

- **R8/ProGuard**：代码混淆、压缩、优化。
- **资源压缩**：`isShrinkResources = true` 移除无用资源。
- **PNG 压缩**：`isCrunchPngs = true`。
- **ABI 分包**：按 `armeabi-v7a`、`arm64-v8a`、`x86_64` 拆分，减少单包体积。

---

## 离线架构

### 数据本地化

```
┌─────────────────────────────────────────┐
│              应用层                      │
│   (完全离线，无网络请求)                 │
└──────────────────┬──────────────────────┘
                   │
        ┌──────────┴──────────┐
        ▼                     ▼
┌──────────────┐      ┌──────────────┐
│ Room DB      │      │  DataStore   │
│ (结构化数据)  │      │ (偏好设置)    │
│              │      │              │
│ - trackers   │      │ - 主题       │
│ - records    │      │ - 动画开关    │
│ - daily_stats│      │ - 应用锁     │
│ - ai_configs │      │ - AI 设置    │
│ - ai_usage   │      │              │
└──────────────┘      └──────────────┘
        │
        │ Keystore 加密
        ▼
┌──────────────┐
│ AndroidKeystore│
│ (密钥存储)    │
└──────────────┘
```

### 备份恢复

| 功能 | 格式 | 说明 |
|------|------|------|
| 完整备份 | ZIP | 包含数据库文件 + DataStore + 配置 |
| 数据导出 | CSV | 打卡记录导出，便于表格分析 |
| 数据导出 | JSON | 完整数据导出，便于迁移 |

**备份流程**：

```
用户点击备份
    │
    ▼
打包数据库 + DataStore → ZIP 文件
    │
    ▼
用户选择保存位置（SAF）
    │
    ▼
写入文件
```

**恢复流程**：

```
用户选择 ZIP 文件
    │
    ▼
解压并校验格式
    │
    ▼
关闭数据库连接
    │
    ▼
替换数据库 + DataStore 文件
    │
    ▼
重新初始化
```

---

## 相关文档

- [README](../README.md) - 项目说明
- [AI Provider 指南](AI_PROVIDER_GUIDE.md) - AI 厂商扩展指南
- [部署运行说明](DEPLOYMENT.md) - 构建与部署
- [测试报告](TEST_REPORT.md) - 测试结果
