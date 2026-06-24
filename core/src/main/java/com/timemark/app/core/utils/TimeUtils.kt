package com.timemark.app.core.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * 时间工具类
 *
 * 基于 java.time API 封装常用时间操作：
 * - 当前时间获取
 * - 日期/时间格式化与解析（中文星期）
 * - 时段问候语
 * - 日/周/月/年起止时间计算
 * - 日期间隔计算
 */
object TimeUtils {

    /** 中文环境 */
    private val zhLocale: Locale = Locale.CHINA

    /** 日期格式：yyyy-MM-dd */
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** 时间格式：HH:mm */
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /** 日期时间格式：yyyy-MM-dd HH:mm */
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /** 中文日期格式：yyyy年M月d日 EEEE（如：2024年3月5日 星期二） */
    private val zhDateFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy年M月d日 EEEE", zhLocale)

    /** 当前时间 */
    fun now(): LocalDateTime = LocalDateTime.now()

    /** 今天日期 */
    fun today(): LocalDate = LocalDate.now()

    /** 格式化为中文日期：yyyy年M月d日 EEEE */
    fun formatDate(date: LocalDate): String = date.format(zhDateFormatter)

    /** 格式化时间：HH:mm */
    fun formatTime(time: LocalTime): String = time.format(timeFormatter)

    /** 格式化日期时间：yyyy-MM-dd HH:mm */
    fun formatDateTime(dateTime: LocalDateTime): String = dateTime.format(dateTimeFormatter)

    /** 解析 "yyyy-MM-dd" 字符串为 LocalDate，失败返回 null */
    fun parseDate(text: String): LocalDate? = runCatching {
        LocalDate.parse(text, dateFormatter)
    }.getOrNull()

    /**
     * 根据当前时间返回问候语
     * - 5-8 早上好
     * - 8-11 上午好
     * - 11-13 中午好
     * - 13-18 下午好
     * - 18-23 晚上好
     * - 23-5 夜深了
     */
    fun greeting(): String {
        val hour = now().hour
        return when (hour) {
            in 5..7 -> "早上好"
            in 8..10 -> "上午好"
            in 11..12 -> "中午好"
            in 13..17 -> "下午好"
            in 18..22 -> "晚上好"
            else -> "夜深了"
        }
    }

    /** 某天的开始时间（00:00:00） */
    fun startOfDay(date: LocalDate): LocalDateTime = date.atStartOfDay()

    /** 某天的结束时间（23:59:59.999999999） */
    fun endOfDay(date: LocalDate): LocalDateTime = date.atTime(LocalTime.MAX)

    /**
     * 某日期所在周的周一（一周以周一开始）
     */
    fun startOfWeek(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** 某日期所在月的第一天 */
    fun startOfMonth(date: LocalDate): LocalDate = date.withDayOfMonth(1)

    /** 某日期所在年的第一天 */
    fun startOfYear(date: LocalDate): LocalDate = date.withDayOfYear(1)

    /** 计算两个日期之间的天数（含起止，end >= start 时返回非负数） */
    fun daysBetween(start: LocalDate, end: LocalDate): Int =
        (end.toEpochDay() - start.toEpochDay()).toInt()
}
