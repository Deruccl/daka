package com.timemark.app.domain.model

/**
 * 预设打卡模板库
 *
 * 包含健康、学习、生活、工作四大类常用打卡模板，
 * 用户可基于模板快速创建打卡项目。
 */
object TrackerTemplates {

    /** 模板分类：健康 */
    const val CATEGORY_HEALTH = "健康"

    /** 模板分类：学习 */
    const val CATEGORY_STUDY = "学习"

    /** 模板分类：生活 */
    const val CATEGORY_LIFE = "生活"

    /** 模板分类：工作 */
    const val CATEGORY_WORK = "工作"

    /** 健康类模板 */
    val health: List<TrackerTemplate> = listOf(
        TrackerTemplate(
            id = "health_water",
            name = "每日饮水",
            icon = "💧",
            color = "#2196F3",
            type = TrackerType.COUNT,
            unit = "杯",
            targetValue = 8.0,
            description = "每日饮水 8 杯，保持身体水分充足",
            category = CATEGORY_HEALTH
        ),
        TrackerTemplate(
            id = "health_diet",
            name = "饮食记录",
            icon = "🍽️",
            color = "#FF5722",
            type = TrackerType.IMAGE_TEXT,
            unit = "次",
            targetValue = 3.0,
            description = "记录三餐饮食，支持拍照 AI 识别",
            category = CATEGORY_HEALTH
        ),
        TrackerTemplate(
            id = "health_exercise",
            name = "运动健身",
            icon = "🏃",
            color = "#F44336",
            type = TrackerType.DURATION,
            unit = "分钟",
            targetValue = 30.0,
            description = "每日运动 30 分钟，保持健康体魄",
            category = CATEGORY_HEALTH
        ),
        TrackerTemplate(
            id = "health_sleep",
            name = "睡眠追踪",
            icon = "😴",
            color = "#3F51B5",
            type = TrackerType.VALUE,
            unit = "小时",
            targetValue = 8.0,
            description = "记录每日睡眠时长，目标 8 小时",
            category = CATEGORY_HEALTH
        ),
        TrackerTemplate(
            id = "health_weight",
            name = "体重记录",
            icon = "⚖️",
            color = "#795548",
            type = TrackerType.VALUE,
            unit = "kg",
            targetValue = 0.0,
            description = "记录体重变化趋势，0 表示无目标",
            category = CATEGORY_HEALTH
        ),
        TrackerTemplate(
            id = "health_meditation",
            name = "冥想练习",
            icon = "🧘",
            color = "#00BCD4",
            type = TrackerType.DURATION,
            unit = "分钟",
            targetValue = 15.0,
            description = "每日冥想 15 分钟，放松身心",
            category = CATEGORY_HEALTH
        ),
        TrackerTemplate(
            id = "health_medicine",
            name = "服药提醒",
            icon = "💊",
            color = "#E91E63",
            type = TrackerType.CHECK,
            unit = "次",
            targetValue = 1.0,
            description = "按时服药提醒，勿漏服",
            category = CATEGORY_HEALTH
        ),
        TrackerTemplate(
            id = "health_bowel",
            name = "排便记录",
            icon = "🚽",
            color = "#8BC34A",
            type = TrackerType.CHECK,
            unit = "次",
            targetValue = 1.0,
            description = "记录每日排便情况，关注肠道健康",
            category = CATEGORY_HEALTH
        )
    )

    /** 学习类模板 */
    val study: List<TrackerTemplate> = listOf(
        TrackerTemplate(
            id = "study_time",
            name = "学习时长",
            icon = "📚",
            color = "#9C27B0",
            type = TrackerType.DURATION,
            unit = "分钟",
            targetValue = 120.0,
            description = "每日学习 2 小时，持续进步",
            category = CATEGORY_STUDY
        ),
        TrackerTemplate(
            id = "study_reading",
            name = "阅读打卡",
            icon = "📖",
            color = "#673AB7",
            type = TrackerType.IMAGE_TEXT,
            unit = "页",
            targetValue = 30.0,
            description = "每日阅读 30 页，记录读书笔记",
            category = CATEGORY_STUDY
        ),
        TrackerTemplate(
            id = "study_vocabulary",
            name = "单词背诵",
            icon = "🔤",
            color = "#3F51B5",
            type = TrackerType.COUNT,
            unit = "个",
            targetValue = 50.0,
            description = "每日背诵 50 个单词",
            category = CATEGORY_STUDY
        ),
        TrackerTemplate(
            id = "study_handwriting",
            name = "练字打卡",
            icon = "✍️",
            color = "#009688",
            type = TrackerType.COUNT,
            unit = "字",
            targetValue = 100.0,
            description = "每日练字 100 个，提升书写",
            category = CATEGORY_STUDY
        ),
        TrackerTemplate(
            id = "study_coding",
            name = "编程练习",
            icon = "💻",
            color = "#607D8B",
            type = TrackerType.DURATION,
            unit = "分钟",
            targetValue = 60.0,
            description = "每日编程练习 1 小时",
            category = CATEGORY_STUDY
        ),
        TrackerTemplate(
            id = "study_listening",
            name = "听力练习",
            icon = "🎧",
            color = "#FF9800",
            type = TrackerType.DURATION,
            unit = "分钟",
            targetValue = 30.0,
            description = "每日听力练习 30 分钟",
            category = CATEGORY_STUDY
        )
    )

    /** 生活类模板 */
    val life: List<TrackerTemplate> = listOf(
        TrackerTemplate(
            id = "life_early_rise",
            name = "早起打卡",
            icon = "🌅",
            color = "#FFC107",
            type = TrackerType.CHECK,
            unit = "次",
            targetValue = 1.0,
            description = "每日 7 点前早起",
            category = CATEGORY_LIFE
        ),
        TrackerTemplate(
            id = "life_early_sleep",
            name = "早睡打卡",
            icon = "🌙",
            color = "#3F51B5",
            type = TrackerType.CHECK,
            unit = "次",
            targetValue = 1.0,
            description = "每日 23 点前入睡",
            category = CATEGORY_LIFE
        ),
        TrackerTemplate(
            id = "life_skincare",
            name = "护肤打卡",
            icon = "🧴",
            color = "#E91E63",
            type = TrackerType.CHECK,
            unit = "次",
            targetValue = 2.0,
            description = "早晚护肤两次",
            category = CATEGORY_LIFE
        ),
        TrackerTemplate(
            id = "life_housework",
            name = "家务打卡",
            icon = "🧹",
            color = "#4CAF50",
            type = TrackerType.CHECK,
            unit = "次",
            targetValue = 1.0,
            description = "每日完成一项家务",
            category = CATEGORY_LIFE
        ),
        TrackerTemplate(
            id = "life_accounting",
            name = "记账记录",
            icon = "💰",
            color = "#FF9800",
            type = TrackerType.COUNT,
            unit = "笔",
            targetValue = 1.0,
            description = "记录每日收支",
            category = CATEGORY_LIFE
        ),
        TrackerTemplate(
            id = "life_water_remind",
            name = "喝水提醒",
            icon = "🥤",
            color = "#03A9F4",
            type = TrackerType.COUNT,
            unit = "次",
            targetValue = 6.0,
            description = "定时喝水提醒，每日 6 次",
            category = CATEGORY_LIFE
        )
    )

    /** 工作类模板 */
    val work: List<TrackerTemplate> = listOf(
        TrackerTemplate(
            id = "work_time",
            name = "工作时长",
            icon = "💼",
            color = "#FF9800",
            type = TrackerType.DURATION,
            unit = "小时",
            targetValue = 8.0,
            description = "记录每日工作时长",
            category = CATEGORY_WORK
        ),
        TrackerTemplate(
            id = "work_task",
            name = "任务完成",
            icon = "✅",
            color = "#4CAF50",
            type = TrackerType.COUNT,
            unit = "个",
            targetValue = 5.0,
            description = "每日完成 5 个任务",
            category = CATEGORY_WORK
        ),
        TrackerTemplate(
            id = "work_pomodoro",
            name = "番茄钟",
            icon = "🍅",
            color = "#F44336",
            type = TrackerType.TIMER,
            unit = "个",
            targetValue = 8.0,
            description = "每日 8 个番茄钟，专注工作",
            category = CATEGORY_WORK
        ),
        TrackerTemplate(
            id = "work_daily_report",
            name = "日报记录",
            icon = "📝",
            color = "#607D8B",
            type = TrackerType.IMAGE_TEXT,
            unit = "篇",
            targetValue = 1.0,
            description = "每日撰写工作日报",
            category = CATEGORY_WORK
        )
    )

    /** 所有模板（按分类顺序） */
    val all: List<TrackerTemplate> = health + study + life + work

    /** 按分类获取模板 */
    fun getByCategory(category: String): List<TrackerTemplate> = when (category) {
        CATEGORY_HEALTH -> health
        CATEGORY_STUDY -> study
        CATEGORY_LIFE -> life
        CATEGORY_WORK -> work
        else -> emptyList()
    }
}
