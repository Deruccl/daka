package com.timemark.app.feature.settings.backup

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.timemark.app.domain.model.Record
import com.timemark.app.domain.model.Tracker
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

/**
 * PDF 导出工具（Task 32.5）
 *
 * 使用 Android 原生 PdfDocument 生成 PDF 报告，无需第三方库。
 *
 * 报告内容：
 * 1. 标题与时间范围
 * 2. 统计摘要（总记录数、打卡项数、时间跨度）
 * 3. 记录列表（日期、打卡项、数值、备注）
 * 4. 图表占位区域（预留）
 *
 * @param context 用于访问文件目录
 */
class PdfExporter(private val context: Context) {

    companion object {
        private const val PAGE_WIDTH = 595 // A4 宽度（点，72dpi）
        private const val PAGE_HEIGHT = 842 // A4 高度（点）
        private const val MARGIN = 36f // 页边距（0.5 英寸）
        private const val TITLE_SIZE = 24f
        private const val HEADING_SIZE = 16f
        private const val BODY_SIZE = 11f
        private const val SMALL_SIZE = 9f
        private const val LINE_HEIGHT = 18f
    }

    /**
     * 导出记录为 PDF 文件。
     *
     * @param records 打卡记录列表
     * @param trackers 打卡项列表（用于名称映射）
     * @param timeRange 时间范围（起始日期，结束日期）
     * @return 生成的 PDF 文件
     */
    fun exportToPdf(
        records: List<Record>,
        trackers: List<Tracker>,
        timeRange: Pair<LocalDate, LocalDate>
    ): File {
        val document = PdfDocument()
        val trackerMap = trackers.associateBy { it.id }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = MARGIN

        // ===== 标题 =====
        yPos = drawText(
            canvas, "时光印记 - 打卡报告",
            MARGIN, yPos + TITLE_SIZE, TITLE_SIZE, Paint.ALIGN_LEFT, bold = true
        )
        yPos += 10f

        // 时间范围
        val dateFmt = DateTimeFormatterHolder.format
        val rangeText = "时间范围：${dateFmt.format(timeRange.first)} ~ ${dateFmt.format(timeRange.second)}"
        yPos = drawText(canvas, rangeText, MARGIN, yPos + BODY_SIZE, BODY_SIZE, Paint.ALIGN_LEFT)
        yPos += 6f

        val generatedText = "生成时间：${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}"
        yPos = drawText(canvas, generatedText, MARGIN, yPos + BODY_SIZE, BODY_SIZE, Paint.ALIGN_LEFT)
        yPos += 20f

        // ===== 统计摘要 =====
        yPos = drawText(canvas, "统计摘要", MARGIN, yPos + HEADING_SIZE, HEADING_SIZE, Paint.ALIGN_LEFT, bold = true)
        yPos += 8f

        val totalRecords = records.size
        val totalTrackers = trackerMap.size
        val dateSpan = java.time.temporal.ChronoUnit.DAYS.between(timeRange.first, timeRange.second) + 1
        yPos = drawText(canvas, "• 总记录数：$totalRecords", MARGIN, yPos + BODY_SIZE, BODY_SIZE, Paint.ALIGN_LEFT)
        yPos += LINE_HEIGHT - BODY_SIZE
        yPos = drawText(canvas, "• 打卡项数：$totalTrackers", MARGIN, yPos + BODY_SIZE, BODY_SIZE, Paint.ALIGN_LEFT)
        yPos += LINE_HEIGHT - BODY_SIZE
        yPos = drawText(canvas, "• 时间跨度：$dateSpan 天", MARGIN, yPos + BODY_SIZE, BODY_SIZE, Paint.ALIGN_LEFT)
        yPos += 20f

        // ===== 图表占位 =====
        yPos = drawText(canvas, "数据图表", MARGIN, yPos + HEADING_SIZE, HEADING_SIZE, Paint.ALIGN_LEFT, bold = true)
        yPos += 8f
        // 绘制图表占位框
        val placeholderPaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + 120f, placeholderPaint)
        val centerPaint = Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = SMALL_SIZE
            textAlign = Paint.ALIGN_CENTER
        }
        canvas.drawText("[图表占位区域]", (PAGE_WIDTH) / 2f, yPos + 65f, centerPaint)
        yPos += 140f

        // ===== 记录列表 =====
        yPos = drawText(canvas, "记录列表", MARGIN, yPos + HEADING_SIZE, HEADING_SIZE, Paint.ALIGN_LEFT, bold = true)
        yPos += 8f

        // 表头
        val headerPaint = Paint().apply {
            textSize = SMALL_SIZE
            isFakeBoldText = true
        }
        canvas.drawText("日期", MARGIN, yPos + SMALL_SIZE, headerPaint)
        canvas.drawText("打卡项", MARGIN + 90, yPos + SMALL_SIZE, headerPaint)
        canvas.drawText("数值", MARGIN + 250, yPos + SMALL_SIZE, headerPaint)
        canvas.drawText("备注", MARGIN + 320, yPos + SMALL_SIZE, headerPaint)
        yPos += LINE_HEIGHT

        // 分隔线
        val linePaint = Paint().apply {
            color = android.graphics.Color.GRAY
            strokeWidth = 0.5f
        }
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, linePaint)
        yPos += 4f

        // 数据行
        val bodyPaint = Paint().apply { textSize = SMALL_SIZE }
        for (record in records) {
            // 检查是否需要换页
            if (yPos > PAGE_HEIGHT - MARGIN - LINE_HEIGHT) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPos = MARGIN
            }

            val tracker = trackerMap[record.trackerId]
            canvas.drawText(record.date, MARGIN, yPos + SMALL_SIZE, bodyPaint)
            canvas.drawText(tracker?.name ?: "未知", MARGIN + 90, yPos + SMALL_SIZE, bodyPaint)
            val valueText = "${record.value} ${tracker?.unit ?: ""}"
            canvas.drawText(valueText, MARGIN + 250, yPos + SMALL_SIZE, bodyPaint)
            val note = if (record.note.length > 30) record.note.take(30) + "…" else record.note
            canvas.drawText(note, MARGIN + 320, yPos + SMALL_SIZE, bodyPaint)
            yPos += LINE_HEIGHT
        }

        document.finishPage(page)

        // 写入文件
        val exportDir = File(context.getExternalFilesDir(null), "export").apply { if (!exists()) mkdirs() }
        val fileName = "timemark_pdf_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val pdfFile = File(exportDir, fileName)
        FileOutputStream(pdfFile).use { out -> document.writeTo(out) }
        document.close()

        return pdfFile
    }

    /** 绘制文本辅助方法 */
    private fun drawText(
        canvas: android.graphics.Canvas,
        text: String,
        x: Float,
        y: Float,
        textSize: Float,
        align: Int = Paint.ALIGN_LEFT,
        bold: Boolean = false
    ): Float {
        val paint = Paint().apply {
            this.textSize = textSize
            textAlign = align
            isFakeBoldText = bold
            color = android.graphics.Color.BLACK
        }
        canvas.drawText(text, x, y, paint)
        return y
    }

    /** 日期格式化器持有对象（避免重复创建） */
    private object DateTimeFormatterHolder {
        val format = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
