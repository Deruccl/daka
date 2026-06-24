package com.timemark.app.feature.settings.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.model.Record
import com.timemark.app.domain.model.Tracker
import com.timemark.app.domain.repository.RecordRepository
import com.timemark.app.domain.repository.TrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * 备份与恢复 ViewModel
 *
 * 提供完整备份、仅数据备份、从备份恢复、导出 CSV / JSON 等功能。
 * 备份格式为 ZIP，内部包含 JSON 格式的 trackers 与 records。
 * 自动备份设置使用 SharedPreferences 持久化。
 */
@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackerRepository: TrackerRepository,
    private val recordRepository: RecordRepository
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    /** 备份/恢复状态 */
    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState

    /** 自动备份开关 */
    private val _autoBackupEnabled = MutableStateFlow(getAutoBackupPref())
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    /** 自动备份频率（天） */
    private val _autoBackupFrequency = MutableStateFlow(getAutoBackupFrequencyPref())
    val autoBackupFrequency: StateFlow<Int> = _autoBackupFrequency

    /** 最近一次备份时间戳 */
    private val _lastBackupTime = MutableStateFlow(getLastBackupTimePref())
    val lastBackupTime: StateFlow<Long> = _lastBackupTime

    /**
     * 创建完整备份（数据库 + 配置）
     * 生成 ZIP 文件，包含 trackers.json、records.json、meta.json
     */
    fun createFullBackup() {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress("正在创建完整备份...")
            try {
                val result = withContext(Dispatchers.IO) {
                    val trackers = trackerRepository.getAllTrackers().first()
                    val records = getAllRecords()
                    val backupData = BackupData(
                        version = BACKUP_VERSION,
                        timestamp = System.currentTimeMillis(),
                        trackers = trackers,
                        records = records
                    )
                    val fileName = generateFileName("full")
                    val backupDir = getBackupDir()
                    val backupFile = File(backupDir, fileName)
                    writeBackupZip(backupFile, backupData)
                    saveLastBackupTime(System.currentTimeMillis())
                    backupFile.absolutePath
                }
                _backupState.value = BackupState.Success("完整备份已保存至：\n$result")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "备份失败")
            }
        }
    }

    /**
     * 创建仅数据备份（仅数据库，不含配置）
     */
    fun createDataOnlyBackup() {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress("正在创建数据备份...")
            try {
                val result = withContext(Dispatchers.IO) {
                    val trackers = trackerRepository.getAllTrackers().first()
                    val records = getAllRecords()
                    val backupData = BackupData(
                        version = BACKUP_VERSION,
                        timestamp = System.currentTimeMillis(),
                        trackers = trackers,
                        records = records
                    )
                    val fileName = generateFileName("data")
                    val backupDir = getBackupDir()
                    val backupFile = File(backupDir, fileName)
                    writeBackupZip(backupFile, backupData)
                    saveLastBackupTime(System.currentTimeMillis())
                    backupFile.absolutePath
                }
                _backupState.value = BackupState.Success("数据备份已保存至：\n$result")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "备份失败")
            }
        }
    }

    /**
     * 从备份文件恢复
     * 读取 ZIP 文件，解析 JSON，替换现有数据
     */
    fun restoreFromBackup(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress("正在从备份恢复...")
            try {
                withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("无法读取备份文件")
                    inputStream.use { stream ->
                        val backupData = readBackupZip(stream)
                        // 导入打卡项，建立旧 ID -> 新 ID 的映射
                        val trackerIdMap = mutableMapOf<Long, Long>()
                        backupData.trackers.forEach { tracker ->
                            val newId = trackerRepository.insertTracker(tracker.copy(id = 0))
                            trackerIdMap[tracker.id] = newId
                        }
                        // 导入记录，使用映射后的新 trackerId
                        backupData.records.forEach { record ->
                            val newTrackerId = trackerIdMap[record.trackerId] ?: record.trackerId
                            recordRepository.insertRecord(record.copy(id = 0, trackerId = newTrackerId))
                        }
                    }
                }
                _backupState.value = BackupState.Success("恢复成功")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "恢复失败")
            }
        }
    }

    /**
     * 导出记录为 CSV
     * @param trackerId 指定打卡项 ID，null 表示全部
     * @param startDate 起始日期（yyyy-MM-dd），null 表示不限
     * @param endDate 结束日期（yyyy-MM-dd），null 表示不限
     */
    fun exportCSV(
        trackerId: Long? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress("正在导出 CSV...")
            try {
                val result = withContext(Dispatchers.IO) {
                    val records = getRecordsForExport(trackerId, startDate, endDate)
                    val trackers = trackerRepository.getAllTrackers().first()
                    val trackerMap = trackers.associateBy { it.id }
                    val fileName = generateFileName("csv", "csv")
                    val exportDir = getExportDir()
                    val exportFile = File(exportDir, fileName)
                    writeCsv(exportFile, records, trackerMap)
                    exportFile.absolutePath
                }
                _backupState.value = BackupState.Success("CSV 已导出至：\n$result")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "导出失败")
            }
        }
    }

    /**
     * 导出记录为 JSON
     * @param trackerId 指定打卡项 ID，null 表示全部
     * @param startDate 起始日期（yyyy-MM-dd），null 表示不限
     * @param endDate 结束日期（yyyy-MM-dd），null 表示不限
     */
    fun exportJSON(
        trackerId: Long? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress("正在导出 JSON...")
            try {
                val result = withContext(Dispatchers.IO) {
                    val records = getRecordsForExport(trackerId, startDate, endDate)
                    val fileName = generateFileName("json", "json")
                    val exportDir = getExportDir()
                    val exportFile = File(exportDir, fileName)
                    exportFile.writeText(
                        json.encodeToString(ListSerializer(Record.serializer()), records),
                        Charsets.UTF_8
                    )
                    exportFile.absolutePath
                }
                _backupState.value = BackupState.Success("JSON 已导出至：\n$result")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "导出失败")
            }
        }
    }

    /**
     * 导出记录为 PDF（Task 32.5）
     * 使用 Android 原生 PdfDocument 生成报告，包含标题、统计摘要、记录列表、图表占位。
     *
     * @param trackerId 指定打卡项 ID，null 表示全部
     * @param startDate 起始日期（yyyy-MM-dd），null 表示近 30 天
     * @param endDate 结束日期（yyyy-MM-dd），null 表示今天
     */
    fun exportToPdf(
        trackerId: Long? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress("正在导出 PDF...")
            try {
                val result = withContext(Dispatchers.IO) {
                    val end = endDate ?: java.time.LocalDate.now().toString()
                    val start = startDate ?: java.time.LocalDate.now().minusDays(30).toString()
                    val records = getRecordsForExport(trackerId, start, end)
                    val trackers = trackerRepository.getAllTrackers().first()
                    val exporter = PdfExporter(context)
                    val pdfFile = exporter.exportToPdf(
                        records = records,
                        trackers = trackers,
                        timeRange = java.time.LocalDate.parse(start) to java.time.LocalDate.parse(end)
                    )
                    pdfFile.absolutePath
                }
                _backupState.value = BackupState.Success("PDF 已导出至：\n$result")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "导出失败")
            }
        }
    }

    /** 设置自动备份开关 */
    fun setAutoBackupEnabled(enabled: Boolean) {
        _autoBackupEnabled.value = enabled
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTO_BACKUP, enabled).apply()
    }

    /** 设置自动备份频率（天） */
    fun setAutoBackupFrequency(days: Int) {
        _autoBackupFrequency.value = days
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_AUTO_BACKUP_FREQ, days).apply()
    }

    /** 重置状态为空闲 */
    fun resetState() {
        _backupState.value = BackupState.Idle
    }

    // ===== 内部方法 =====

    /** 获取所有记录（使用宽日期范围查询） */
    private suspend fun getAllRecords(): List<Record> {
        return recordRepository.getAllRecordsByDateRange("1970-01-01", "2099-12-31").first()
    }

    /** 按条件获取记录用于导出 */
    private suspend fun getRecordsForExport(
        trackerId: Long?,
        startDate: String?,
        endDate: String?
    ): List<Record> {
        val start = startDate ?: "1970-01-01"
        val end = endDate ?: "2099-12-31"
        return if (trackerId != null) {
            recordRepository.getRecordsByTrackerAndDateRange(trackerId, start, end).first()
        } else {
            recordRepository.getAllRecordsByDateRange(start, end).first()
        }
    }

    /** 将备份数据写入 ZIP 文件 */
    private fun writeBackupZip(file: File, data: BackupData) {
        ZipOutputStream(file.outputStream().buffered()).use { zip ->
            // 元数据
            zip.putNextEntry(ZipEntry("meta.json"))
            val meta = BackupMeta(
                version = data.version,
                timestamp = data.timestamp,
                trackerCount = data.trackers.size,
                recordCount = data.records.size
            )
            zip.write(json.encodeToString(BackupMeta.serializer(), meta).toByteArray())
            zip.closeEntry()
            // 打卡项
            zip.putNextEntry(ZipEntry("trackers.json"))
            zip.write(
                json.encodeToString(ListSerializer(Tracker.serializer()), data.trackers).toByteArray()
            )
            zip.closeEntry()
            // 记录
            zip.putNextEntry(ZipEntry("records.json"))
            zip.write(
                json.encodeToString(ListSerializer(Record.serializer()), data.records).toByteArray()
            )
            zip.closeEntry()
        }
    }

    /** 从 ZIP 输入流读取备份数据 */
    private fun readBackupZip(stream: java.io.InputStream): BackupData {
        var trackers: List<Tracker> = emptyList()
        var records: List<Record> = emptyList()
        var meta: BackupMeta? = null

        ZipInputStream(stream.buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val content = zip.readBytes().toString(Charsets.UTF_8)
                when (entry.name) {
                    "meta.json" -> meta = json.decodeFromString(BackupMeta.serializer(), content)
                    "trackers.json" -> trackers =
                        json.decodeFromString(ListSerializer(Tracker.serializer()), content)
                    "records.json" -> records =
                        json.decodeFromString(ListSerializer(Record.serializer()), content)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return BackupData(
            version = meta?.version ?: BACKUP_VERSION,
            timestamp = meta?.timestamp ?: System.currentTimeMillis(),
            trackers = trackers,
            records = records
        )
    }

    /** 将记录写入 CSV 文件 */
    private fun writeCsv(
        file: File,
        records: List<Record>,
        trackerMap: Map<Long, Tracker>
    ) {
        BufferedWriter(OutputStreamWriter(file.outputStream(), Charsets.UTF_8)).use { writer ->
            // UTF-8 BOM，确保 Excel 正确识别编码
            writer.write("\uFEFF")
            // 表头
            writer.write("ID,打卡项,日期,时间,数值,单位,备注,标签,心情,创建时间")
            writer.newLine()
            // 数据行
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            records.forEach { record ->
                val tracker = trackerMap[record.trackerId]
                val row = listOf(
                    record.id.toString(),
                    escapeCsv(tracker?.name ?: ""),
                    escapeCsv(record.date),
                    escapeCsv(record.time),
                    record.value.toString(),
                    escapeCsv(tracker?.unit ?: ""),
                    escapeCsv(record.note),
                    escapeCsv(record.tags.joinToString(";")),
                    escapeCsv(record.mood ?: ""),
                    dateFormat.format(Date(record.createdAt))
                ).joinToString(",")
                writer.write(row)
                writer.newLine()
            }
        }
    }

    /** CSV 字段转义：包含逗号、引号或换行时用双引号包裹 */
    private fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    /** 生成备份/导出文件名 */
    private fun generateFileName(prefix: String, ext: String = "zip"): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "timemark_${prefix}_${dateFormat.format(Date())}.$ext"
    }

    /** 获取备份目录 */
    private fun getBackupDir(): File {
        val dir = File(context.getExternalFilesDir(null), "backup")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** 获取导出目录 */
    private fun getExportDir(): File {
        val dir = File(context.getExternalFilesDir(null), "export")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // ===== SharedPreferences 读写 =====

    private fun getAutoBackupPref(): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_BACKUP, false)

    private fun getAutoBackupFrequencyPref(): Int =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_AUTO_BACKUP_FREQ, 7)

    private fun getLastBackupTimePref(): Long =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_BACKUP, 0L)

    private fun saveLastBackupTime(time: Long) {
        _lastBackupTime.value = time
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putLong(KEY_LAST_BACKUP, time).apply()
    }

    companion object {
        private const val PREF_NAME = "backup_prefs"
        private const val KEY_AUTO_BACKUP = "auto_backup_enabled"
        private const val KEY_AUTO_BACKUP_FREQ = "auto_backup_frequency"
        private const val KEY_LAST_BACKUP = "last_backup_time"
        private const val BACKUP_VERSION = 1
    }
}

/**
 * 备份/恢复状态
 */
sealed class BackupState {
    /** 空闲 */
    object Idle : BackupState()
    /** 进行中 */
    data class InProgress(val message: String) : BackupState()
    /** 成功 */
    data class Success(val message: String) : BackupState()
    /** 失败 */
    data class Error(val message: String) : BackupState()
}

/** 备份数据结构 */
@Serializable
private data class BackupData(
    val version: Int,
    val timestamp: Long,
    val trackers: List<Tracker>,
    val records: List<Record>
)

/** 备份元数据 */
@Serializable
private data class BackupMeta(
    val version: Int,
    val timestamp: Long,
    val trackerCount: Int,
    val recordCount: Int
)
