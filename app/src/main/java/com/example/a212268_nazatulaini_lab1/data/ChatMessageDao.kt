package com.example.a212268_nazatulaini_lab1.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages WHERE ownerName = :owner AND itemName = :item ORDER BY timestamp ASC")
    fun getMessagesForConversation(owner: String, item: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE ownerName = :owner AND itemName = :item")
    suspend fun deleteConversation(owner: String, item: String)
}