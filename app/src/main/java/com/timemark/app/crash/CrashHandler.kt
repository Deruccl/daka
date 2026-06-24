package com.timemark.app.crash

import android.content.Context
import android.os.Build
import android.util.Log
import com.timemark.app.core.utils.Logger
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Task 38.4: 崩溃处理器
 *
 * 实现 [Thread.UncaughtExceptionHandler]，捕获未处理的异常并保存到文件。
 *
 * 功能：
 * - 捕获崩溃信息：时间、线程、异常堆栈、设备信息、应用版本
 * - 保存到文件（app/crashes/crash_YYYYMMDD_HHmmss.txt）
 * - 最多保留 10 个崩溃记录，超过时删除最旧的
 * - 用户可通过设置开关启用/禁用
 *
 * 使用方式：
 * ```
 * CrashHandler.install(applicationContext)
 * ```
 *
 * 在 TimeMarkApp.onCreate 中调用，根据用户设置决定是否启用。
 */
class CrashHandler private constructor(
    private val context: Context
) : Thread.UncaughtExceptionHandler {

    /** 崩溃目录名 */
    private val crashDirName = "crashes"

    /** 最大保留崩溃记录数 */
    private val maxCrashCount = 10

    /** 系统默认异常处理器（崩溃处理后委托给系统，避免吞掉异常） */
    private val defaultHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    /** 崩溃收集开关（AtomicBoolean 保证线程安全） */
    private val enabled = AtomicBoolean(true)

    /** 防止递归崩溃（在处理崩溃时再次崩溃） */
    private val isHandling = AtomicBoolean(false)

    /**
     * Task 38.4: 安装崩溃处理器
     *
     * 将当前实例设置为全局未捕获异常处理器。
     * 应在 Application.onCreate 中调用。
     *
     * @param context 应用上下文
     * @return CrashHandler 实例，可用于控制开关
     */
    companion object {
        private val instance = AtomicReference<CrashHandler?>(null)

        /**
         * 安装崩溃处理器
         *
         * @param context 应用上下文
         * @return CrashHandler 实例
         */
        fun install(context: Context): CrashHandler {
            val handler = CrashHandler(context.applicationContext)
            instance.set(handler)
            Thread.setDefaultUncaughtExceptionHandler(handler)
            Logger.i(tag = TAG, msg = "CrashHandler 已安装")
            return handler
        }

        /**
         * 获取 CrashHandler 实例
         *
         * @return 已安装的实例，未安装返回 null
         */
        fun get(): CrashHandler? = instance.get()

        private const val TAG = "CrashHandler"
    }

    /**
     * Task 38.4: 设置崩溃收集开关
     *
     * @param enabled 是否启用崩溃收集
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled.set(enabled)
        Logger.i(tag = TAG, msg = "崩溃收集已${if (enabled) "启用" else "禁用"}")
    }

    /**
     * Task 38.4: 查询崩溃收集是否启用
     */
    fun isEnabled(): Boolean = enabled.get()

    /**
     * Task 38.4: 获取崩溃文件目录
     */
    fun getCrashDir(): File {
        return File(context.filesDir, crashDirName).also { if (!it.exists()) it.mkdirs() }
    }

    /**
     * Task 38.4: 获取所有崩溃记录文件列表（按修改时间降序，最新的在前）
     *
     * @return 崩溃文件列表
     */
    fun getCrashFiles(): List<File> {
        val dir = getCrashDir()
        return dir.listFiles { file -> file.isFile && file.name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    /**
     * Task 38.4: 读取崩溃文件内容
     *
     * @param file 崩溃文件
     * @return 文件内容字符串
     */
    fun readCrashFile(file: File): String {
        return if (file.exists()) file.readText() else ""
    }

    /**
     * Task 38.4: 导出崩溃文件
     *
     * 将指定崩溃文件复制到缓存目录，便于分享。
     *
     * @param file 源崩溃文件
     * @return 导出的文件
     */
    fun exportCrashFile(file: File): File? {
        if (!file.exists()) return null
        val exportDir = File(context.cacheDir, "exported_crashes").apply { mkdirs() }
        val exportFile = File(exportDir, file.name)
        file.copyTo(exportFile, overwrite = true)
        return exportFile
    }

    /**
     * Task 38.4: 清除所有崩溃记录
     *
     * @return 是否成功清除
     */
    fun clearAllCrashes(): Boolean {
        val dir = getCrashDir()
        var success = true
        dir.listFiles()?.forEach { file ->
            if (file.isFile) success = file.delete() && success
        }
        return success
    }

    /**
     * Task 38.4: 未捕获异常处理回调
     *
     * 当线程抛出未捕获异常时触发：
     * 1. 检查崩溃收集开关
     * 2. 防止递归崩溃
     * 3. 收集崩溃信息并写入文件
     * 4. 清理超出上限的旧崩溃记录
     * 5. 委托给系统默认处理器（让应用正常崩溃，不吞掉异常）
     *
     * @param t 抛出异常的线程
     * @param e 未捕获的异常
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
        // 检查开关
        if (!enabled.get()) {
            defaultHandler?.uncaughtException(t, e)
            return
        }

        // 防止递归崩溃
        if (!isHandling.compareAndSet(false, true)) {
            defaultHandler?.uncaughtException(t, e)
            return
        }

        try {
            // 写入崩溃日志文件
            writeCrashToFile(t, e)
            // 清理超出上限的旧记录
            cleanupOldCrashes()
        } catch (writeException: Throwable) {
            // 写入崩溃日志时出错，记录到 Logcat
            Log.e(TAG, "写入崩溃日志失败", writeException)
        } finally {
            isHandling.set(false)
        }

        // 委托给系统默认处理器，让应用正常崩溃
        defaultHandler?.uncaughtException(t, e)
    }

    /**
     * Task 38.4: 将崩溃信息写入文件
     *
     * 文件名格式：crash_YYYYMMDD_HHmmss.txt
     * 文件内容包含：
     * - 崩溃时间
     * - 线程信息
     * - 异常类型与消息
     * - 完整堆栈
     * - 设备信息（型号、品牌、SDK 版本）
     * - 应用版本
     *
     * @param thread 崩溃线程
     * @param throwable 异常
     */
    private fun writeCrashToFile(thread: Thread, throwable: Throwable) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val crashDir = getCrashDir()
        val crashFile = File(crashDir, "crash_$timestamp.txt")

        // 格式化显示时间
        val displayTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

        // 获取堆栈字符串
        val stackTrace = getStackTraceString(throwable)

        // 构建崩溃报告
        val report = buildString {
            appendLine("========== TimeMark 崩溃报告 ==========")
            appendLine()
            appendLine("崩溃时间: $displayTime")
            appendLine("线程: ${thread.name} (id=${thread.id}, state=${thread.state})")
            appendLine()
            appendLine("---------- 异常信息 ----------")
            appendLine("异常类型: ${throwable.javaClass.name}")
            appendLine("异常消息: ${throwable.message}")
            appendLine()
            appendLine("---------- 堆栈跟踪 ----------")
            appendLine(stackTrace)
            appendLine()
            appendLine("---------- 设备信息 ----------")
            appendLine("品牌: ${Build.BRAND}")
            appendLine("型号: ${Build.MODEL}")
            appendLine("设备: ${Build.DEVICE}")
            appendLine("产品: ${Build.PRODUCT}")
            appendLine("制造商: ${Build.MANUFACTURER}")
            appendLine("SDK 版本: ${Build.VERSION.SDK_INT} (API ${Build.VERSION.SDK_INT})")
            appendLine("系统版本: ${Build.VERSION.RELEASE}")
            appendLine("构建号: ${Build.DISPLAY}")
            appendLine()
            appendLine("---------- 应用信息 ----------")
            appendLine("应用版本名: ${getAppVersionName()}")
            appendLine("应用版本号: ${getAppVersionCode()}")
            appendLine("包名: ${context.packageName}")
            appendLine()
            appendLine("========== 崩溃报告结束 ==========")
        }

        // 写入文件
        crashFile.writeText(report)

        Logger.e(tag = TAG, msg = "崩溃已记录: ${crashFile.name}", tr = throwable)
    }

    /**
     * Task 38.4: 清理超出上限的旧崩溃记录
     *
     * 保留最近 [maxCrashCount] 个崩溃文件，删除更旧的。
     */
    private fun cleanupOldCrashes() {
        val files = getCrashFiles()
        if (files.size <= maxCrashCount) return

        // 按修改时间降序排列，删除超出上限的旧文件
        files.drop(maxCrashCount).forEach { file ->
            file.delete()
        }
        Logger.i(tag = TAG, msg = "已清理 ${files.size - maxCrashCount} 个旧崩溃记录")
    }

    /** 获取异常堆栈字符串 */
    private fun getStackTraceString(tr: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        return sw.toString()
    }

    /** 获取应用版本名 */
    private fun getAppVersionName(): String {
        return runCatching {
            val pm = context.packageManager
            val pInfo = pm.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "unknown"
        }.getOrDefault("unknown")
    }

    /** 获取应用版本号 */
    private fun getAppVersionCode(): Long {
        return runCatching {
            val pm = context.packageManager
            val pInfo = pm.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
        }.getOrDefault(0L)
    }
}
