// app/src/main/java/com/example/a212268_nazatulaini_lab1/data/UserProfileDao.kt

package com.example.a212268_nazatulaini_lab1.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    // Observe profile as a live stream — UI auto-updates when it changes
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    // Insert (first time) or replace (edit)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)
}