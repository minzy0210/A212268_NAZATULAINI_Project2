package com.example.a212268_nazatulaini_lab1.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations")
    fun getAll(): Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReservationEntity)

    @Query("SELECT * FROM reservations WHERE itemName = :name LIMIT 1")
    suspend fun getByName(name: String): ReservationEntity?
}

@Dao
interface BorrowedItemDao {
    @Query("SELECT * FROM borrowed_items")
    fun getAll(): Flow<List<BorrowedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: BorrowedItemEntity)

    @Query("SELECT itemName FROM borrowed_items")
    suspend fun getAllNames(): List<String>
}