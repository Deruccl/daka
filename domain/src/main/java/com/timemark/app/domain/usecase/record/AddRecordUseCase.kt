package com.timemark.app.domain.usecase.record

import com.timemark.app.domain.model.Record
import com.timemark.app.domain.repository.RecordRepository
import javax.inject.Inject

/**
 * 新增打卡记录用例
 *
 * 支持两种调用方式：
 * 1. 通过字段参数构造 Record 后插入；
 * 2. 直接传入已构造好的 Record 对象。
 */
class AddRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    /** 通过字段参数构造记录并插入，返回新记录 id */
    suspend operator fun invoke(
        trackerId: Long,
        value: Double = 1.0,
        date: String,
        time: String,
        note: String = "",
        images: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        mood: String? = null,
        duration: Long = 0
    ): Long {
        val record = Record(
            trackerId = trackerId,
            value = value,
            date = date,
            time = time,
            timestamp = System.currentTimeMillis(),
            note = note,
            images = images,
            tags = tags,
            mood = mood,
            duration = duration
        )
        return recordRepository.insertRecord(record)
    }

    /** 直接插入已构造好的 Record，返回新记录 id */
    suspend operator fun invoke(record: Record): Long =
        recordRepository.insertRecord(record)
}
