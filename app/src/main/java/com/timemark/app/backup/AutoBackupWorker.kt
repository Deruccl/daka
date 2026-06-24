package com.timemark.app.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.timemark.app.core.utils.Logger
import com.timemark.app.domain.model.Record
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.repository.RecordRepository
import com.timemark.app.domain.repository.SettingsRepository
import com.timemark.app.domain.repository.TrackerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 自动备份 Worker（Task 32.4）
 *
 * 周期性执行备份任务：
 * 1. 读取所有 Tracker 与 Record 数据
 * 2. 序列化为 JSON 并打包为 ZIP
 * 3. 保存到应用外部存储 backup 目录
 * 4. 清理超出保留版本数的旧备份文件
 *
 * 调度由 [AutoBackupScheduler] 管理，支持每日/每周/每月频率。
 */
@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val trackerRepository: TrackerRepository,
    private val recordRepository: RecordRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    override suspend fun doWork(): Result {
        return try {
            Logger.i(tag = TAG, msg = "自动备份开始执行")
            performBackup()
            cleanupOldBackups()
            Logger.i(tag = TAG, msg = "自动备份完成")
            Result.success()
        } catch (e: Exception) {
            Logger.e(tag = TAG, msg = "自动备份失败：${e.message}", tr = e)
            Result.retry()
        }
    }

    /**
     * 执行备份：读取数据 → 序列化 → 打包 ZIP → 保存文件
     */
    private suspend fun performBackup() {
        val trackers = trackerRepository.getAllTrackers().first()
        val records = getAllRecords()
        val timestamp = System.currentTimeMillis()

        val backupDir = getBackupDir()
        val fileName = generateFileName(timestamp)
        val backupFile = File(backupDir, fileName)

        // 将数据写入 ZIP 文件
        ZipOutputStream(backupFile.outputStream().buffered()).use { zip ->
            // 元数据
            zip.putNextEntry(ZipEntry("meta.json"))
            val meta = """{"version":1,"timestamp":$timestamp,"trackerCount":${trackers.size},"recordCount":${records.size}}"""
            zip.write(meta.toByteArray())
            zip.closeEntry()

            // 打卡项
            zip.putNextEntry(ZipEntry("trackers.json"))
            zip.write(
                json.encodeToString(ListSerializer(Tracker.serializer()), trackers).toByteArray()
            )
            zip.closeEntry()

            // 记录
            zip.putNextEntry(ZipEntry("records.json"))
            zip.write(
                json.encodeToString(ListSerializer(Record.serializer()), records).toByteArray()
            )
            zip.closeEntry()
        }
        Logger.i(tag = TAG, msg = "备份文件已保存：${backupFile.absolutePath}")
    }

    /** 获取所有记录（使用宽日期范围查询） */
    private suspend fun getAllRecords(): List<Record> {
        return recordRepository.getAllRecordsByDateRange("1970-01-01", "2099-12-31").first()
    }

    /**
     * 清理超出保留版本数的旧备份文件。
     * 按 lastModified 降序排列，保留最新的 N 个，其余删除。
     */
    private suspend fun cleanupOldBackups() {
        val keepCount = settingsRepository.autoBackupKeepCount.first()
        if (keepCount <= 0) return

        val backupDir = getBackupDir()
        val backupFiles = backupDir.listFiles { file ->
            file.isFile && file.name.startsWith(BACKUP_FILE_PREFIX) && file.name.endsWith(".zip")
        }?.sortedByDescending { it.lastModified() } ?: return

        if (backupFiles.size > keepCount) {
            backupFiles.drop(keepCount).forEach { file ->
                if (file.delete()) {
                    Logger.d(tag = TAG, msg = "已删除旧备份：${file.name}")
                }
            }
        }
    }

    /** 生成备份文件名 */
    private fun generateFileName(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "$BACKUP_FILE_PREFIX${dateFormat.format(Date(timestamp))}.zip"
    }

    /** 获取备份目录 */
    private fun getBackupDir(): File {
        val dir = File(applicationContext.getExternalFilesDir(null), "backup")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    companion object {
        private const val TAG = "AutoBackupWorker"
        private const val BACKUP_FILE_PREFIX = "timemark_auto_"

        /** 唯一工作名称，用于调度标识 */
        const val WORK_NAME = "timemark_auto_backup"
    }
}
