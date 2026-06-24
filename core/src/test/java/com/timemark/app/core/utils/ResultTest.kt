package com.timemark.app.core.utils

import com.timemark.app.core.utils.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Result 单元测试
 *
 * 覆盖 Success/Error/Loading 三种状态及其转换方法。
 */
class ResultTest {

    @Test
    fun Success_获取数据_返回正确值() {
        val result = Result.Success(42)
        assertEquals(42, result.data)
    }

    @Test
    fun Error_获取消息_返回正确文本() {
        val result = Result.Error("网络错误")
        assertEquals("网络错误", result.message)
        assertNull(result.cause)
    }

    @Test
    fun Error_带异常_保留异常信息() {
        val exception = RuntimeException("连接超时")
        val result = Result.Error("请求失败", exception)
        assertEquals("请求失败", result.message)
        assertEquals(exception, result.cause)
    }

    @Test
    fun Loading_类型判断_正确识别() {
        val result: Result<Int> = Result.Loading
        assertTrue(result is Result.Loading)
    }

    @Test
    fun map_Success状态_应用变换() {
        val result = Result.Success(10)
        val mapped = result.map { it * 2 }
        assertTrue(mapped is Result.Success)
        assertEquals(20, (mapped as Result.Success).data)
    }

    @Test
    fun map_Error状态_不变换() {
        val result: Result<Int> = Result.Error("失败")
        val mapped = result.map { it * 2 }
        assertTrue(mapped is Result.Error)
        assertEquals("失败", (mapped as Result.Error).message)
    }

    @Test
    fun map_Loading状态_不变换() {
        val result: Result<Int> = Result.Loading
        val mapped = result.map { it * 2 }
        assertTrue(mapped is Result.Loading)
    }

    @Test
    fun map_Success转字符串_正确变换() {
        val result = Result.Success(100)
        val mapped = result.map { "值: $it" }
        assertTrue(mapped is Result.Success)
        assertEquals("值: 100", (mapped as Result.Success).data)
    }

    @Test
    fun onSuccess_Success状态_执行回调() {
        var captured = 0
        val result = Result.Success(99)
        result.onSuccess { captured = it }
        assertEquals(99, captured)
    }

    @Test
    fun onSuccess_Error状态_不执行回调() {
        var executed = false
        val result: Result<Int> = Result.Error("失败")
        result.onSuccess { executed = true }
        assertFalse(executed)
    }

    @Test
    fun onSuccess_Loading状态_不执行回调() {
        var executed = false
        val result: Result<Int> = Result.Loading
        result.onSuccess { executed = true }
        assertFalse(executed)
    }

    @Test
    fun onSuccess_返回原始Result() {
        val result = Result.Success(42)
        val returned = result.onSuccess { }
        assertEquals(result, returned)
    }

    @Test
    fun onError_Error状态_执行回调() {
        var capturedMsg = ""
        var capturedCause: Throwable? = null
        val exception = RuntimeException("超时")
        val result: Result<Int> = Result.Error("请求失败", exception)
        result.onError { msg, cause ->
            capturedMsg = msg
            capturedCause = cause
        }
        assertEquals("请求失败", capturedMsg)
        assertEquals(exception, capturedCause)
    }

    @Test
    fun onError_Success状态_不执行回调() {
        var executed = false
        val result = Result.Success(42)
        result.onError { _, _ -> executed = true }
        assertFalse(executed)
    }

    @Test
    fun onError_Loading状态_不执行回调() {
        var executed = false
        val result: Result<Int> = Result.Loading
        result.onError { _, _ -> executed = true }
        assertFalse(executed)
    }

    @Test
    fun onError_返回原始Result() {
        val result: Result<Int> = Result.Error("失败")
        val returned = result.onError { _, _ -> }
        assertEquals(result, returned)
    }

    @Test
    fun 链式调用_onSuccess后继续链式() {
        var value = 0
        val result = Result.Success(10)
        result
            .onSuccess { value += it }
            .map { it + 5 }
            .onSuccess { value += it }
        // 第一次 onSuccess: value = 10
        // map 后变成 Success(15)
        // 第二次 onSuccess: value = 10 + 15 = 25
        assertEquals(25, value)
    }

    @Test
    fun Error_链式调用_onError后继续链式() {
        var errorMsg = ""
        val result: Result<Int> = Result.Error("网络异常")
        result
            .onError { msg, _ -> errorMsg = msg }
            .map { it * 2 }
            .onError { msg, _ -> errorMsg = "$msg-链式" }
        // 第一次 onError: errorMsg = "网络异常"
        // map 不改变 Error
        // 第二次 onError: errorMsg = "网络异常-链式"
        assertEquals("网络异常-链式", errorMsg)
    }
}
