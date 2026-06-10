package com.example.a212268_nazatulaini_lab1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    // Only one profile row — use a fixed ID of 1
    @PrimaryKey val id: Int = 1,
    val displayName: String = "",
    val bio: String = "",
    val location: String = "",
    val phoneNumber: String = "",
    val joinDate: String = ""
)