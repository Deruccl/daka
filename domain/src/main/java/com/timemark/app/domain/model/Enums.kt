package com.timemark.app.domain.model

/** 打卡类型 */
enum class TrackerType {
    COUNT,        // 计数型（如喝水杯数）
    DURATION,     // 时长型（如运动时长，单位秒）
    VALUE,        // 数值型（如体重 kg）
    CHECK,        // 勾选型（完成/未完成）
    IMAGE_TEXT,   // 图文型（饮食记录、读书笔记）
    TIMER         // 计时型（开始/结束自动计时）
}

/** 打卡时间段 */
enum class TimePeriod {
    ALL_DAY,      // 全天
    MORNING,      // 早上 (5-12)
    AFTERNOON,    // 下午 (12-18)
    EVENING,      // 晚上 (18-24)
    CUSTOM        // 自定义
}

/** 提醒频率 */
enum class ReminderFrequency {
    DAILY,                  // 每天
    WEEKLY,                 // 每周指定日
    INTERVAL,               // 间隔（小时）
    SMART                   // 智能提醒
}

/** AI 提供商 */
enum class AIProvider {
    OPENAI,
    ANTHROPIC,
    GEMINI,
    BAIDU,
    ALIBABA,
    BYTEDANCE,
    ZHIPU,
    MOONSHOT,
    OLLAMA,
    CUSTOM
}

/** AI 模型类型 */
enum class AIModelType {
    TEXT,           // 纯文本
    MULTIMODAL,     // 多模态（图片+文本）
    VOICE           // 语音
}

/** AI 功能类型 */
enum class AIFeature {
    FOOD_RECOGNITION,       // 食物识别
    NUTRITION_ANALYSIS,     // 营养分析
    WATER_ANALYSIS,         // 饮水分析
    EXERCISE_ANALYSIS,      // 运动分析
    SLEEP_ANALYSIS,         // 睡眠分析
    HABIT_ANALYSIS,         // 习惯分析
    CHAT,                   // 聊天
    REPORT                  // 报告生成
}

/** 主题模式 */
enum class ThemeMode {
    LIGHT,      // 浅色
    DARK,       // 深色
    SYSTEM      // 跟随系统
}

/** 每周第一天 */
enum class FirstDayOfWeek {
    MONDAY,
    SUNDAY
}

/** 应用锁方式 */
enum class LockMethod {
    NONE,       // 无
    PASSWORD,   // 密码
    BIOMETRIC   // 生物识别（指纹/面部）
}
