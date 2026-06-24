package com.timemark.app.feature.ai.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timemark.app.domain.model.ChatHistoryEntry
import com.timemark.app.domain.model.ChatMessage
import com.timemark.app.domain.repository.ChatHistoryRepository
import com.timemark.app.domain.usecase.ai.ChatWithAIUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI 聊天 ViewModel（Task 33.2 增强）
 *
 * 增强：
 * - 启动时从本地数据库加载历史对话
 * - 每条新消息（user/assistant）异步保存到数据库
 * - 支持清空历史
 * - 支持删除单条消息
 */
@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val chatWithAIUseCase: ChatWithAIUseCase,
    private val chatHistoryRepository: ChatHistoryRepository
) : ViewModel() {

    /** 当前对话消息列表（按时间正序） */
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    /** 历史记录列表（按时间倒序，用于侧滑删除等 UI 操作） */
    val historyEntries: StateFlow<List<ChatHistoryEntry>> = chatHistoryRepository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // 启动时加载历史对话
        loadHistory()
    }

    /** 从数据库加载历史对话到内存 */
    private fun loadHistory() {
        viewModelScope.launch {
            chatHistoryRepository.getAllHistoryAsc().collect { entries ->
                // 仅展示 user/assistant 消息，过滤 system 提示
                val restored = entries
                    .filter { it.role == "user" || it.role == "assistant" }
                    .map { ChatMessage(role = it.role, content = it.content) }
                _messages.value = restored
            }
        }
    }

    /** 发送消息 */
    fun send(content: String) {
        if (content.isBlank() || _isLoading.value) return

        val userMessage = ChatMessage(role = "user", content = content)
        _messages.value = _messages.value + userMessage

        // 保存用户消息到数据库
        saveMessage(userMessage, 0)

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            // 构造完整对话历史（包含系统提示）
            val history = buildList {
                add(ChatMessage(role = "system", content = "你是 TimeMark 应用的智能助手，可以帮助用户管理打卡习惯、分析数据、提供建议。请用简洁友好的中文回复。"))
                addAll(_messages.value)
            }

            val result = chatWithAIUseCase.chat(history)
            _isLoading.value = false

            when (result) {
                is com.timemark.app.domain.Result.Success -> {
                    val assistantMessage = ChatMessage(role = "assistant", content = result.data.content)
                    _messages.value = _messages.value + assistantMessage
                    // 保存 AI 回复到数据库（记录输出 Token 数）
                    saveMessage(assistantMessage, result.data.tokensOutput)
                }
                is com.timemark.app.domain.Result.Error -> {
                    _error.value = result.message
                }
                is com.timemark.app.domain.Result.Loading -> {
                    // 不会发生
                }
            }
        }
    }

    /** 保存单条消息到数据库 */
    private fun saveMessage(message: ChatMessage, tokenCount: Int) {
        viewModelScope.launch {
            val entry = ChatHistoryEntry(
                provider = "CHAT",
                role = message.role,
                content = message.content,
                timestamp = System.currentTimeMillis(),
                tokenCount = tokenCount
            )
            runCatching { chatHistoryRepository.insert(entry) }
        }
    }

    /** 删除单条历史记录（按 ID） */
    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            runCatching { chatHistoryRepository.deleteById(id) }
        }
    }

    /** 清空全部对话历史 */
    fun clear() {
        viewModelScope.launch {
            runCatching { chatHistoryRepository.clearAll() }
            _messages.value = emptyList()
            _error.value = null
        }
    }
}
