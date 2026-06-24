package com.timemark.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TrackerTemplates 模板库单元测试
 *
 * 验证模板完整性：24 个模板，分类正确，字段有效。
 */
class TrackerTemplatesTest {

    @Test
    fun all_总模板数_应为24个() {
        assertEquals(24, TrackerTemplates.all.size)
    }

    @Test
    fun health_健康类模板_应为8个() {
        assertEquals(8, TrackerTemplates.health.size)
    }

    @Test
    fun study_学习类模板_应为6个() {
        assertEquals(6, TrackerTemplates.study.size)
    }

    @Test
    fun life_生活类模板_应为6个() {
        assertEquals(6, TrackerTemplates.life.size)
    }

    @Test
    fun work_工作类模板_应为4个() {
        assertEquals(4, TrackerTemplates.work.size)
    }

    @Test
    fun all_模板总数_等于各分类之和() {
        val sum = TrackerTemplates.health.size +
                TrackerTemplates.study.size +
                TrackerTemplates.life.size +
                TrackerTemplates.work.size
        assertEquals(sum, TrackerTemplates.all.size)
    }

    @Test
    fun all_模板ID_全部唯一() {
        val ids = TrackerTemplates.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun health_所有模板_分类为健康() {
        TrackerTemplates.health.forEach { template ->
            assertEquals(TrackerTemplates.CATEGORY_HEALTH, template.category)
        }
    }

    @Test
    fun study_所有模板_分类为学习() {
        TrackerTemplates.study.forEach { template ->
            assertEquals(TrackerTemplates.CATEGORY_STUDY, template.category)
        }
    }

    @Test
    fun life_所有模板_分类为生活() {
        TrackerTemplates.life.forEach { template ->
            assertEquals(TrackerTemplates.CATEGORY_LIFE, template.category)
        }
    }

    @Test
    fun work_所有模板_分类为工作() {
        TrackerTemplates.work.forEach { template ->
            assertEquals(TrackerTemplates.CATEGORY_WORK, template.category)
        }
    }

    @Test
    fun all_所有模板_名称非空() {
        TrackerTemplates.all.forEach { template ->
            assertTrue("模板 ${template.id} 的名称不应为空", template.name.isNotEmpty())
        }
    }

    @Test
    fun all_所有模板_图标非空() {
        TrackerTemplates.all.forEach { template ->
            assertTrue("模板 ${template.id} 的图标不应为空", template.icon.isNotEmpty())
        }
    }

    @Test
    fun all_所有模板_颜色为有效十六进制() {
        TrackerTemplates.all.forEach { template ->
            assertTrue(
                "模板 ${template.id} 的颜色 ${template.color} 应以 # 开头",
                template.color.startsWith("#")
            )
            // #RRGGBB 格式，长度为 7
            assertEquals(7, template.color.length)
        }
    }

    @Test
    fun all_所有模板_单位非空() {
        TrackerTemplates.all.forEach { template ->
            assertTrue("模板 ${template.id} 的单位不应为空", template.unit.isNotEmpty())
        }
    }

    @Test
    fun all_所有模板_描述非空() {
        TrackerTemplates.all.forEach { template ->
            assertTrue("模板 ${template.id} 的描述不应为空", template.description.isNotEmpty())
        }
    }

    @Test
    fun all_所有模板_ID非空() {
        TrackerTemplates.all.forEach { template ->
            assertTrue("模板 ID 不应为空", template.id.isNotEmpty())
        }
    }

    @Test
    fun getByCategory_健康_返回健康模板列表() {
        val result = TrackerTemplates.getByCategory(TrackerTemplates.CATEGORY_HEALTH)
        assertEquals(TrackerTemplates.health, result)
    }

    @Test
    fun getByCategory_学习_返回学习模板列表() {
        val result = TrackerTemplates.getByCategory(TrackerTemplates.CATEGORY_STUDY)
        assertEquals(TrackerTemplates.study, result)
    }

    @Test
    fun getByCategory_生活_返回生活模板列表() {
        val result = TrackerTemplates.getByCategory(TrackerTemplates.CATEGORY_LIFE)
        assertEquals(TrackerTemplates.life, result)
    }

    @Test
    fun getByCategory_工作_返回工作模板列表() {
        val result = TrackerTemplates.getByCategory(TrackerTemplates.CATEGORY_WORK)
        assertEquals(TrackerTemplates.work, result)
    }

    @Test
    fun getByCategory_未知分类_返回空列表() {
        val result = TrackerTemplates.getByCategory("未知分类")
        assertTrue(result.isEmpty())
    }

    @Test
    fun getByCategory_空字符串_返回空列表() {
        val result = TrackerTemplates.getByCategory("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun health_饮水模板_字段正确() {
        val water = TrackerTemplates.health.find { it.id == "health_water" }
        assertNotNull(water)
        assertEquals("每日饮水", water!!.name)
        assertEquals("💧", water.icon)
        assertEquals("#2196F3", water.color)
        assertEquals(TrackerType.COUNT, water.type)
        assertEquals("杯", water.unit)
        assertEquals(8.0, water.targetValue, 0.001)
    }

    @Test
    fun health_体重模板_目标值为0() {
        val weight = TrackerTemplates.health.find { it.id == "health_weight" }
        assertNotNull(weight)
        assertEquals(0.0, weight!!.targetValue, 0.001)
    }

    @Test
    fun work_番茄钟模板_类型为TIMER() {
        val pomodoro = TrackerTemplates.work.find { it.id == "work_pomodoro" }
        assertNotNull(pomodoro)
        assertEquals(TrackerType.TIMER, pomodoro!!.type)
    }

    @Test
    fun all_所有模板类型_在有效枚举范围内() {
        TrackerTemplates.all.forEach { template ->
            assertNotNull(
                "模板 ${template.id} 的类型 ${template.type} 应在有效枚举范围内",
                template.type
            )
        }
    }

    @Test
    fun 分类常量_值正确() {
        assertEquals("健康", TrackerTemplates.CATEGORY_HEALTH)
        assertEquals("学习", TrackerTemplates.CATEGORY_STUDY)
        assertEquals("生活", TrackerTemplates.CATEGORY_LIFE)
        assertEquals("工作", TrackerTemplates.CATEGORY_WORK)
    }
}
