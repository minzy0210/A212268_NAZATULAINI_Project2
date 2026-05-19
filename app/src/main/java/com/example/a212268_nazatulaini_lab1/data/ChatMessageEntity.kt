package com.example.a212268_nazatulaini_lab1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val ownerName: String,
    val itemName: String,
    val itemImageRes: Int,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: String
)