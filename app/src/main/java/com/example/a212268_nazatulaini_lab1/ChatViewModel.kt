package com.example.a212268_nazatulaini_lab1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a212268_nazatulaini_lab1.data.ChatMessageDao
import com.example.a212268_nazatulaini_lab1.data.ChatMessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.map

class ChatViewModel(private val chatMessageDao: ChatMessageDao) : ViewModel() {

    // All messages grouped by "owner::item" key
    val messages: StateFlow<Map<String, List<ChatMessage>>> =
        chatMessageDao.getAllConversations()
            .map { entities ->
                entities.groupBy { "${it.ownerName}::${it.itemName}" }
                    .mapValues { (_, msgs) ->
                        msgs.sortedBy { it.timestamp }
                            .map { it.toDomain() }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // Distinct conversations (latest message per owner+item)
    val conversations: StateFlow<List<Conversation>> =
        chatMessageDao.getAllConversations()
            .map { entities ->
                entities
                    .groupBy { "${it.ownerName}::${it.itemName}" }
                    .values
                    .map { msgs ->
                        val last = msgs.maxByOrNull { it.timestamp }!!
                        Conversation(
                            ownerName    = last.ownerName,
                            itemName     = last.itemName,
                            itemImageRes = last.itemImageRes,
                            lastMessage  = last.text,
                            timestamp    = last.timestamp,
                            unreadCount  = msgs.count { !it.isFromMe }
                        )
                    }
                    .sortedByDescending { it.timestamp }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun startConversation(ownerName: String, itemName: String, itemImageRes: Int) {
        viewModelScope.launch {
            val key = "$ownerName::$itemName"
            val existing = messages.value[key]
            if (existing.isNullOrEmpty()) {
                val now = nowTimestamp()
                chatMessageDao.insert(ChatMessageEntity(
                    id = System.currentTimeMillis().toString(),
                    ownerName = ownerName, itemName = itemName,
                    itemImageRes = itemImageRes,
                    text = "Hi! I'm interested in your $itemName.",
                    isFromMe = true, timestamp = now
                ))
                chatMessageDao.insert(ChatMessageEntity(
                    id = (System.currentTimeMillis() + 1).toString(),
                    ownerName = ownerName, itemName = itemName,
                    itemImageRes = itemImageRes,
                    text = "Hello! Sure, feel free to ask me anything about it.",
                    isFromMe = false, timestamp = nowTimestamp()
                ))
            }
        }
    }

    fun sendMessage(ownerName: String, itemName: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val imageRes = messages.value["$ownerName::$itemName"]
                ?.firstOrNull()?.let {
                    // recover imageRes from existing messages
                    chatMessageDao.getAllConversations().first()
                        .firstOrNull { e -> e.ownerName == ownerName && e.itemName == itemName }
                        ?.itemImageRes ?: getItemImage(itemName)
                } ?: getItemImage(itemName)

            chatMessageDao.insert(ChatMessageEntity(
                id           = System.currentTimeMillis().toString(),
                ownerName    = ownerName,
                itemName     = itemName,
                itemImageRes = imageRes,
                text         = text,
                isFromMe     = true,
                timestamp    = nowTimestamp()
            ))
        }
    }

    fun markAsRead(ownerName: String, itemName: String) { /* unread count is derived from DB */ }

    private fun nowTimestamp() =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    // Factory — ChatViewModel now needs a DAO
    class Factory(private val dao: ChatMessageDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChatViewModel(dao) as T
    }
}

// Mapper
fun ChatMessageEntity.toDomain() = ChatMessage(
    id        = id,
    text      = text,
    isFromMe  = isFromMe,
    timestamp = timestamp
)