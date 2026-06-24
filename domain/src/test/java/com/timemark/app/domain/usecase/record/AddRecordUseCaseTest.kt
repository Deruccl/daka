package com.timemark.app.domain.usecase.record

import com.timemark.app.domain.model.Record
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * AddRecordUseCase 单元测试
 *
 * 验证添加打卡记录的业务逻辑，包括字段构造与直接传入两种方式。
 */
class AddRecordUseCaseTest {

    private lateinit var fakeRepository: FakeRecordRepository
    private lateinit var useCase: AddRecordUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeRecordRepository()
        useCase = AddRecordUseCase(fakeRepository)
    }

    @Test
    fun invoke_字段参数_返回非零ID() = runTest {
        val id = useCase(
            trackerId = 1L,
            date = "2024-06-15",
            time = "14:30"
        )
        assertTrue(id > 0)
    }

    @Test
    fun invoke_字段参数_正确保存到仓库() = runTest {
        val id = useCase(
            trackerId = 5L,
            value = 3.0,
            date = "2024-06-15",
            time = "10:00",
            note = "测试记录"
        )
        val saved = fakeRepository.getRecordById(id)
        assertEquals(5L, saved!!.trackerId)
        assertEquals(3.0, saved.value, 0.001)
        assertEquals("2024-06-15", saved.date)
        assertEquals("10:00", saved.time)
        assertEquals("测试记录", saved.note)
    }

    @Test
    fun invoke_字段参数_默认值为1() = runTest {
        val id = useCase(
            trackerId = 1L,
            date = "2024-06-15",
            time = "12:00"
        )
        val saved = fakeRepository.getRecordById(id)
        assertEquals(1.0, saved!!.value, 0.001)
    }

    @Test
    fun invoke_字段参数_带图片和标签() = runTest {
        val id = useCase(
            trackerId = 1L,
            date = "2024-06-15",
            time = "08:00",
            images = listOf("img1.jpg", "img2.jpg"),
            tags = listOf("早餐", "健康")
        )
        val saved = fakeRepository.getRecordById(id)
        assertEquals(2, saved!!.images.size)
        assertEquals("img1.jpg", saved.images[0])
        assertEquals(2, saved.tags.size)
        assertEquals("早餐", saved.tags[0])
    }

    @Test
    fun invoke_字段参数_带心情和时长() = runTest {
        val id = useCase(
            trackerId = 1L,
            date = "2024-06-15",
            time = "18:00",
            mood = "开心",
            duration = 3600000L
        )
        val saved = fakeRepository.getRecordById(id)
        assertEquals("开心", saved!!.mood)
        assertEquals(3600000L, saved.duration)
    }

    @Test
    fun invoke_Record对象_返回非零ID() = runTest {
        val record = Record(
            trackerId = 1L,
            value = 2.0,
            date = "2024-06-15",
            time = "14:00",
            timestamp = System.currentTimeMillis()
        )
        val id = useCase(record)
        assertTrue(id > 0)
    }

    @Test
    fun invoke_Record对象_正确保存() = runTest {
        val record = Record(
            trackerId = 3L,
            value = 5.0,
            date = "2024-06-16",
            time = "09:30",
            timestamp = 1718447400000L,
            note = "直接传入 Record"
        )
        val id = useCase(record)
        val saved = fakeRepository.getRecordById(id)
        assertEquals(3L, saved!!.trackerId)
        assertEquals(5.0, saved.value, 0.001)
        assertEquals("2024-06-16", saved.date)
        assertEquals("09:30", saved.time)
        assertEquals("直接传入 Record", saved.note)
    }

    @Test
    fun invoke_多条记录_ID递增() = runTest {
        val id1 = useCase(trackerId = 1L, date = "2024-06-15", time = "10:00")
        val id2 = useCase(trackerId = 1L, date = "2024-06-15", time = "11:00")
        val id3 = useCase(trackerId = 1L, date = "2024-06-15", time = "12:00")
        assertTrue(id2 > id1)
        assertTrue(id3 > id2)
    }

    @Test
    fun invoke_同一天多条记录_按日期查询返回全部() = runTest {
        useCase(trackerId = 1L, date = "2024-06-15", time = "10:00")
        useCase(trackerId = 1L, date = "2024-06-15", time = "14:00")
        useCase(trackerId = 1L, date = "2024-06-15", time = "18:00")
        val dayRecords = fakeRepository.getRecordsByTrackerAndDate(1L, "2024-06-15").first()
        assertEquals(3, dayRecords.size)
    }

    @Test
    fun invoke_不同日期记录_按日期查询仅返回对应日期() = runTest {
        useCase(trackerId = 1L, date = "2024-06-15", time = "10:00")
        useCase(trackerId = 1L, date = "2024-06-16", time = "10:00")
        val day1Records = fakeRepository.getRecordsByTrackerAndDate(1L, "2024-06-15").first()
        val day2Records = fakeRepository.getRecordsByTrackerAndDate(1L, "2024-06-16").first()
        assertEquals(1, day1Records.size)
        assertEquals(1, day2Records.size)
    }
}
