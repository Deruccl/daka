# 「时光印记」Android 打卡应用 Spec

## Why

用户需要一款完全离线运行、隐私优先、高度自定义的 Android 原生打卡应用。所有数据 100% 存储在本地，AI 功能为可选增强（用户自带 API Key）。本规范基于 `Android打卡APP_AI开发提示词.md` 文档完整定义应用的功能、技术架构、UI 设计、性能与验收标准。

> 注：用户原始输入中提到的"用户注册与登录、位置获取、数据同步"与文档核心定位冲突。文档明确要求**无账号系统、无云端同步、完全离线**，本规范以文档为准。位置仅在打卡记录中可选附加（用户主动选择），不作为核心功能。

## What Changes

### 核心架构
- 新建 Android 原生项目，100% Kotlin + Jetpack Compose + Material 3
- 采用 Clean Architecture + MVVM 分层架构
- 模块化设计：app / core / data / domain / ai / feature-* 多模块
- 最低 SDK 26（Android 8.0），目标 SDK 34
- Hilt 依赖注入、Room 数据库、DataStore 偏好存储、Coroutines + Flow 异步

### 核心功能模块
- 自定义打卡系统：6 种类型（计数/时长/数值/勾选/图文/计时）
- 预设打卡模板库（健康/学习/生活/工作四大类）
- 打卡创建/编辑/删除分步引导流程
- 打卡记录管理（添加/编辑/删除/批量操作）
- 打卡提醒系统（固定时间/间隔/智能提醒）
- 多维度时间视图（分钟/小时/日/周/月/年，支持双指缩放切换）
- 数据统计与图表（环形进度/折线/柱状/热力图/饼图）

### AI 智能分析系统（可选增强）
- 多厂商 API 集成（OpenAI/Anthropic/Google/百度/阿里/字节/智谱/Moonshot/本地 Ollama/自定义）
- 多模态与非多模态协同架构（节省 Token）
- 食物识别、营养分析、饮水/运动/睡眠分析、习惯养成建议
- AI 聊天助手（自然语言查询/记录/建议）
- Token 消耗统计与预算控制
- API Key 使用 Android Keystore 加密存储

### UI/UX 设计体系
- 液态玻璃（Liquid Glass）视觉效果（轻薄/标准/厚重三级）
- 新拟态 + 液态玻璃融合风格
- 完整色彩系统（主色 #6366F1 靛蓝、次色 #EC4899 粉红、第三色 #10B981 翠绿）
- 字体层级（Display/Headline/Title/Body/Label）
- 8 点栅格间距系统
- 浅色/深色/跟随系统三种主题
- Android 12+ 动态颜色（Material You）支持

### 动画与交互
- 物理真实感动画（弹簧曲线、EaseInOutCubic）
- 页面切换、组件交互、列表动画、液态玻璃动效
- 手势交互（滑动/缩放/长按/侧滑返回）
- 触觉反馈分级
- 庆祝动画（完成打卡/达成目标/里程碑）

### 数据存储与隐私安全
- Room 数据库（Trackers/Records/DailyStats/AIConfigs/AIUsage 表）
- 可选 SQLCipher 数据库加密
- 应用锁（密码/指纹/面部识别）
- 数据备份与恢复（.zip 完整/仅数据）
- 数据导出（CSV/JSON/PDF）
- 所有数据本地存储，无任何上传

### 性能指标
- 冷启动 < 1 秒，热启动 < 300ms
- 列表滚动 60fps，动画 60fps（高端设备 120fps）
- 核心逻辑单元测试覆盖率 > 80%
- LeakCanary 内存泄漏检测
- Android 8.0+ 兼容性、屏幕适配、厂商适配

### 交付物
- 完整 Android 项目源码（Kotlin + Compose）
- 可运行的 Release 签名 APK
- 项目文档（说明/架构/API/部署）
- 设计资源（规范/图标/颜色字体定义）

## Impact

- Affected specs: 全新项目，无既有 spec 受影响
- Affected code: 全新代码库，主要模块包括：
  - `app/` - 主应用入口、Application 类、MainActivity
  - `core/` - 通用工具、扩展函数、通用组件、主题系统
  - `data/` - Room 数据库、DAO、Repository 实现、DataStore
  - `domain/` - 数据模型、Repository 接口、UseCase
  - `ai/` - AI API 封装、多模型管理、Token 统计
  - `feature-home/` - 首页打卡列表
  - `feature-tracker/` - 打卡创建/编辑/详情
  - `feature-stats/` - 统计与时间视图
  - `feature-ai/` - AI 功能页面
  - `feature-settings/` - 设置页面

## ADDED Requirements

### Requirement: 项目基础架构
系统 SHALL 使用 Kotlin + Jetpack Compose + Material 3 构建，采用 Clean Architecture + MVVM 分层，模块化设计，最低支持 Android 8.0（API 26）。

#### Scenario: 项目构建成功
- **WHEN** 执行 `./gradlew assembleRelease`
- **THEN** 成功生成签名 APK，无编译错误

#### Scenario: 模块化结构
- **WHEN** 查看项目结构
- **THEN** 存在 app/core/data/domain/ai/feature-* 等独立模块，依赖关系清晰

### Requirement: 自定义打卡系统
系统 SHALL 支持创建 6 种类型的打卡项目（计数/时长/数值/勾选/图文/计时），每种类型有对应的记录方式和统计逻辑。

#### Scenario: 创建计数型打卡
- **WHEN** 用户选择"计数型"类型，填写名称"喝水"、单位"杯"、目标值"8"
- **THEN** 成功创建打卡项目，首页显示该卡片，可点击 +1 记录

#### Scenario: 创建图文型打卡
- **WHEN** 用户选择"图文型"，填写名称"饮食记录"
- **THEN** 创建成功，记录时可上传图片和文字

#### Scenario: 使用预设模板
- **WHEN** 用户在创建页选择"每日饮水"模板
- **THEN** 自动填充模板配置，用户可修改后确认创建

### Requirement: 打卡记录管理
系统 SHALL 提供打卡记录的添加、编辑、删除功能，支持单条删除（5 秒内可撤销）和批量删除。

#### Scenario: 添加记录
- **WHEN** 用户在打卡详情页点击添加按钮，输入数值和时间
- **THEN** 记录保存成功，详情页和首页进度实时更新

#### Scenario: 删除记录可撤销
- **WHEN** 用户删除一条记录后 5 秒内点击"撤销"
- **THEN** 记录恢复，数据不变

### Requirement: 多维度时间视图
系统 SHALL 提供分钟/小时/日/周/月/年六级时间视图，支持双指缩放、点击进入、左右滑动切换，视图切换有平滑动画。

#### Scenario: 双指缩放切换视图
- **WHEN** 用户在日视图双指捏合缩小
- **THEN** 平滑过渡到周视图，保持视觉连续性

#### Scenario: 左右滑动切换日期
- **WHEN** 用户在日视图左右滑动
- **THEN** 切换到上一天/下一天，有视差效果

### Requirement: AI 智能分析（可选）
系统 SHALL 提供可选的 AI 功能，用户配置自己的 API Key 后可使用食物识别、营养分析、习惯建议、AI 聊天等功能；未配置时应用完整可用。

#### Scenario: 未配置 AI 时正常使用
- **WHEN** 用户未配置任何 AI API
- **THEN** 应用所有基础功能正常，仅 AI 入口显示"未配置"提示

#### Scenario: 多模态食物识别
- **WHEN** 用户在饮食打卡上传食物照片，已配置多模态模型
- **THEN** AI 返回结构化食物列表（名称/份量/热量/营养），用户确认后保存

#### Scenario: 多模态协同节省 Token
- **WHEN** 启用协同模式，进行食物识别+营养分析
- **THEN** 多模态模型仅做识别输出结构化文字，非多模态模型做分析，Token 消耗显著低于全程多模态

### Requirement: 多模型 API 管理
系统 SHALL 支持配置多个 AI 厂商模型，支持智能路由、故障转移、Token 统计与预算控制。

#### Scenario: 添加并测试模型
- **WHEN** 用户填写 API Key 和 Base URL，点击测试
- **THEN** 显示连接结果（成功/失败、延迟、Token 数），成功后可保存

#### Scenario: 故障自动转移
- **WHEN** 主模型请求失败
- **THEN** 自动尝试备用模型，记录失败原因

#### Scenario: 预算上限保护
- **WHEN** Token 消耗达到设置的上限
- **THEN** 提醒用户，可配置自动停止 AI 功能

### Requirement: 液态玻璃 UI 设计
系统 SHALL 实现液态玻璃视觉效果，包含半透明、模糊、折射、高光、边框、阴影六要素，分轻薄/标准/厚重三级，低性能设备自动降级。

#### Scenario: 标准玻璃卡片显示
- **WHEN** 渲染主要内容卡片
- **THEN** 80% 不透明度、20dp 模糊半径、顶部高光、1dp 渐变边框、柔和阴影

#### Scenario: 低性能设备降级
- **WHEN** 设备不支持硬件模糊
- **THEN** 使用半透明纯色替代，保留高光和边框，视觉风格一致

### Requirement: 数据存储与隐私
系统 SHALL 将所有数据 100% 存储在本地，无账号系统、无云端同步、无数据收集；API Key 使用 Android Keystore 加密。

#### Scenario: 数据完全本地化
- **WHEN** 用户使用应用所有功能
- **THEN** 除用户主动触发的 AI 请求外，无任何网络请求

#### Scenario: API Key 加密存储
- **WHEN** 用户保存 API Key
- **THEN** 使用 Android Keystore 加密存储，不明文显示、不上传、卸载时删除

### Requirement: 数据备份与导出
系统 SHALL 支持完整备份（.zip 含数据库+图片）、仅数据备份、从备份恢复、导出 CSV/JSON/PDF。

#### Scenario: 完整备份与恢复
- **WHEN** 用户创建完整备份，之后从该备份恢复
- **THEN** 所有打卡项目、记录、图片、设置完整恢复

#### Scenario: 导出 CSV
- **WHEN** 用户选择导出 CSV 格式
- **THEN** 生成可用 Excel 打开的 CSV 文件，包含所有打卡记录

### Requirement: 提醒系统
系统 SHALL 支持固定时间提醒、间隔提醒、智能提醒，可通过通知/震动/铃声提醒，支持免打扰时段。

#### Scenario: 间隔提醒触发
- **WHEN** 设置"每 2 小时提醒喝水"，到达间隔
- **THEN** 系统通知出现，有快捷操作（已完成/稍后提醒）

### Requirement: 性能指标达标
系统 SHALL 满足冷启动 <1s、热启动 <300ms、列表滚动 60fps、动画 60fps、无内存泄漏。

#### Scenario: 冷启动性能
- **WHEN** 冷启动应用
- **THEN** 启动时间 < 1 秒

#### Scenario: 列表滚动流畅
- **WHEN** 快速滚动打卡列表
- **THEN** 保持 60fps，无掉帧

### Requirement: 应用锁与安全
系统 SHALL 支持应用锁（密码/指纹/面部识别），支持可选数据库加密（SQLCipher）。

#### Scenario: 应用锁启用
- **WHEN** 用户启用应用锁并设置密码
- **THEN** 每次进入应用需解锁，超时自动锁定

## MODIFIED Requirements

（无既有需求需要修改，本项目为全新开发）

## REMOVED Requirements

### Requirement: 用户注册与登录
**Reason**: 文档明确要求"无账号系统、完全离线"，应用不需要用户注册与登录。
**Migration**: 不实现此功能。所有数据本地存储，无需身份认证。

### Requirement: 云端数据同步
**Reason**: 文档明确要求"无云端同步、数据 100% 本地存储"，以保证用户隐私。
**Migration**: 不实现云端同步。提供本地备份与恢复功能作为数据迁移方案。

### Requirement: 位置获取（作为核心功能）
**Reason**: 文档未将位置获取列为核心功能，应用定位为"打卡助手"而非"考勤打卡"。
**Migration**: 不实现位置获取核心模块。如用户需求明确，可在打卡记录中可选附加位置信息（用户主动选择），但不作为核心功能。
